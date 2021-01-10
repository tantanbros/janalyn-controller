package com.example.modelcontroller.models;

public class Patient {
    private int Age;
    private String Gender;
    private String Status;
    private double FluidIntake;

    public Patient(int age, String gender, String status, double fluidIntake) {
        Age = age;
        Gender = gender;
        Status = status;
        FluidIntake = fluidIntake;
    }

    public int getAge() {
        return Age;
    }

    public void setAge(int age) {
        Age = age;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public double getFluidIntake() {
        return FluidIntake;
    }

    public void setFluidIntake(double fluidIntake) {
        FluidIntake = fluidIntake;
    }

    public boolean isMale() { return Gender.toLowerCase().equals("male"); }
    public boolean isPostOperative() { return Status.toLowerCase().equals("post-operative"); }
}
