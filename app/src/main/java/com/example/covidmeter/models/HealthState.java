package com.example.covidmeter.models;

import java.util.List;

public class HealthState {
    private String state;
    private List<Symptom> symptoms;

    public HealthState(String state, List<Symptom> symptoms) {
        this.state = state;
        this.symptoms = symptoms;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }
}
