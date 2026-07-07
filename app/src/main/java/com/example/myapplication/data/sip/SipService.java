package com.example.myapplication.data.sip;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myapplication.data.model.CallState;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class SipService {

    private static final String TAG = "SipService";

    private static SipService instance;
    private final Context context;

    private int localPort = 5070;
    private String localIpAddress = "";

    private String callId;
    private String nonce;
    private String realm;
    private String qop;
    private int cseq = 1;
    private boolean isRegistered = false;
    private int registerRetryCount = 0;
    private int authRetryCount = 0;
    private static final int MAX_REGISTER_RETRY = 3;
    private static final int MAX_AUTH_RETRY = 3;
    private static final long REGISTER_RETRY_DELAY = 3000;

    private DatagramSocket udpSocket;
    private Thread receiveThread;
    private boolean isRunning = false;

    private CallState callState = CallState.IDLE;
    private SipCallback callback;
    private final Handler retryHandler = new Handler(Looper.getMainLooper());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Set<String> processedInviteCseqs = new HashSet<>();
    private String ackMessage;

    public interface SipCallback {
        void onRegistered();
        void onRegistrationFailed(String error);
        void onUnregistered();
        void onCallIncoming(String caller);
        void onCallConnected();
        void onCallEnded();
        void onCallFailed(String error);
    }

    private SipService(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized SipService getInstance(Context context) {
        if (instance == null) {
            instance = new SipService(context);
        }
        return instance;
    }

    public void setCallback(SipCallback callback) {
        this.callback = callback;
    }

    public boolean register() {
        registerRetryCount = 0;
        authRetryCount = 0;
        initSip();
        sendRegisterRequest();
        return true;
    }

    private void initSip() {
        try {
            localIpAddress = getLocalIpAddress();
            Log.d(TAG, "Local IP: " + localIpAddress);

            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
            }

            udpSocket = new DatagramSocket(localPort);
            isRunning = true;

            receiveThread = new Thread(() -> {
                byte[] buffer = new byte[4096];
                while (isRunning) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        udpSocket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());
                        String from = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                        Log.d(TAG, "Received from " + from + ":\n" + message);
                        parseSipResponse(message);
                    } catch (IOException e) {
                        if (isRunning) {
                            Log.e(TAG, "Receive error: " + e.getMessage());
                        }
                        break;
                    }
                }
            });
            receiveThread.start();

            Log.d(TAG, "SIP initialized successfully");
        } catch (SocketException e) {
            Log.e(TAG, "SIP init failed: " + e.getMessage());
        }
    }

    private String getLocalIpAddress() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
                java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()
                            && address instanceof java.net.Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting IP: " + e.getMessage());
        }
        return "127.0.0.1";
    }

    private void parseSipResponse(String message) {
        if (message.contains("401 Unauthorized")) {
            authRetryCount++;
            if (authRetryCount > MAX_AUTH_RETRY) {
                Log.e(TAG, "Auth retry limit reached (" + MAX_AUTH_RETRY + ")");
                if (callback != null) {
                    mainHandler.post(() -> callback.onRegistrationFailed("认证失败，已重试" + MAX_AUTH_RETRY + "次"));
                }
                return;
            }
            Log.d(TAG, "Received 401 (attempt " + authRetryCount + "/" + MAX_AUTH_RETRY + ")");
            realm = extractHeaderValue(message, "WWW-Authenticate:", "realm");
            nonce = extractHeaderValue(message, "WWW-Authenticate:", "nonce");
            qop = extractHeaderValue(message, "WWW-Authenticate:", "qop");
            sendRegisterRequest();
        } else if (message.contains("200 OK")) {
            if (message.contains("REGISTER")) {
                isRegistered = true;
                registerRetryCount = 0;
                authRetryCount = 0;
                Log.d(TAG, "Registration successful");
                if (callback != null) {
                    mainHandler.post(() -> callback.onRegistered());
                }
            } else if (message.contains("CSeq") && message.contains("INVITE")) {
                Log.d(TAG, "Received INVITE");
                String pattern = "CSeq:\\s*(\\d+\\s+\\w+)";
                java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE).matcher(message);
                String CSeq = m.find() ? m.group(1) : "";
                if (!processedInviteCseqs.contains(CSeq)) {
                    String from = extractDeviceIdFromMessage(message);
                    ackMessage = message;
                    sendAckMessage(message, from);
                    if (callback != null) {
                        mainHandler.post(() -> callback.onCallIncoming(from));
                    }
                }
                if (!CSeq.isEmpty()) processedInviteCseqs.add(CSeq);
            }
        } else if (message.contains("403 No Register")) {
            realm = "";
            nonce = "";
            qop = "";
            registerRetryCount = 0;
            sendRegisterRequest();
        }
    }

    private String extractHeaderValue(String message, String header, String param) {
        if (message.contains(header)) {
            String headerLine = message.split(header)[1].split("\r\n")[0].trim();
            if (param != null && headerLine.contains(param + "=\"")) {
                return headerLine.split(param + "=\"")[1].split("\"")[0];
            } else if (param != null && headerLine.contains(param + "=")) {
                String val = headerLine.split(param + "=")[1];
                if (val.contains(" ")) val = val.split(" ")[0];
                return val;
            } else if (param == null) {
                return headerLine;
            }
        }
        return null;
    }

    private String extractDeviceIdFromMessage(String message) {
        if (message.contains("To:")) {
            String toLine = message.split("To:")[1].split("\r\n")[0].trim();
            if (toLine.contains("sip:")) {
                String sipUri = toLine.split("sip:")[1];
                if (sipUri.contains("@")) return sipUri.split("@")[0];
            }
        }
        return null;
    }

    private void sendRegisterRequest() {
        new Thread(() -> {
            try {
                if (udpSocket == null || udpSocket.isClosed()) {
                    udpSocket = new DatagramSocket(localPort);
                }
                SipConfig config = SipConfig.getInstance();
                String msg = buildRegisterMessage();
                byte[] buffer = msg.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                        InetAddress.getByName(config.getServerHost()), config.getServerPort());
                udpSocket.send(packet);
                Log.d(TAG, "Sent REGISTER to " + config.getServerHost());
            } catch (Exception e) {
                Log.e(TAG, "Error sending register: " + e.getMessage());
                registerRetryCount++;
                if (registerRetryCount < MAX_REGISTER_RETRY) {
                    retryHandler.postDelayed(this::sendRegisterRequest, REGISTER_RETRY_DELAY);
                } else if (callback != null) {
                    callback.onRegistrationFailed(e.getMessage());
                }
            }
        }).start();
    }

    private String buildRegisterMessage() {
        SipConfig config = SipConfig.getInstance();
        StringBuilder sb = new StringBuilder();
        if (callId == null) callId = System.currentTimeMillis() + "@" + localIpAddress;

        sb.append("REGISTER sip:").append(config.getServerHost()).append(":").append(config.getServerPort()).append(" SIP/2.0\r\n");
        sb.append("Via: SIP/2.0/UDP ").append(localIpAddress).append(":").append(localPort).append(";branch=z9hG4bK").append(System.currentTimeMillis()).append(";rport\r\n");
        sb.append("From: <sip:").append(config.getUsername()).append("@").append(config.getServerHost()).append(">;tag=").append(System.currentTimeMillis()).append("\r\n");
        sb.append("To: <sip:").append(config.getUsername()).append("@").append(config.getServerHost()).append(">\r\n");
        sb.append("Call-ID: ").append(callId).append("\r\n");
        sb.append("CSeq: ").append(cseq).append(" REGISTER\r\n");
        sb.append("Contact: <sip:").append(config.getUsername()).append("@").append(localIpAddress).append(":").append(localPort).append(";ob>\r\n");

        if (nonce != null && !nonce.isEmpty() && realm != null && !realm.isEmpty()) {
            String cnonce = System.currentTimeMillis() + "-cnonce";
            String uri = "sip:" + config.getServerHost() + ":" + config.getServerPort();
            String ha1 = md5(config.getUsername() + ":" + realm + ":" + config.getPassword());
            String ha2 = md5("REGISTER:" + uri);
            String response = md5(ha1 + ":" + nonce + ":00000001:" + cnonce + ":" + qop + ":" + ha2);
            sb.append("Authorization: Digest username=\"").append(config.getUsername()).append("\", realm=\"").append(realm)
                    .append("\", nonce=\"").append(nonce).append("\", uri=\"").append(uri)
                    .append("\", cnonce=\"").append(cnonce).append("\", qop=").append(qop)
                    .append(", response=\"").append(response).append("\", nc=00000001\r\n");
        }

        sb.append("Expires: 3600\r\n");
        sb.append("Max-Forwards: 70\r\n");
        sb.append("User-Agent: AppClient\r\n");
        sb.append("Content-Length: 0\r\n");
        sb.append("\r\n");
        cseq++;
        return sb.toString();
    }

    private void sendAckMessage(String message, String from) {
        try {
            String contactAddr = extractHeaderValue(message, "Contact:", null);
            if (contactAddr != null && contactAddr.startsWith("<") && contactAddr.endsWith(">")) {
                contactAddr = contactAddr.substring(1, contactAddr.length() - 1);
            }
            if (contactAddr == null || contactAddr.isEmpty()) return;

            String fromAddr = message.contains("From: ") ? message.split("From: ")[1].split("\r\n")[0].trim() : null;
            String toAddr = message.contains("To: ") ? message.split("To: ")[1].split("\r\n")[0].trim() : null;
            String branch = extractHeaderValue(message, "Via:", "branch");
            String cid = extractHeaderValue(message, "Call-ID:", null);
            String cseqValue = extractHeaderValue(message, "CSeq:", null);
            if (cseqValue != null) cseqValue = cseqValue.split(" ")[0];

            SipConfig config = SipConfig.getInstance();
            StringBuilder ack = new StringBuilder();
            ack.append("ACK ").append(contactAddr).append(" SIP/2.0\r\n");
            ack.append("Via: SIP/2.0/UDP ").append(localIpAddress).append(":").append(localPort).append(";rport;branch=").append(branch).append("\r\n");
            ack.append("Max-Forwards: 70\r\n");
            ack.append("From: ").append(fromAddr).append("\r\n");
            ack.append("To: ").append(toAddr).append("\r\n");
            ack.append("Call-ID: ").append(cid).append("\r\n");
            ack.append("CSeq: ").append(cseqValue).append(" ACK\r\n");
            ack.append("Contact: <sip:").append(config.getUsername()).append("@").append(localIpAddress).append(":").append(localPort).append(";ob>\r\n");
            ack.append("Content-Length: 0\r\n");
            ack.append("\r\n");

            new Thread(() -> {
                try {
                    byte[] buffer = ack.toString().getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                            InetAddress.getByName(config.getServerHost()), config.getServerPort());
                    udpSocket.send(packet);
                    Log.d(TAG, "Sent ACK");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to send ACK: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Failed to build ACK: " + e.getMessage());
        }
    }

    public boolean makeCall(String targetUsername) {
        Log.d(TAG, "makeCall: " + targetUsername);
        callState = CallState.CALLING;
        return true;
    }

    public void answerCall() {
        Log.d(TAG, "answerCall");
        callState = CallState.CONNECTED;
        if (callback != null) callback.onCallConnected();
    }

    public void hangUp() {
        Log.d(TAG, "hangUp");
        callState = CallState.ENDED;
        if (callback != null) callback.onCallEnded();
    }

    public void unregister() {
        isRunning = false;
        if (receiveThread != null) receiveThread.interrupt();
        if (udpSocket != null && !udpSocket.isClosed()) {
            try { udpSocket.close(); } catch (Exception e) { }
        }
    }

    public boolean isRegistered() { return isRegistered; }
    public String getRegistrationError() { return null; }
    public CallState getCallState() { return callState; }

    public void destroy() {
        unregister();
        instance = null;
    }

    /**
     * 发送SIP消息（用于PTZ控制等）
     */
    public void sendTextMessage(String message) {
        new Thread(() -> {
            try {
                if (udpSocket == null || udpSocket.isClosed()) {
                    udpSocket = new DatagramSocket(localPort);
                }
                SipConfig config = SipConfig.getInstance();
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                        InetAddress.getByName(config.getServerHost()), config.getServerPort());
                udpSocket.send(packet);
                Log.d(TAG, "Sent SIP message");
            } catch (Exception e) {
                Log.e(TAG, "Failed to send SIP message: " + e.getMessage());
            }
        }).start();
    }

    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
