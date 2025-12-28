package com.manufacturing.manufacturingindex.model;

public class DefectReturns extends BestPractice {

    private int shipped;
    private int returned;
    private double wholesale;

    public DefectReturns(String fy, String quarter, String factory,
                          int shipped, int returned, double wholesale) {
        super(fy, quarter, factory);
        this.shipped = shipped;
        this.returned = returned;
        this.wholesale = wholesale;
    }

    @Override
    public double calculateIndex() {
        porcentage = (returned * wholesale) / shipped;

        if (porcentage <= 0.03) points = 20;
        else if (porcentage <= 0.08) points = 16;
        else if (porcentage <= 0.20) points = 10;
        else if (porcentage <= 0.49) points = 5;
        else points = 0;

        return points;
    }
}
