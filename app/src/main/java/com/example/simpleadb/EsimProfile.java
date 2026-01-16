package com.example.simpleadb;

public class EsimProfile {
    private String name;
    private String activationCode;
    private String smdpAddress;

    public EsimProfile(String name, String activationCode, String smdpAddress) {
        this.name = name;
        this.activationCode = activationCode;
        this.smdpAddress = smdpAddress;
    }

    public String getName() { return name; }
    public String getActivationCode() { return activationCode; }
    public String getSmdpAddress() { return smdpAddress; }
}
