package com.example.modelcontroller.models;

public class Simulation {
    private String Volume;
    private String FlowRate;
    private String Concentration;

    public Simulation() {}

    public Simulation(String volume, String flowRate, String concentration) {
        Volume = volume;
        FlowRate = flowRate;
        Concentration = concentration.toLowerCase();
    }

    public String getVolume() {
        return Volume;
    }

    public void setVolume(String volume) {
        Volume = volume;
    }

    public void setVolume(double volume) {
        Volume = Double.toString(volume);
    }

    public String getFlowRate() {
        return FlowRate;
    }

    public void setFlowRate(String flowRate) {
        FlowRate = flowRate;
    }
    public void setFlowRate(int flowRate) { FlowRate = Integer.toString(flowRate); }

    public String getConcentration() {
        return Concentration.toLowerCase();
    }

    public void setConcentration(String concentration) {
        Concentration = concentration.toLowerCase();
    }
}
