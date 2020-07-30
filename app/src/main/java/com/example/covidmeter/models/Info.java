package com.example.covidmeter.models;

public class Info {
    private String typeInfo;
    private int qtdInfo;

    public Info(String typeInfo) {
        this.typeInfo = typeInfo;
        this.qtdInfo=0;
    }

    public String getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(String typeInfo) {
        this.typeInfo = typeInfo;
    }

    public int getQtdInfo() {
        return qtdInfo;
    }

    public void setQtdInfo(int qtdInfo) {
        this.qtdInfo = qtdInfo;
    }
}
