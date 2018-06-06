package com.example.dq.gradesafe;

import android.app.PendingIntent;
import android.arch.persistence.room.TypeConverter;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by DQ on 4/1/2018.
 */

public class GradingScale implements Serializable {

    private String name;
    private HashMap<String, ScoreRange> scoreRanges;

    GradingScale(String name) {
        this.name = name;
        this.scoreRanges = new HashMap<>();
    }

    protected GradingScale(Parcel in) {
        name = in.readString();
        scoreRanges = (HashMap) in.readValue(HashMap.class.getClassLoader());
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, ScoreRange> getScoreRanges() {
        return scoreRanges;
    }

    public void addScoreRange(String label, ScoreRange scoreRange) {
        scoreRanges.put(label, scoreRange);
    }

    public ScoreRange getScoreRange(String label) {
        return scoreRanges.get(label);
    }

    public boolean equals(final GradingScale otherGradingScale) {
        if (this == otherGradingScale) {
            return true;
        }

        if (!Objects.equals(name, otherGradingScale.name)) {
            return false;
        }

        if (scoreRanges.size() != otherGradingScale.scoreRanges.size()) {
            return false;
        }

        for (String key : scoreRanges.keySet()) {
            if (!scoreRanges.get(key).equals(otherGradingScale.scoreRanges.get(key))) {
                return false;
            }
        }
        return true;
    }
}

class ScoreRange implements Serializable {

    private double inclusiveLowerBound;
    private double exclusiveUpperBound;
    private double contribution;

    ScoreRange(double inclusiveLowerBound, double exclusiveUpperBound, double contribution) {
        this.inclusiveLowerBound = inclusiveLowerBound;
        this.exclusiveUpperBound = exclusiveUpperBound;
        this.contribution = contribution;
    }

    protected ScoreRange(Parcel in) {
        inclusiveLowerBound = in.readDouble();
        exclusiveUpperBound = in.readDouble();
        contribution = in.readDouble();
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

    public boolean equals(final ScoreRange otherScoreRange) {
        return inclusiveLowerBound == otherScoreRange.inclusiveLowerBound && exclusiveUpperBound == otherScoreRange.exclusiveUpperBound
                && contribution == otherScoreRange.contribution;
    }
}

class ScoreRangeConverter {

    @TypeConverter
    public static ScoreRange toScoreRange(String value) {
        Scanner scanner = new Scanner(value);
        scanner.useDelimiter("\t");
        double inclusiveLowerBound = Double.parseDouble(scanner.next());
        double exclusiveUpperBound = Double.parseDouble(scanner.next());
        double contribution = Double.parseDouble(scanner.next());
        return new ScoreRange(inclusiveLowerBound, exclusiveUpperBound, contribution);
    }

    @TypeConverter
    public static String toString(ScoreRange scoreRange) {
        return String.valueOf(scoreRange.getInclusiveLowerBound()) + "\t"
                + String.valueOf(scoreRange.getExclusiveUpperBound()) + "\t"
                + String.valueOf(scoreRange.getContribution());
    }

}

class GradingScaleConverter {

    @TypeConverter
    public static GradingScale toGradingScale(String value) {
        Scanner scanner = new Scanner(value);
        scanner.useDelimiter("\t");
        String name = scanner.next();
        GradingScale gradingScale = new GradingScale(name);
        String label;
        double inclusiveLowerBound, exclusiveUpperBound, contribution;
        while (scanner.hasNext()) {
            label = scanner.next();
            inclusiveLowerBound = Double.parseDouble(scanner.next());
            exclusiveUpperBound = Double.parseDouble(scanner.next());
            contribution = Double.parseDouble(scanner.next());
            ScoreRange scoreRange = new ScoreRange(inclusiveLowerBound, exclusiveUpperBound, contribution);
            gradingScale.addScoreRange(label, scoreRange);
        }
        return gradingScale;
    }

    @TypeConverter
    public static String toString(GradingScale gradingScale) {
        StringBuilder str = new StringBuilder(gradingScale.getName() + "\t");
        HashMap<String, ScoreRange> scoreRanges = gradingScale.getScoreRanges();
        Iterator it = scoreRanges.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            str.append(pair.getKey().toString());
            str.append("\t");
            str.append(ScoreRangeConverter.toString((ScoreRange) pair.getValue()));
            str.append("\t");
        }
        return str.toString();
    }
}