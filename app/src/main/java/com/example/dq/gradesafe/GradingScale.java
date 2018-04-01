package com.example.dq.gradesafe;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by DQ on 4/1/2018.
 */

public class GradingScale implements Serializable {

    private String name;
    private HashMap<String, ScoreRange> scoreRanges;

    GradingScale() {
        this.name = "";
        this.scoreRanges = new HashMap<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void addScoreRange(String label, ScoreRange scoreRange) {
        scoreRanges.put(label, scoreRange);
    }

    public ScoreRange getScoreRange(String label) {
        return scoreRanges.get(label);
    }
}

class ScoreRange implements Serializable {

    private double inclusiveLowerBound;
    private double exclusiveUpperBound;
    private double contribution;

    ScoreRange() {
        this.inclusiveLowerBound = 0;
        this.exclusiveUpperBound = 0;
        this.contribution = 0;
    }

    ScoreRange(double inclusiveLowerBound, double exclusiveUpperBound, double contribution) {
        this.inclusiveLowerBound = inclusiveLowerBound;
        this.exclusiveUpperBound = exclusiveUpperBound;
        this.contribution = contribution;
    }

    public double getInclusiveLowerBound() {
        return inclusiveLowerBound;
    }
    public void setInclusiveLowerBound(double inclusiveLowerBound) {
        this.inclusiveLowerBound = inclusiveLowerBound;
    }

    public double getExclusiveUpperBound() {
        return exclusiveUpperBound;
    }
    public void setExclusiveUpperBound(double exclusiveUpperBound) {
        this.exclusiveUpperBound = exclusiveUpperBound;
    }

    public double getContribution() {
        return contribution;
    }
    public void setContribution(double contribution) {
        this.contribution = contribution;
    }
}
