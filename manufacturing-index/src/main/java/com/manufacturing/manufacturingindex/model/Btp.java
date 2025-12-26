package com.manufacturing.manufacturingindex.model;

public class Btp extends BestPractice {

    private int tested;
    private int approved;

    public Btp(String fy, String quarter, String factory,
               int tested, int approved) {
        super(fy, quarter, factory);
        this.tested = tested;
        this.approved = approved;
    }

    @Override
    public double calculateIndex() {
        porcentage = (double) approved / tested * 100;

        if (porcentage >= 99) points = 40;
        else if (porcentage >= 97) points = 30;
        else if (porcentage >= 93.5) points = 20;
        else if (porcentage >= 91) points = 10;
        else points = 0;

        return points;
    }
}
