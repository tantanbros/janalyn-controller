package com.example.modelcontroller.helpers;

import com.example.modelcontroller.models.Patient;
import com.example.modelcontroller.models.Simulation;

public class DataHolder {
    private static Patient patient;
    private static Simulation simulation;
    private static String deviceIp;

    public static String getDeviceIp() {
        return deviceIp;
    }

    public static void setDeviceIp(String deviceIp) {
        DataHolder.deviceIp = deviceIp;
    }


    public static Patient getPatient() {
        return patient;
    }

    public static void setPatient(Patient patient) {
        DataHolder.patient = patient;
    }

    public static Simulation getSimulation() {
        if(simulation == null) {
            simulation = new Simulation();
        }
        return simulation;
    }
}
