package com.example.dq.gradesafe;

import java.io.Serializable;

/**
 * Created by DQ on 3/19/2018.
 */

public class Assignment implements Serializable {
    
    private String name;
    private double weight;
    private double scoreNumerator;
    private double scoreDenominator;

    public Assignment() {
        this.name = "";
        this.weight = 0;
        this.scoreNumerator = 0;
        this.scoreDenominator = 0;
    }

    public Assignment(String name, double weight, double scoreNumerator, double scoreDenominator) {
        this.name = name;
        this.weight = weight;
        this.scoreNumerator = scoreNumerator;
        this.scoreDenominator = scoreDenominator;
    }

    public void setName(String name) { this.name = name; }
    public void setWeight(double weight) { this.weight = weight; }
    public void setScoreNumerator(double scoreNumerator) { this.scoreNumerator = scoreNumerator; }
    public void setScoreDenominator(double scoreDenominator) { this.scoreDenominator = scoreDenominator; }

    public String getName() { return name; }
    public double getWeight() { return weight; }
    public double getScoreNumerator() { return scoreNumerator; }
    public double getScoreDenominator() { return scoreDenominator; }
}
