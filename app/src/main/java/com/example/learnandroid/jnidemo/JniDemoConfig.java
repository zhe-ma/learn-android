package com.example.learnandroid.jnidemo;

public class JniDemoConfig {
    private int type = 0;
    private String name = "";

    public JniDemoConfig() {
    }

    public JniDemoConfig(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
