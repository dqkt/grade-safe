package com.example.dq.gradesafe;

import android.app.PendingIntent;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by DQ on 4/1/2018.
 */

@Entity
public class GradingScale implements Serializable {

    @PrimaryKey
    @NonNull
    private String name;

    @TypeConverters(ScoreRangeListConverter.class)
    private List<ScoreRange> scoreRanges;

    GradingScale(String name) {
        this.name = name;
        this.scoreRanges = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<ScoreRange> getScoreRanges() {
        return scoreRanges;
    }
    public void setScoreRanges(List<ScoreRange> scoreRanges) {
        this.scoreRanges = scoreRanges;
    }

    public void addScoreRange(ScoreRange scoreRange) {
        int numScoreRanges = scoreRanges.size();
        double exclusiveUpperBound = scoreRange.getExclusiveUpperBound();
        int i;
        for (i = 0; i < numScoreRanges && scoreRanges.get(i).getInclusiveLowerBound() > exclusiveUpperBound; i++);
        scoreRanges.add(i, scoreRange);
    }

    public ScoreRange getScoreRange(double score) {
        int numScoreRanges = scoreRanges.size();
        if (numScoreRanges == 0) {
            return null;
        }
        int i;
        for (i = 0; i < numScoreRanges && scoreRanges.get(i).getInclusiveLowerBound() > score; i++);
        if (i < numScoreRanges) {
            return scoreRanges.get(i);
        }
        return null;
    }

    public boolean equals(final GradingScale otherGradingScale) {
        if (this == otherGradingScale) {
            return true;
        }

        if (!Objects.equals(name, otherGradingScale.name)) {
            return false;
        }

        int numScoreRanges;
        if ((numScoreRanges = scoreRanges.size()) != otherGradingScale.scoreRanges.size()) {
            return false;
        }

        for (int i = 0; i < numScoreRanges; i++) {
            if (!scoreRanges.get(i).equals(otherGradingScale.scoreRanges.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static GradingScale createStandardGradingScale() {
        GradingScale standardGradingScale = new GradingScale("Standard Grading Scale");
        standardGradingScale.addScoreRange(new ScoreRange("F", 0, 60, 0.0));
        standardGradingScale.addScoreRange(new ScoreRange("D", 60, 70, 1.0));
        standardGradingScale.addScoreRange(new ScoreRange("C", 70, 80, 2.0));
        standardGradingScale.addScoreRange(new ScoreRange("B", 80, 90, 3.0));
        standardGradingScale.addScoreRange(new ScoreRange("A", 90, Double.MAX_VALUE, 4.0));

        return standardGradingScale;
    }

    @Dao
    public interface GradingScaleDao {
        @Insert(onConflict = OnConflictStrategy.IGNORE)
        void insertAll(GradingScale... gradingScales);

        @Delete
        void delete(GradingScale gradingScale);

        @Update
        void update(GradingScale gradingScale);

        @Query("SELECT * FROM gradingScale")
        LiveData<List<GradingScale>> getAllGradingScales();
    }
}

class ScoreRange implements Serializable {

    private String grade;
    private double inclusiveLowerBound;
    private double exclusiveUpperBound;
    private double contribution;

    ScoreRange(String grade, double inclusiveLowerBound, double exclusiveUpperBound, double contribution) {
        this.grade = grade;
        this.inclusiveLowerBound = inclusiveLowerBound;
        this.exclusiveUpperBound = exclusiveUpperBound;
        this.contribution = contribution;
    }

    public String getGrade() { return grade; }
    public void setGrade(String grade) {
        this.grade = grade;
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
        return Objects.equals(grade, otherScoreRange.grade)
                && inclusiveLowerBound == otherScoreRange.inclusiveLowerBound && exclusiveUpperBound == otherScoreRange.exclusiveUpperBound
                && contribution == otherScoreRange.contribution;
    }
}

class ScoreRangeListConverter {

    @TypeConverter
    public static List<ScoreRange> toScoreRanges(String value) {
        List<ScoreRange> scoreRangeList = new ArrayList<>();
        Scanner scanner = new Scanner(value);
        scanner.useDelimiter("\t");
        String grade;
        double inclusiveLowerBound, exclusiveUpperBound, contribution;
        while (scanner.hasNext()) {
            grade = scanner.next();
            inclusiveLowerBound = Double.parseDouble(scanner.next());
            exclusiveUpperBound = Double.parseDouble(scanner.next());
            contribution = Double.parseDouble(scanner.next());
            ScoreRange scoreRange = new ScoreRange(grade, inclusiveLowerBound, exclusiveUpperBound, contribution);
            scoreRangeList.add(scoreRange);
        }
        return scoreRangeList;
    }

    @TypeConverter
    public static String toString(List<ScoreRange> scoreRanges) {
        StringBuilder str = new StringBuilder();
        for (ScoreRange scoreRange : scoreRanges) {
            str.append(ScoreRangeConverter.toString((ScoreRange) scoreRange));
            str.append("\t");
        }
        return str.toString();
    }

}

class ScoreRangeConverter {

    @TypeConverter
    public static ScoreRange toScoreRange(String value) {
        Scanner scanner = new Scanner(value);
        scanner.useDelimiter("\t");
        String grade = scanner.next();
        double inclusiveLowerBound = Double.parseDouble(scanner.next());
        double exclusiveUpperBound = Double.parseDouble(scanner.next());
        double contribution = Double.parseDouble(scanner.next());
        return new ScoreRange(grade, inclusiveLowerBound, exclusiveUpperBound, contribution);
    }

    @TypeConverter
    public static String toString(ScoreRange scoreRange) {
        return scoreRange.getGrade() + "\t"
                + String.valueOf(scoreRange.getInclusiveLowerBound()) + "\t"
                + String.valueOf(scoreRange.getExclusiveUpperBound()) + "\t"
                + String.valueOf(scoreRange.getContribution());
    }

}