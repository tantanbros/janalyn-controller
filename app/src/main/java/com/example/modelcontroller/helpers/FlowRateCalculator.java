package com.example.modelcontroller.helpers;

import com.example.modelcontroller.models.Patient;


public class FlowRateCalculator {

    public static int ComputeFlowRate(Patient patient) {
        int flowRate = 0;
        int age = patient.getAge();
        boolean isMale = patient.isMale();

        // Normal Values
        if(age >= 4 && age <=7){
            flowRate = 10;
        } else if (age >= 8 && age <= 13) {
            flowRate = isMale ? 12 : 15;
        } else if (age >= 14 && age <= 45) {
            flowRate = isMale ? 21 : 18;
        }else if (age >= 46 && age <= 65) {
            flowRate = isMale ? 12 : 18;
        }else if (age >= 66 && age <= 80) {
            flowRate = isMale ? 9 : 18;
        } else {
            // age not in range
            return -1;
        }

        // When the surgery status selected was “Post-operative” the normal values depending on
        // age and gender will decrease by 5ml/s
        if(patient.isPostOperative()) {
            flowRate -= 5;
        }

        return flowRate;
    }
}
