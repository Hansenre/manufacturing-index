package com.manufacturing.manufacturingindex.model;

public class Mqaas extends BestPractice {

    private int pointsAvailable;
    private int pointsAchieved;

    public Mqaas(String fy, String quarter, String factory,
                 int pointsAvailable, int pointsAchieved) {
        super(fy, quarter, factory);
        this.pointsAvailable = pointsAvailable;
        this.pointsAchieved = pointsAchieved;
    }

    @Override
    public double calculateIndex() {
        porcentage = (double) pointsAchieved / pointsAvailable * 100;

        if (porcentage >= 98) points = 40;
        else if (porcentage >= 96.5) points = 35;
        else if (porcentage >= 95.5) points = 30;
        else if (porcentage >= 93) points = 20;
        else if (porcentage >= 90) points = 10;
        else points = 0;

        return points;
    }
}
