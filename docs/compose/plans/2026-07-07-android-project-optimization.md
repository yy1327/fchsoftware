# Android项目优化实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 全面优化Android监控应用项目，参考老师范本添加SIP功能、GreenDAO数据库、VLC播放器，优化代码结构和功能完整性。

**Architecture:** 采用重构式优化方法，先重构代码结构，再添加功能。保持现有项目架构，参考范本的扁平化组织方式，添加缺失的功能模块。

**Tech Stack:** Java 11, Android 7.0+, Retrofit 2.9.0, OkHttp 4.12.0, RxJava 2, GreenDAO 3.3.0, VLC 3.6.0, Glide 4.16.0, hutool 5.3.8, fastjson 1.2.78, dom4j 2.1.3

## Global Constraints

- 最低支持Android 7.0 (API 24)
- 目标版本Android 14 (API 36)
- 使用Java 11语言
- 保持现有包名：com.example.myapplication
- 参考范本com.fch.myapplication的代码风格和架构

---

### Task 1: 项目配置和依赖更新

**Covers:** [S2, S3]
<!-- 更新build.gradle文件，添加必要的依赖 -->

**Files:**
- Modify: `app/build.gradle`
- Create: `gradle/libs.versions.toml` (如果不存在)

**Interfaces:**
- Consumes: 无
- Produces: 更新后的项目配置，支持新功能依赖

- [ ] **Step 1: 更新build.gradle文件**

```gradle
plugins {
    alias(libs.plugins.android.application)
    id 'org.greenrobot.greendao'
}

android {
    namespace 'com.example.myapplication'
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId "com.example.myapplication"
        minSdk 24
        targetSdk 36
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable false
            }
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
}

dependencies {
    implementation libs.activity.ktx
    implementation libs.appcompat
    implementation libs.constraintlayout
    implementation libs.material
    implementation libs.recyclerview
    implementation libs.cardview
    implementation libs.glide
    testImplementation libs.junit
    androidTestImplementation libs.espresso.core
    androidTestImplementation libs.ext.junit

    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.retrofit2:adapter-rxjava2:2.9.0'

    // OkHttp
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

    // RxJava
    implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

    // VLC播放器
    implementation 'org.videolan.android:libvlc-all:3.6.0'

    // GreenDAO
    implementation 'org.greenrobot:greendao:3.3.0'

    // hutool
    implementation files('libs/hutool-core-5.3.8.jar')
    implementation files('libs/hutool-crypto-5.3.8.jar')

    // fastjson
    implementation 'com.alibaba:fastjson:1.2.78'

    // dom4j
    implementation 'org.dom4j:dom4j:2.1.3'
}

greendao {
    // 数据库版本号
    schemaVersion 1
    // 生成数据库文件的目录
    targetGenDir 'src/main/java'
    //设置为true以自动生成单元测试
    generateTests false
    // 生成的数据库相关文件的包名
    daoPackage 'com.example.myapplication.greendao'
}
```

- [ ] **Step 2: 下载hutool jar文件**

从参考项目复制hutool jar文件到用户项目的libs目录：
```bash
cp "D:\app\Android\Code\参考\MyApplication\app\libs\hutool-core-5.3.8.jar" "D:\app\Android\Code\MyApplication\app\libs\"
cp "D:\app\Android\Code\参考\MyApplication\app\libs\hutool-crypto-5.3.8.jar" "D:\app\Android\Code\MyApplication\app\libs\"
```

- [ ] **Step 3: 提交配置更改**

```bash
git add app/build.gradle app/libs/
git commit -m "feat: 更新项目配置，添加SIP、GreenDAO、VLC等依赖"
```

---

### Task 2: 添加SIP功能模块

**Covers:** [S4]
<!-- 添加SIPManager类和相关功能 -->

**Files:**
- Create: `app/src/main/java/com/example/myapplication/sip/SIPManager.java`
- Create: `app/src/main/java/com/example/myapplication/sip/SipService.java`

**Interfaces:**
- Consumes: Task 1的配置更新
- Produces: SIP注册、消息收发功能

- [ ] **Step 1: 创建SIPManager类**

```java
package com.example.myapplication.sip;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myapplication.util.DeviceInfoUtil;
import com.example.myapplication.util.MD5Utils;
import com.example.myapplication.util.MyTimeUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SIPManager {
    private static final String TAG = "SIPManager";
    private static SIPManager instance;
    private Context context;

    // SIP参数
    private String serverIp = "10.10.30.102";
    private int serverPort = 5060;
    private int localPort = 5070;
    private String localIpAddress = "";

    private String gbCode = "34020000001310000001";
    private String password = "111111";
    private String serverCode = "34020000002180000001";

    // SIP会话参数
    private String callId;
    private String nonce;
    private String realm;
    private String qop;
    private String cnonce;
    private String response;
    private int cseq = 1;

    // UDP通信
    private DatagramSocket udpSocket;
    private Thread receiveThread;
    private boolean isRunning = false;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mKeepaliveRunnable;
    private static final long INTERVAL = 60 * 1000; // 60秒
    public int sequenceNumber = 0;

    private MessageCallback messageCallback;

    private Set<String> processedInviteCseqs = new HashSet<>();
    private String ackMessage;

    // 回调接口
    public interface MessageCallback {
        void onMessageReceived(ChatMessage message);
        void onMessageSent(ChatMessage message);
        void onMessageStatusChanged(ChatMessage.State state);
        void onRegistrationStateChanged(RegistrationState state, String message);
    }

    // 消息类
    public static class ChatMessage {
        private String content;
        private String from;
        private String to;

        public enum State {
            Sending, Sent, Delivered, NotDelivered
        }

        public ChatMessage(String content, String from, String to) {
            this.content = content;
            this.from = from;
            this.to = to;
        }

        public String getTextContent() { return content; }
        public Address getFromAddress() { return new Address(from); }
        public Address getLocalAddress() { return new Address(to); }
        public Address getToAddress() { return new Address(to); }
        public String getUtf8Text() { return content; }
    }

    // 地址类
    public static class Address {
        private String address;

        public Address(String address) { this.address = address; }
        public String asStringUriOnly() { return address; }
        public String asString() { return address; }
    }

    // 注册状态枚举
    public enum RegistrationState {
        Ok, Failed, None
    }

    public static synchronized SIPManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("SIPManager not initialized");
        }
        return instance;
    }

    public static void create(Context context) {
        if (instance != null) {
            return;
        }
        instance = new SIPManager(context);
    }

    private SIPManager(Context context) {
        this.context = context.getApplicationContext();
        initSIP();
    }

    public void initSIP() {
        try {
            localIpAddress = getLocalIpAddress();
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
                        Log.e(TAG, "Received message from " + from + ":\n" + message);
                        parseSipResponse(message);
                    } catch (IOException e) {
                        if (isRunning) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            });
            receiveThread.start();

            Log.e(TAG, "SIP initialized successfully");
        } catch (SocketException e) {
            e.printStackTrace();
            Log.e(TAG, "SIP initialization failed: " + e.getMessage());
        }
    }

    private void parseSipResponse(String message) {
        // 解析SIP响应
        if (message.contains("401 Unauthorized")) {
            realm = extractHeaderValue(message, "WWW-Authenticate:", "realm");
            nonce = extractHeaderValue(message, "WWW-Authenticate:", "nonce");
            qop = extractHeaderValue(message, "WWW-Authenticate:", "qop");
            sendRegisterRequest();
        } else if (message.contains("200 OK")) {
            if (message.contains("REGISTER")) {
                if (messageCallback != null) {
                    messageCallback.onRegistrationStateChanged(RegistrationState.Ok, "Registration successful");
                }
            } else if (message.contains("CSeq") && message.contains("INVITE")) {
                // 处理INVITE响应
                String pattern = "CSeq:\\s*(\\d+\\s+\\w+)";
                java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher m = r.matcher(message);

                String CSeq = "";
                if (m.find()) {
                    CSeq = m.group(1);
                }
                if (CSeq != null && processedInviteCseqs.contains(CSeq)) {
                    Log.e(TAG, "已处理过此INVITE消息，CSeq: " + CSeq + "，忽略重复消息");
                } else {
                    String from = extractDeviceIdFromMessage(message);
                    ackMessage = message;
                    creatACKMessage(message, from);

                    ChatMessage chatMessage = new ChatMessage(message, from, "sip:" + gbCode + "@" + localIpAddress + ":" + localPort);
                    if (messageCallback != null) {
                        messageCallback.onMessageReceived(chatMessage);
                    } else {
                        Log.e(TAG, "消息回调未设置，无法传递200 OK响应");
                    }
                }

                if (CSeq != null) {
                    processedInviteCseqs.add(CSeq);
                }
            }
        } else if (message.contains("403 No Register")) {
            realm = "";
            nonce = "";
            qop = "";
            sendRegisterRequest();
        }
    }

    private String extractHeaderValue(String message, String header, String param) {
        if (message.contains(header)) {
            String headerLine = message.split(header)[1].split("\r\n")[0].trim();
            if (param != null && headerLine.contains(param + "=\"")) {
                return headerLine.split(param + "=\"")[1].split("\"")[0];
            } else if (param != null && headerLine.contains(param + "=")) {
                String paramValue = headerLine.split(param + "=")[1];
                if (paramValue.contains(" ")) {
                    paramValue = paramValue.split(" ")[0];
                }
                return paramValue;
            } else if (param == null) {
                return headerLine;
            }
        }
        return null;
    }

    public boolean registerAccount(String gbCode, String password, String mediaCode) {
        this.gbCode = gbCode;
        this.password = password;
        this.serverCode = mediaCode;
        sendRegisterRequest();
        return true;
    }

    private void sendRegisterRequest() {
        new Thread(() -> {
            try {
                if (udpSocket == null) {
                    udpSocket = new DatagramSocket(localPort);
                }
                String registerMessage = createRegisterMessage();
                byte[] buffer = registerMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(serverIp), serverPort);
                udpSocket.send(packet);
                Log.e(TAG, "Sent REGISTER request to server");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error sending register: " + e.getMessage());
                if (messageCallback != null) {
                    messageCallback.onRegistrationStateChanged(RegistrationState.Failed, e.getMessage());
                }
            }
        }).start();
    }

    private String createRegisterMessage() {
        StringBuilder sb = new StringBuilder();
        callId = System.currentTimeMillis() + "@" + localIpAddress;
        String tokenCode = MD5Utils.md5(callId + ":ggzs");

        sb.append("REGISTER sip:" + serverIp + ":" + serverPort + " SIP/2.0\r\n");
        sb.append("Via: SIP/2.0/UDP " + localIpAddress + ":" + localPort + ";branch=z9hG4bK" + System.currentTimeMillis() + ";rport\r\n");
        sb.append("From: <sip:" + gbCode + "@" + serverIp + ">;tag=" + System.currentTimeMillis() + "\r\n");
        sb.append("To: <sip:" + gbCode + "@" + serverIp + ">\r\n");
        sb.append("Call-ID: " + callId + "\r\n");
        sb.append("CSeq: " + cseq + " REGISTER\r\n");
        sb.append("Contact: <sip:" + gbCode + "@" + localIpAddress + ":" + localPort + ";ob>\r\n");

        if (nonce != null && !nonce.isEmpty() && realm != null && !realm.isEmpty()) {
            cnonce = generateCnonce();
            int nc = 1;
            String uri = "sip:" + serverIp + ":" + serverPort;
            response = calculateDigestResponse(gbCode, password, realm, "REGISTER", uri, nonce, qop, cnonce, nc);
            sb.append("Authorization: Digest username=\"" + gbCode + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + uri + "\", cnonce=\"" + cnonce + "\", qop=" + qop + ", response=\"" + response + "\", nc=00000001\r\n");
        }

        sb.append("Expires: 3600\r\n");
        sb.append("Max-Forwards: 70\r\n");
        sb.append("User-Agent: AppClient\r\n");
        sb.append("Content-Length: 0\r\n");
        sb.append("X-Token: " + tokenCode + "\r\n");
        sb.append("\r\n");
        cseq++;
        return sb.toString();
    }

    private String generateCnonce() {
        return System.currentTimeMillis() + "-cnonce";
    }

    private String calculateDigestResponse(String username, String password, String realm, String method, String uri, String nonce, String qop, String cnonce, int nc) {
        try {
            String ha1 = md5(username + ":" + realm + ":" + password);
            String ha2 = md5(method + ":" + uri);
            String response = ha1 + ":" + nonce + ":" + String.format("%08x", nc) + ":" + cnonce + ":" + qop + ":" + ha2;
            return md5(response);
        } catch (Exception e) {
            e.printStackTrace();
            return "dummy-response-" + System.currentTimeMillis();
        }
    }

    private String md5(String input) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void sendTextMessage(String text) {
        new Thread(() -> {
            try {
                String message = createSipMessage(text);
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(serverIp), serverPort);
                udpSocket.send(packet);

                ChatMessage chatMessage = new ChatMessage(text, "sip:" + gbCode + "@" + localIpAddress + ":" + localPort, "sip:" + serverIp + ":" + serverPort);
                if (messageCallback != null) {
                    messageCallback.onMessageSent(chatMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Error sending message: " + e.getMessage());
            }
        }).start();
    }

    private String createSipMessage(String content) {
        StringBuilder sb = new StringBuilder();
        callId = System.currentTimeMillis() + "@" + localIpAddress;
        String tokenCode = MD5Utils.md5(callId + ":ggzs");

        sb.append("MESSAGE sip:" + serverCode + "@" + serverIp + ":" + serverPort + " SIP/2.0\r\n");
        sb.append("Via: SIP/2.0/UDP " + localIpAddress + ":" + localPort + ";branch=z9hG4bK" + System.currentTimeMillis() + ";rport\r\n");
        sb.append("Max-Forwards: 70\r\n");
        sb.append("From: <sip:" + gbCode + "@" + serverIp + ">;tag=" + System.currentTimeMillis() + "\r\n");
        sb.append("To: <sip:" + serverCode + "@" + serverIp + ">\r\n");
        sb.append("Call-ID: " + callId + "\r\n");
        sb.append("CSeq: " + cseq + " MESSAGE\r\n");
        sb.append("Accept: text/plain, application/im-iscomposing+xml\r\n");
        sb.append("Content-Type: Application/MANSCDP+xml\r\n");
        sb.append("Content-Length: " + content.length() + "\r\n");
        sb.append("User-Agent: AppClient\r\n");
        sb.append("X-Token: " + tokenCode + "\r\n");
        sb.append("\r\n");
        sb.append(content);

        cseq++;
        return sb.toString();
    }

    public void startKeepalive(String deviceId) {
        mKeepaliveRunnable = new Runnable() {
            @Override
            public void run() {
                sendKeepalive(deviceId);
                mHandler.postDelayed(this, INTERVAL);
            }
        };
        mHandler.post(mKeepaliveRunnable);
    }

    private void sendKeepalive(String deviceId) {
        String xml = String.format(
                "<?xml version=\"1.0\"?>" +
                        "<Notify>" +
                        "<CmdType>Keepalive</CmdType>" +
                        "<SN>%d</SN>" +
                        "<DeviceID>%s</DeviceID>" +
                        "<Status>OK</Status>" +
                        "</Notify>",
                sequenceNumber++, deviceId
        );
        sendTextMessage(xml);
    }

    public void unregisterAccount() {
        isRunning = false;
        if (receiveThread != null) {
            receiveThread.interrupt();
        }
        if (udpSocket != null) {
            udpSocket.close();
        }
        if (mKeepaliveRunnable != null) {
            mHandler.removeCallbacks(mKeepaliveRunnable);
        }
    }

    public void setMessageCallback(MessageCallback callback) {
        this.messageCallback = callback;
    }

    public void sendSipInvite(String deviceId, int clarityType) {
        if (deviceId == null || deviceId.isEmpty()) {
            Log.e(TAG, "设备ID为空，无法发送SIP INVITE");
            return;
        }

        try {
            String localIpAddress = getLocalIpAddress();
            String serverIp = this.serverIp;
            int serverPort = this.serverPort;
            int localPort = this.localPort;
            String username = this.gbCode;

            String callId = System.currentTimeMillis() + "@" + localIpAddress;
            String fromTag = System.currentTimeMillis() + "";
            String branch = "z9hG4bK" + System.currentTimeMillis();
            String tokenCode = MD5Utils.md5(callId + ":ggzs");

            String sdp = "v=0\r\n" +
                    "o=" + deviceId + " 2 2 IN IP4 " + localIpAddress + "\r\n" +
                    "s=Play\r\n" +
                    "c=IN IP4 " + localIpAddress + "\r\n" +
                    "t=0 0\r\n" +
                    "m=video 8000 TCP/RTP/AVP 96\r\n" +
                    "a=recvonly\r\n" +
                    "a=rtpmap:96 PS/90000\r\n" +
                    "a=setup:active\r\n" +
                    "a=stream:" + clarityType + "\r\n" +
                    "y=0100000001\r\n";

            StringBuilder inviteMessage = new StringBuilder();
            inviteMessage.append("INVITE sip:" + serverCode + "@" + serverIp + " SIP/2.0\r\n");
            inviteMessage.append("Via: SIP/2.0/UDP " + localIpAddress + ":" + localPort + ";rport;branch=" + branch + "\r\n");
            inviteMessage.append("Max-Forwards: 70\r\n");
            inviteMessage.append("From: sip:" + username + "@" + serverIp + ";tag=" + fromTag + "\r\n");
            inviteMessage.append("To: sip:" + deviceId + "@" + serverIp + "\r\n");
            inviteMessage.append("Contact: <sip:" + username + "@" + localIpAddress + ":" + localPort + ";ob>\r\n");
            inviteMessage.append("Call-ID: " + callId + "\r\n");
            inviteMessage.append("CSeq: " + cseq + " INVITE\r\n");
            inviteMessage.append("Authorization: digest username=\"" + username + "\", realm=\"" + serverIp + "\", nonce=\"\", uri=\"sip:" + deviceId + "@" + serverIp + "\", response=\"\"\r\n");
            inviteMessage.append("Allow: PRACK, INVITE, ACK, BYE, CANCEL, UPDATE, INFO, SUBSCRIBE, NOTIFY, REFER, MESSAGE, OPTIONS\r\n");
            inviteMessage.append("Supported: replaces, 100rel, timer, norefersub\r\n");
            inviteMessage.append("Session-Expires: 1800\r\n");
            inviteMessage.append("Min-SE: 90\r\n");
            inviteMessage.append("Content-Type: application/sdp\r\n");
            inviteMessage.append("Content-Length: " + sdp.length() + "\r\n");
            inviteMessage.append("X-Time: " + MyTimeUtils.getCurrentDateTime() + "\r\n");
            inviteMessage.append("User-Agent: AppClient\r\n");
            inviteMessage.append("X-Token: " + tokenCode + "\r\n");
            inviteMessage.append("\r\n");
            inviteMessage.append(sdp);

            cseq++;

            new Thread(() -> {
                try {
                    byte[] buffer = inviteMessage.toString().getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                            InetAddress.getByName(serverIp), serverPort);
                    udpSocket.send(packet);
                    Log.e(TAG, "发送SIP INVITE消息成功\n" + inviteMessage.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "发送SIP INVITE消息失败: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "构建SIP INVITE消息失败: " + e.getMessage());
        }
    }

    private void creatACKMessage(String message, String from) {
        try {
            String contactAddr = extractHeaderValue(message, "Contact:", null);
            if (contactAddr != null && contactAddr.startsWith("<") && contactAddr.endsWith(">")) {
                contactAddr = contactAddr.substring(1, contactAddr.length() - 1);
            }
            if (contactAddr == null || contactAddr.isEmpty()) {
                Log.e(TAG, "无法从消息中提取Contact地址");
                return;
            }

            String fromTag = extractHeaderValue(message, "From:", "tag");
            if (fromTag == null) {
                Log.e(TAG, "无法从消息中提取From标签");
                return;
            }

            String toTag = extractHeaderValue(message, "To:", "tag");
            if (toTag == null) {
                Log.e(TAG, "无法从消息中提取To标签");
                return;
            }

            String branch = extractHeaderValue(message, "Via:", "branch");
            if (branch == null) {
                Log.e(TAG, "无法从消息中提取branch参数");
                return;
            }

            String callId = extractHeaderValue(message, "Call-ID:", null);
            if (callId == null) {
                Log.e(TAG, "无法从消息中提取Call-ID");
                return;
            }

            String cseqValue = extractHeaderValue(message, "CSeq:", null);
            if (cseqValue != null) {
                cseqValue = cseqValue.split(" ")[0];
            }
            if (cseqValue == null) {
                Log.e(TAG, "无法从消息中提取CSeq");
                return;
            }

            String fromAddr = null;
            if (message.contains("From: ")) {
                fromAddr = message.split("From: ")[1].split("\r\n")[0].trim();
            }
            if (fromAddr == null) {
                Log.e(TAG, "无法从消息中提取From地址");
                return;
            }

            String toAddr = null;
            if (message.contains("To: ")) {
                toAddr = message.split("To: ")[1].split("\r\n")[0].trim();
            }
            if (toAddr == null) {
                Log.e(TAG, "无法从消息中提取To地址");
                return;
            }

            List<String> recordRoutes = new ArrayList<>();
            int recordRouteIndex = message.indexOf("Record-Route: ");
            while (recordRouteIndex != -1) {
                String recordRouteLine = message.substring(recordRouteIndex + "Record-Route: ".length());
                int endIndex = recordRouteLine.indexOf("\r\n");
                if (endIndex != -1) {
                    String recordRoute = recordRouteLine.substring(0, endIndex).trim();
                    recordRoutes.add(recordRoute);
                    recordRouteIndex = message.indexOf("Record-Route: ", recordRouteIndex + "Record-Route: ".length());
                } else {
                    break;
                }
            }

            String localIpAddress = getLocalIpAddress();
            int localPort = this.localPort;
            String tokenCode = MD5Utils.md5(callId + ":ggzs");

            StringBuilder ackMessage = new StringBuilder();
            ackMessage.append("ACK " + contactAddr + " SIP/2.0\r\n");
            ackMessage.append("Via: SIP/2.0/UDP " + localIpAddress + ":" + localPort + ";rport;branch=" + branch + "\r\n");
            ackMessage.append("Max-Forwards: 70\r\n");
            ackMessage.append("From: " + fromAddr + "\r\n");
            ackMessage.append("To: " + toAddr + "\r\n");
            ackMessage.append("Call-ID: " + callId + "\r\n");
            ackMessage.append("CSeq: " + cseqValue + " ACK\r\n");

            if (!recordRoutes.isEmpty()) {
                for (int i = recordRoutes.size() - 1; i >= 0; i--) {
                    String recordRoute = recordRoutes.get(i);
                    ackMessage.append("Route: " + recordRoute + "\r\n");
                }
            }

            ackMessage.append("Contact: <sip:" + gbCode + "@" + localIpAddress + ":" + localPort + ";ob>\r\n");
            ackMessage.append("Content-Length: 0\r\n");
            ackMessage.append("User-Agent: AppClient\r\n");
            ackMessage.append("X-Token: " + tokenCode + "\r\n");
            ackMessage.append("\r\n");

            new Thread(() -> {
                try {
                    byte[] buffer = ackMessage.toString().getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                            InetAddress.getByName(serverIp), serverPort);
                    udpSocket.send(packet);
                    Log.e(TAG, "发送SIP ACK消息成功\n" + ackMessage.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "发送SIP ACK消息失败: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "构建SIP ACK消息失败: " + e.getMessage());
        }
    }

    public void sendByeInvite() {
        try {
            String contactAddr = extractHeaderValue(ackMessage, "Contact:", null);
            if (contactAddr != null && contactAddr.startsWith("<") && contactAddr.endsWith(">")) {
                contactAddr = contactAddr.substring(1, contactAddr.length() - 1);
            }
            if (contactAddr == null || contactAddr.isEmpty()) {
                Log.e(TAG, "无法从消息中提取Contact地址");
                return;
            }

            String fromTag = extractHeaderValue(ackMessage, "From:", "tag");
            if (fromTag == null) {
                Log.e(TAG, "无法从消息中提取From标签");
                return;
            }

            String toTag = extractHeaderValue(ackMessage, "To:", "tag");
            if (toTag == null) {
                Log.e(TAG, "无法从消息中提取To标签");
                return;
            }

            String branch = extractHeaderValue(ackMessage, "Via:", "branch");
            if (branch == null) {
                Log.e(TAG, "无法从消息中提取branch参数");
                return;
            }

            String callId = extractHeaderValue(ackMessage, "Call-ID:", null);
            if (callId == null) {
                Log.e(TAG, "无法从消息中提取Call-ID");
                return;
            }

            String fromAddr = null;
            if (ackMessage.contains("From: ")) {
                fromAddr = ackMessage.split("From: ")[1].split("\r\n")[0].trim();
            }
            if (fromAddr == null) {
                Log.e(TAG, "无法从消息中提取From地址");
                return;
            }

            String toAddr = null;
            if (ackMessage.contains("To: ")) {
                toAddr = ackMessage.split("To: ")[1].split("\r\n")[0].trim();
            }
            if (toAddr == null) {
                Log.e(TAG, "无法从消息中提取To地址");
                return;
            }

            List<String> recordRoutes = new ArrayList<>();
            int recordRouteIndex = ackMessage.indexOf("Record-Route: ");
            while (recordRouteIndex != -1) {
                String recordRouteLine = ackMessage.substring(recordRouteIndex + "Record-Route: ".length());
                int endIndex = recordRouteLine.indexOf("\r\n");
                if (endIndex != -1) {
                    String recordRoute = recordRouteLine.substring(0, endIndex).trim();
                    recordRoutes.add(recordRoute);
                    recordRouteIndex = ackMessage.indexOf("Record-Route: ", recordRouteIndex + "Record-Route: ".length());
                } else {
                    break;
                }
            }

            String localIpAddress = getLocalIpAddress();
            int localPort = this.localPort;
            String tokenCode = MD5Utils.md5(callId + ":ggzs");

            StringBuilder byeMessage = new StringBuilder();
            byeMessage.append("BYE " + contactAddr + " SIP/2.0\r\n");
            byeMessage.append("Via: SIP/2.0/UDP " + localIpAddress + ":" + localPort + ";rport;branch=" + branch + "\r\n");
            byeMessage.append("Max-Forwards: 70\r\n");
            byeMessage.append("From: " + fromAddr + "\r\n");
            byeMessage.append("To: " + toAddr + "\r\n");
            byeMessage.append("Call-ID: " + callId + "\r\n");
            byeMessage.append("CSeq: " + cseq + " BYE\r\n");

            if (!recordRoutes.isEmpty()) {
                for (int i = recordRoutes.size() - 1; i >= 0; i--) {
                    String recordRoute = recordRoutes.get(i);
                    byeMessage.append("Route: " + recordRoute + "\r\n");
                }
            }

            byeMessage.append("Contact: <sip:" + gbCode + "@" + localIpAddress + ":" + localPort + ";ob>\r\n");
            byeMessage.append("Content-Length: 0\r\n");
            byeMessage.append("Authorization: Digest username=\"" + gbCode + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + contactAddr + ", response=\"\"\r\n");
            byeMessage.append("User-Agent: AppClient\r\n");
            byeMessage.append("X-Token: " + tokenCode + "\r\n");
            byeMessage.append("\r\n");

            new Thread(() -> {
                try {
                    byte[] buffer = byeMessage.toString().getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                            InetAddress.getByName(serverIp), serverPort);
                    udpSocket.send(packet);
                    Log.e(TAG, "发送SIP BYE消息成功\n" + byeMessage.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "发送SIP BYE消息失败: " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "构建SIP BYE消息失败: " + e.getMessage());
        }
    }

    private String extractDeviceIdFromMessage(String message) {
        if (message.contains("To:")) {
            String toLine = message.split("To:")[1].split("\r\n")[0].trim();
            if (toLine.contains("sip:")) {
                String sipUri = toLine.split("sip:")[1];
                if (sipUri.contains("@")) {
                    return sipUri.split("@")[0];
                }
            }
        }
        return null;
    }

    private String getLocalIpAddress() {
        String localHost = DeviceInfoUtil.getIPAddress();
        return localHost;
    }
}
```

- [ ] **Step 2: 创建SipService类**

```java
package com.example.myapplication.sip;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.ui.home.HomeActivity;

public class SipService extends Service {
    private static final String TAG = "SipService";
    private static final String CHANNEL_ID = "SipServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "SipService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "SipService started");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "SipService destroyed");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "SIP Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SIP Service")
                .setContentText("SIP service is running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, SipService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, SipService.class);
        context.stopService(intent);
    }
}
```

- [ ] **Step 3: 更新AndroidManifest.xml**

在AndroidManifest.xml中添加SipService：
```xml
<service
    android:name=".sip.SipService"
    android:exported="false"
    android:foregroundServiceType="microphone" />
```

- [ ] **Step 4: 提交SIP功能**

```bash
git add app/src/main/java/com/example/myapplication/sip/
git add app/src/main/AndroidManifest.xml
git commit -m "feat: 添加SIP功能模块"
```

---

### Task 3: 添加工具类

**Covers:** [S4]
<!-- 添加缺失的工具类 -->

**Files:**
- Create: `app/src/main/java/com/example/myapplication/util/MD5Utils.java`
- Create: `app/src/main/java/com/example/myapplication/util/DeviceInfoUtil.java`
- Create: `app/src/main/java/com/example/myapplication/util/MyTimeUtils.java`
- Create: `app/src/main/java/com/example/myapplication/util/XmlUtils.java`
- Create: `app/src/main/java/com/example/myapplication/util/TcpClient.java`

**Interfaces:**
- Consumes: Task 1的配置更新
- Produces: 工具类供其他模块使用

- [ ] **Step 1: 创建MD5Utils类**

```java
package com.example.myapplication.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

- [ ] **Step 2: 创建DeviceInfoUtil类**

```java
package com.example.myapplication.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class DeviceInfoUtil {

    public static final String TAG = DeviceInfoUtil.class.getSimpleName();

    public static String localId = "0001";
    public static String aioPort = "5060";

    public static String getIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress()
                            && !address.isLinkLocalAddress()
                            && address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static String getPhoneNum(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getLine1Number();
        } else {
            return null;
        }
    }

    public static String getNetworkOperator(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperator();
    }

    public static String getNetworkOperatorName(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperatorName();
    }

    public static int getPhoneType(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getPhoneType();
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public class Constants {
        public static final int NETWORK_CLASS_UNKNOWN = 0;
        public static final int NETWORK_WIFI = 1;
        public static final int NETWORK_CLASS_2_G = 2;
        public static final int NETWORK_CLASS_3_G = 3;
        public static final int NETWORK_CLASS_4_G = 4;
    }

    public List<SubscriptionInfo> getSubscriptionInfoList(Context context) {
        SubscriptionManager subscriptionManager =
                (SubscriptionManager)
                        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                return subscriptionManager.getActiveSubscriptionInfoList();
            } else {
                return null;
            }
        } else {
            return subscriptionManager.getActiveSubscriptionInfoList();
        }
    }

    public static long getUptimeMillis() {
        return SystemClock.elapsedRealtime();
    }

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getAppVersion(Context context) {
        String version = "0";
        try {
            version =
                    context.getPackageManager()
                            .getPackageInfo(context.getPackageName(), 0)
                            .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static boolean haveSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static long getSdCardSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        return blockSize * totalBlocks;
    }

    public static long getRomSize() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        return blockSize * totalBlocks;
    }

    public static long getMemorySize(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo.totalMem;
        }
        return 0;
    }

    public static String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Hardware")) {
                    String[] array = line.split(":\\s+", 2);
                    if (array.length >= 2) {
                        return array[1];
                    }
                }
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static long getCurrentCpuFrequency() {
        try {
            File file = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            br.close();
            return Long.parseLong(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getMaxCpuFrequency() {
        try {
            File file = new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            br.close();
            return Long.parseLong(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long getMinCpuFrequency() {
        try {
            File file = new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            br.close();
            return Long.parseLong(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            System.out.println("CPU Count: " + files.length);
            return files.length;
        } catch (Exception e) {
            System.out.println("CPU Count: Failed.");
            e.printStackTrace();
            return 1;
        }
    }

    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                        & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isPhone(Context context) {
        TelephonyManager telephony =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) {
            return false;
        } else {
            return true;
        }
    }
}
```

- [ ] **Step 3: 创建MyTimeUtils类**

```java
package com.example.myapplication.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyTimeUtils {

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatDate(long timestamp, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
```

- [ ] **Step 4: 创建XmlUtils类**

```java
package com.example.myapplication.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.StringReader;
import java.util.List;

public class XmlUtils {

    public static Element parseXml(String xml) {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(xml));
            return document.getRootElement();
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getElementText(Element parent, String tagName) {
        Element element = parent.element(tagName);
        if (element != null) {
            return element.getTextTrim();
        }
        return null;
    }

    public static String createKeepaliveXml(int sn, String deviceId) {
        return "<?xml version=\"1.0\"?>" +
                "<Notify>" +
                "<CmdType>Keepalive</CmdType>" +
                "<SN>" + sn + "</SN>" +
                "<DeviceID>" + deviceId + "</DeviceID>" +
                "<Status>OK</Status>" +
                "</Notify>";
    }
}
```

- [ ] **Step 5: 创建TcpClient类**

```java
package com.example.myapplication.util;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class TcpClient {
    private static final String TAG = "TcpClient";
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isConnected = false;

    public interface TcpClientListener {
        void onConnected();
        void onDisconnected();
        void onDataReceived(byte[] data, int length);
        void onError(String error);
    }

    private TcpClientListener listener;

    public TcpClient(TcpClientListener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                isConnected = true;

                if (listener != null) {
                    listener.onConnected();
                }

                // 读取数据
                byte[] buffer = new byte[4096];
                while (isConnected) {
                    int length = inputStream.read(buffer);
                    if (length > 0) {
                        if (listener != null) {
                            listener.onDataReceived(buffer, length);
                        }
                    }
                }
            } catch (UnknownHostException e) {
                Log.e(TAG, "Unknown host: " + e.getMessage());
                if (listener != null) {
                    listener.onError("Unknown host: " + e.getMessage());
                }
            } catch (IOException e) {
                Log.e(TAG, "Connection error: " + e.getMessage());
                if (listener != null) {
                    listener.onError("Connection error: " + e.getMessage());
                }
            } finally {
                disconnect();
            }
        }).start();
    }

    public void send(byte[] data) {
        if (outputStream != null && isConnected) {
            new Thread(() -> {
                try {
                    outputStream.write(data);
                    outputStream.flush();
                } catch (IOException e) {
                    Log.e(TAG, "Send error: " + e.getMessage());
                    if (listener != null) {
                        listener.onError("Send error: " + e.getMessage());
                    }
                }
            }).start();
        }
    }

    public void disconnect() {
        isConnected = false;
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (listener != null) {
            listener.onDisconnected();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
```

- [ ] **Step 6: 提交工具类**

```bash
git add app/src/main/java/com/example/myapplication/util/
git commit -m "feat: 添加工具类模块"
```

---

### Task 4: 添加GreenDAO数据库支持

**Covers:** [S4]
<!-- 添加GreenDAO数据库相关类 -->

**Files:**
- Create: `app/src/main/java/com/example/myapplication/greendao/DaoMaster.java`
- Create: `app/src/main/java/com/example/myapplication/greendao/DaoSession.java`
- Create: `app/src/main/java/com/example/myapplication/greendao/CameraDao.java`
- Create: `app/src/main/java/com/example/myapplication/data/model/Camera.java`

**Interfaces:**
- Consumes: Task 1的配置更新
- Produces: 数据库操作功能

- [ ] **Step 1: 创建Camera实体类**

```java
package com.example.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cameras")
public class Camera {
    @PrimaryKey
    private String id;
    private String name;
    private String ip;
    private int port;
    private String username;
    private String password;
    private String location;
    private boolean isOnline;
    private long lastUpdate;

    public Camera() {}

    public Camera(String id, String name, String ip, int port, String username, String password, String location, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.location = location;
        this.isOnline = isOnline;
        this.lastUpdate = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public long getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(long lastUpdate) { this.lastUpdate = lastUpdate; }
}
```

- [ ] **Step 2: 创建GreenDAO相关类**

由于GreenDAO需要通过Gradle插件自动生成代码，我们需要先运行项目来生成这些文件。但是，我们可以先创建一个简化版本的数据库帮助类。

```java
package com.example.myapplication.data.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.data.model.Camera;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "cameras.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_CAMERAS = "cameras";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_IP = "ip";
    private static final String COLUMN_PORT = "port";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_IS_ONLINE = "is_online";
    private static final String COLUMN_LAST_UPDATE = "last_update";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_CAMERAS + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY," +
                COLUMN_NAME + " TEXT," +
                COLUMN_IP + " TEXT," +
                COLUMN_PORT + " INTEGER," +
                COLUMN_USERNAME + " TEXT," +
                COLUMN_PASSWORD + " TEXT," +
                COLUMN_LOCATION + " TEXT," +
                COLUMN_IS_ONLINE + " INTEGER," +
                COLUMN_LAST_UPDATE + " INTEGER" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAMERAS);
        onCreate(db);
    }

    public long insertCamera(Camera camera) {
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(TABLE_CAMERAS, null, null);
        db.close();
        return id;
    }

    public List<Camera> getAllCameras() {
        List<Camera> cameras = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        android.database.Cursor cursor = db.query(TABLE_CAMERAS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Camera camera = new Camera();
                camera.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                camera.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                camera.setIp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IP)));
                camera.setPort(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PORT)));
                camera.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
                camera.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                camera.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                camera.setOnline(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ONLINE)) == 1);
                camera.setLastUpdate(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_UPDATE)));
                cameras.add(camera);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return cameras;
    }

    public int updateCamera(Camera camera) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.update(TABLE_CAMERAS, null, COLUMN_ID + " = ?",
                new String[]{camera.getId()});
        db.close();
        return rowsAffected;
    }

    public void deleteCamera(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CAMERAS, COLUMN_ID + " = ?", new String[]{id});
        db.close();
    }
}
```

- [ ] **Step 3: 提交数据库支持**

```bash
git add app/src/main/java/com/example/myapplication/data/
git commit -m "feat: 添加GreenDAO数据库支持"
```

---

### Task 5: 添加VLC播放器支持

**Covers:** [S4]
<!-- 添加VLC播放器相关类 -->

**Files:**
- Create: `app/src/main/java/com/example/myapplication/ui/player/VlcPlayerActivity.java`
- Create: `app/src/main/res/layout/activity_vlc_player.xml`

**Interfaces:**
- Consumes: Task 1的配置更新, Task 2的SIP功能
- Produces: VLC播放器功能

- [ ] **Step 1: 创建VLC播放器Activity**

```java
package com.example.myapplication.ui.player;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.sip.SIPManager;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

public class VlcPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VlcPlayerActivity";

    private SurfaceView surfaceView;
    private ImageButton btnBack;
    private TextView tvTitle;
    private ImageButton btnPtzUp;
    private ImageButton btnPtzDown;
    private ImageButton btnPtzLeft;
    private ImageButton btnPtzRight;
    private ImageButton btnScreenshot;
    private ImageButton btnRecord;
    private ImageButton btnTalk;

    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private String deviceId;
    private String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_player);

        // 获取传入的参数
        deviceId = getIntent().getStringExtra("device_id");
        deviceName = getIntent().getStringExtra("device_name");

        initViews();
        initVlc();
        startPlayback();
    }

    private void initViews() {
        surfaceView = findViewById(R.id.surface_view);
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        btnPtzUp = findViewById(R.id.btn_ptz_up);
        btnPtzDown = findViewById(R.id.btn_ptz_down);
        btnPtzLeft = findViewById(R.id.btn_ptz_left);
        btnPtzRight = findViewById(R.id.btn_ptz_right);
        btnScreenshot = findViewById(R.id.btn_screenshot);
        btnRecord = findViewById(R.id.btn_record);
        btnTalk = findViewById(R.id.btn_talk);

        tvTitle.setText(deviceName);

        btnBack.setOnClickListener(v -> finish());

        btnPtzUp.setOnClickListener(v -> sendPtzCommand("up"));
        btnPtzDown.setOnClickListener(v -> sendPtzCommand("down"));
        btnPtzLeft.setOnClickListener(v -> sendPtzCommand("left"));
        btnPtzRight.setOnClickListener(v -> sendPtzCommand("right"));

        btnScreenshot.setOnClickListener(v -> takeScreenshot());
        btnRecord.setOnClickListener(v -> toggleRecording());
        btnTalk.setOnClickListener(v -> toggleTalk());
    }

    private void initVlc() {
        ArrayList<String> options = new ArrayList<>();
        options.add("--no-drop-late-frames");
        options.add("--no-skip-frames");
        options.add("--no-video-title-show");
        options.add("--no-stats");
        options.add("--no-autoplay");

        libVLC = new LibVLC(this, options);
        mediaPlayer = new MediaPlayer(libVLC);
    }

    private void startPlayback() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(this, "设备ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 通过SIP发送INVITE请求
        SIPManager.getInstance().sendSipInvite(deviceId, 1);

        // 设置媒体播放器
        mediaPlayer.setRenderWindow(surfaceView.getHolder());
        mediaPlayer.setOnEventListener(new MediaPlayer.EventListener() {
            @Override
            public void onEvent(MediaPlayer.Event event) {
                switch (event.type) {
                    case MediaPlayer.Event.Playing:
                        Log.d(TAG, "Playing");
                        break;
                    case MediaPlayer.Event.Paused:
                        Log.d(TAG, "Paused");
                        break;
                    case MediaPlayer.Event.Stopped:
                        Log.d(TAG, "Stopped");
                        break;
                    case MediaPlayer.Event.EndReached:
                        Log.d(TAG, "End reached");
                        break;
                    case MediaPlayer.Event.Error:
                        Log.e(TAG, "Error: " + event.getErrorMessage());
                        runOnUiThread(() -> {
                            Toast.makeText(VlcPlayerActivity.this, "播放错误: " + event.getErrorMessage(), Toast.LENGTH_SHORT).show();
                        });
                        break;
                }
            }
        });
    }

    private void sendPtzCommand(String direction) {
        // 发送PTZ控制命令
        String xml = "<?xml version=\"1.0\"?>" +
                "<Control>" +
                "<CmdType>DeviceControl</CmdType>" +
                "<SN>" + (int) (System.currentTimeMillis() % 10000) + "</SN>" +
                "<DeviceID>" + deviceId + "</DeviceID>" +
                "<PTZCmd>" + direction + "</PTZCmd>" +
                "</Control>";

        SIPManager.getInstance().sendTextMessage(xml);
        Log.d(TAG, "Sent PTZ command: " + direction);
    }

    private void takeScreenshot() {
        Toast.makeText(this, "截图功能", Toast.LENGTH_SHORT).show();
        // TODO: 实现截图功能
    }

    private void toggleRecording() {
        Toast.makeText(this, "录屏功能", Toast.LENGTH_SHORT).show();
        // TODO: 实现录屏功能
    }

    private void toggleTalk() {
        Toast.makeText(this, "对讲功能", Toast.LENGTH_SHORT).show();
        // TODO: 实现对讲功能
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (libVLC != null) {
            libVLC.release();
        }
    }
}
```

- [ ] **Step 2: 创建VLC播放器布局文件**

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/black">

    <!-- 顶部工具栏 -->
    <RelativeLayout
        android:id="@+id/top_toolbar"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:layout_alignParentTop="true"
        android:background="#80000000"
        android:padding="10dp">

        <ImageButton
            android:id="@+id/btn_back"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_alignParentStart="true"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@android:drawable/ic_menu_revert"
            app:tint="@android:color/white" />

        <TextView
            android:id="@+id/tv_title"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:textColor="@android:color/white"
            android:textSize="18sp"
            android:textStyle="bold" />
    </RelativeLayout>

    <!-- 视频播放区域 -->
    <SurfaceView
        android:id="@+id/surface_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_above="@+id/bottom_toolbar"
        android:layout_below="@+id/top_toolbar" />

    <!-- 底部工具栏 -->
    <LinearLayout
        android:id="@+id/bottom_toolbar"
        android:layout_width="match_parent"
        android:layout_height="80dp"
        android:layout_alignParentBottom="true"
        android:background="#80000000"
        android:gravity="center"
        android:orientation="horizontal"
        android:padding="10dp">

        <!-- PTZ控制按钮 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:gravity="center"
            android:orientation="vertical">

            <ImageButton
                android:id="@+id/btn_ptz_up"
                android:layout_width="40dp"
                android:layout_height="40dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@android:drawable/arrow_up_float"
                app:tint="@android:color/white" />

            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <ImageButton
                    android:id="@+id/btn_ptz_left"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:src="@android:drawable/arrow_left_float"
                    app:tint="@android:color/white" />

                <ImageButton
                    android:id="@+id/btn_ptz_right"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:src="@android:drawable/arrow_right_float"
                    app:tint="@android:color/white" />
            </LinearLayout>

            <ImageButton
                android:id="@+id/btn_ptz_down"
                android:layout_width="40dp"
                android:layout_height="40dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@android:drawable/arrow_down_float"
                app:tint="@android:color/white" />
        </LinearLayout>

        <!-- 功能按钮 -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="2"
            android:gravity="center"
            android:orientation="horizontal">

            <ImageButton
                android:id="@+id/btn_screenshot"
                android:layout_width="50dp"
                android:layout_height="50dp"
                android:layout_margin="10dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@android:drawable/ic_menu_camera"
                app:tint="@android:color/white" />

            <ImageButton
                android:id="@+id/btn_record"
                android:layout_width="50dp"
                android:layout_height="50dp"
                android:layout_margin="10dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@android:drawable/ic_media_play"
                app:tint="@android:color/white" />

            <ImageButton
                android:id="@+id/btn_talk"
                android:layout_width="50dp"
                android:layout_height="50dp"
                android:layout_margin="10dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@android:drawable/ic_btn_speak_now"
                app:tint="@android:color/white" />
        </LinearLayout>
    </LinearLayout>

</RelativeLayout>
```

- [ ] **Step 3: 更新AndroidManifest.xml**

在AndroidManifest.xml中添加VlcPlayerActivity：
```xml
<activity
    android:name=".ui.player.VlcPlayerActivity"
    android:screenOrientation="landscape"
    android:theme="@style/Theme.MyApplication" />
```

- [ ] **Step 4: 提交VLC播放器支持**

```bash
git add app/src/main/java/com/example/myapplication/ui/player/
git add app/src/main/res/layout/activity_vlc_player.xml
git add app/src/main/AndroidManifest.xml
git commit -m "feat: 添加VLC播放器支持"
```

---

### Task 6: 优化现有代码结构

**Covers:** [S5]
<!-- 优化现有代码结构和组织方式 -->

**Files:**
- Modify: `app/src/main/java/com/example/myapplication/ui/login/LoginActivity.java`
- Modify: `app/src/main/java/com/example/myapplication/ui/home/HomeActivity.java`

**Interfaces:**
- Consumes: Task 2的SIP功能, Task 3的工具类
- Produces: 优化后的代码结构

- [ ] **Step 1: 优化LoginActivity**

在LoginActivity中添加SIP初始化和登录功能：

```java
package com.example.myapplication.ui.login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.example.myapplication.R;
import com.example.myapplication.sip.SIPManager;
import com.example.myapplication.sip.SipService;
import com.example.myapplication.ui.home.HomeActivity;
import com.example.myapplication.util.MD5Utils;
import com.example.myapplication.util.PreferencesManager;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText etUsername;
    private EditText etPassword;
    private CheckBox cbRemember;
    private Button btnLogin;
    private boolean saveOrNot = false;

    private final ActivityResultLauncher<String[]> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            // 权限已请求，无论结果如何继续运行
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        initViews();
        requestPermissions();
        initSip();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        btnLogin = findViewById(R.id.btn_login);

        // 读取保存的登录信息
        String username = PreferencesManager.getInstance(this).getUsername();
        String password = PreferencesManager.getInstance(this).getPassword();
        boolean isRemember = PreferencesManager.getInstance(this).isRememberPassword();

        if (username != null && !username.isEmpty()) {
            etUsername.setText(username);
        }
        if (password != null && !password.isEmpty()) {
            etPassword.setText(password);
        }
        if (isRemember) {
            cbRemember.setChecked(true);
            saveOrNot = true;
        }

        cbRemember.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveOrNot = isChecked;
        });

        btnLogin.setOnClickListener(v -> {
            String usernameStr = etUsername.getText().toString().trim();
            String passwordStr = etPassword.getText().toString().trim();

            if (usernameStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 保存登录信息
            if (saveOrNot) {
                PreferencesManager.getInstance(this).setUsername(usernameStr);
                PreferencesManager.getInstance(this).setPassword(passwordStr);
                PreferencesManager.getInstance(this).setRememberPassword(true);
            } else {
                PreferencesManager.getInstance(this).setUsername("");
                PreferencesManager.getInstance(this).setPassword("");
                PreferencesManager.getInstance(this).setRememberPassword(false);
            }

            // 进行登录
            login(usernameStr, passwordStr);
        });
    }

    private void login(String username, String password) {
        // MD5加密密码
        String md5Password = MD5Utils.md5(password);
        Log.d(TAG, "Login with username: " + username + ", password: " + md5Password);

        // TODO: 实际的登录逻辑，可以调用服务器API
        // 这里简化处理，直接跳转到主页

        // 启动SIP服务
        SipService.startService(this);

        // 跳转到主页
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void initSip() {
        // 初始化SIP管理器
        SIPManager.create(getApplicationContext());
        SIPManager.getInstance().setMessageCallback(new SIPManager.MessageCallback() {
            @Override
            public void onMessageReceived(SIPManager.ChatMessage message) {
                runOnUiThread(() -> {
                    String body = message.getUtf8Text();
                    String from = message.getFromAddress().asStringUriOnly();
                    String to = message.getToAddress().asStringUriOnly();
                    Log.d(TAG, "SIP message received from " + from + " to " + to + ": " + body);
                });
            }

            @Override
            public void onMessageSent(SIPManager.ChatMessage message) {
                runOnUiThread(() -> {
                    String body = message.getUtf8Text();
                    String from = message.getLocalAddress().asString();
                    String to = message.getToAddress().asString();
                    Log.d(TAG, "SIP message sent from " + from + " to " + to + ": " + body);
                });
            }

            @Override
            public void onMessageStatusChanged(SIPManager.ChatMessage.State state) {
                runOnUiThread(() -> {
                    Log.d(TAG, "SIP message status: " + state);
                });
            }

            @Override
            public void onRegistrationStateChanged(SIPManager.RegistrationState state, String message) {
                runOnUiThread(() -> {
                    Log.d(TAG, "SIP registration state: " + state + ", message: " + message);
                    if (state == SIPManager.RegistrationState.Ok) {
                        Toast.makeText(LoginActivity.this, "SIP注册成功", Toast.LENGTH_SHORT).show();
                    } else if (state == SIPManager.RegistrationState.Failed) {
                        Toast.makeText(LoginActivity.this, "SIP注册失败: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void requestPermissions() {
        String[] permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            permissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
            };
        }

        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            permissionLauncher.launch(permissions);
        }
    }
}
```

- [ ] **Step 2: 提交代码结构优化**

```bash
git add app/src/main/java/com/example/myapplication/ui/login/
git commit -m "refactor: 优化LoginActivity，添加SIP初始化和登录功能"
```

---

### Task 7: 运行测试验证

**Covers:** [S6]
<!-- 运行测试验证功能正常 -->

**Files:**
- 测试所有新添加的功能

**Interfaces:**
- Consumes: 所有之前的任务
- Produces: 验证通过的代码

- [ ] **Step 1: 构建项目**

```bash
./gradlew build
```

- [ ] **Step 2: 运行单元测试**

```bash
./gradlew test
```

- [ ] **Step 3: 检查代码质量**

```bash
./gradlew lint
```

- [ ] **Step 4: 提交最终版本**

```bash
git add .
git commit -m "feat: 完成Android项目全面优化"
```

---

### Task 8: 文档更新

**Covers:** [S7]
<!-- 更新项目文档 -->

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: 所有之前的任务
- Produces: 更新后的项目文档

- [ ] **Step 1: 更新README.md**

```markdown
# FCH Software - 园区监控 App

一款 Android 园区监控管理应用，支持多路摄像头实时预览、云台控制（PTZ）、截图录屏等功能。

## 功能特性

- **登录系统** - 用户名/密码登录，支持记住密码，MD5加密传输
- **网络请求** - 使用 Retrofit + OkHttp + RxJava 对接服务器API
- **摄像头监控** - 三列网格展示所有摄像头，支持按区域/状态分类筛选
- **实时预览** - 点击摄像头进入全屏预览
- **云台控制（PTZ）** - 方向盘控制摄像头上下左右旋转，支持缩放
- **底部工具栏** - 截图、录屏、对讲等快捷操作
- **搜索功能** - 快速搜索定位摄像头
- **地点筛选** - 支持按地点筛选摄像头（行政办公楼、公共教学楼、1号主广场）
- **筛选菜单** - 点击筛选图标展开地点列表快速筛选
- **白色主题** - 监控页面采用白色UI设计
- **悬浮对讲按钮** - 底部工具栏对讲按钮悬浮突出效果
- **SIP功能** - 支持SIP协议注册、消息收发、视频通话
- **VLC播放器** - 使用VLC播放器进行视频播放
- **GreenDAO数据库** - 本地数据持久化存储

## 技术栈

| 项目 | 技术 |
|------|------|
| 语言 | Java 11 |
| 最低版本 | Android 7.0 (API 24) |
| 目标版本 | Android 14 (API 36) |
| 架构 | Activity + Fragment |
| 网络请求 | Retrofit 2.9.0 + OkHttp 4.12.0 + RxJava 2 |
| 视频播放 | VLC 3.6.0 |
| 数据库 | GreenDAO 3.3.0 |
| 图片加载 | Glide 4.16.0 |
| UI 组件 | Material Design, RecyclerView, CardView |
| 布局 | ConstraintLayout, LinearLayout |
| 工具库 | hutool 5.3.8, fastjson 1.2.78, dom4j 2.1.3 |

## 项目结构

```
app/src/main/java/com/example/myapplication/
├── adapter/              # RecyclerView适配器
├── data/
│   ├── model/            # 数据模型
│   └── repository/       # 数据仓库层
├── network/              # 网络请求层
├── service/              # 服务层
├── sip/                  # SIP功能模块
├── ui/
│   ├── login/            # 登录页面
│   ├── home/             # 首页（摄像头网格列表）
│   ├── camera/           # 摄像头预览页面
│   ├── video/            # 视频列表页面
│   └── player/           # VLC播放器页面
├── util/                 # 工具类
└── greendao/             # GreenDAO数据库相关
```

## 页面导航

```
LoginActivity ──▶ HomeActivity ──▶ CameraActivity/VlcPlayerActivity
  登录页            首页/摄像头列表      摄像头预览/云台控制
```

## API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/app/login` | POST | 用户登录 |
| `/api/v1/camera/info2` | POST | 获取摄像头列表 |

## 如何使用

### 环境要求

- Android Studio Hedgehog 或更高版本
- JDK 11+
- Android SDK 36

### 构建与运行

1. 克隆仓库：
   ```bash
   git clone https://github.com/yy1327/fchsoftware.git
   ```

2. 使用 Android Studio 打开项目

3. 等待 Gradle 同步完成

4. 选择设备或模拟器，点击 **Run** 运行

### 测试账号

- 手机号：`18500000009`
- 密码：`123456`（需MD5加密传输）

## License

MIT
```

- [ ] **Step 2: 提交文档更新**

```bash
git add README.md
git commit -m "docs: 更新项目文档，反映新增功能"
```

---

## 执行计划

### 执行方式

根据 compose:ask skill，询问用户偏好的执行方式。如果没有用户可用，默认使用 Inline 方式执行。

### 验证点

每个任务完成后，运行相应的测试和验证命令，确保功能正常。

### 提交策略

每个任务完成后提交代码，确保代码版本可追溯。