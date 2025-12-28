package com.manufacturing.manufacturingindex.model;

public abstract class BestPractice {

    protected String fy;
    protected String quarter;
    protected String factory;
    protected double porcentage;
    protected int points;

    public BestPractice(String fy, String quarter, String factory) {
        this.fy = fy;
        this.quarter = quarter;
        this.factory = factory;
    }

    public abstract double calculateIndex();

    public double getPorcentage() {
        return porcentage;
    }

    public int getPoints() {
        return points;
    }
}
