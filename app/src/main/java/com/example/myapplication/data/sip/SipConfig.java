package com.example.myapplication.data.sip;

public class SipConfig {

    private static final SipConfig INSTANCE = new SipConfig();

    private String serverHost = "172.16.10.169";
    private int serverPort = 5060;
    private String username = "18500000009";
    private String password = "123456";
    private String domain = "172.16.10.169";
    private int callTimeout = 30;

    private SipConfig() {
    }

    public static SipConfig getInstance() {
        return INSTANCE;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getCallTimeout() {
        return callTimeout;
    }

    public void setCallTimeout(int callTimeout) {
        this.callTimeout = callTimeout;
    }

    public String getSipUri() {
        return "sip:" + username + "@" + domain;
    }

    public String getServerAddress() {
        return serverHost + ":" + serverPort;
    }
}
