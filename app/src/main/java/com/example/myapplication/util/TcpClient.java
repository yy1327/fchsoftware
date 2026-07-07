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
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
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