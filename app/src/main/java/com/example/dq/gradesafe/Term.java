package com.example.dq.gradesafe;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;
import android.os.Parcel;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by DQ on 3/19/2018.
 */

@Entity(foreignKeys = {
        @ForeignKey(entity = Year.class, parentColumns = "yearID", childColumns = "yearID", onDelete = CASCADE, onUpdate = CASCADE),
        @ForeignKey(entity = GradingScale.class, parentColumns = "name", childColumns = "gradingScaleName", onUpdate = CASCADE)
        })
public class Term implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int termID;
    private String name;

    @Ignore
    List<Course> courses;

    private double totalNumCredits;
    private double gpa;

    private int yearID;
    private String gradingScaleName;

    private int listIndex;

    Term(String name, String gradingScaleName, int yearID) {
        this.name = name;

        this.gradingScaleName = gradingScaleName;
        this.yearID = yearID;
    }

    public int getTermID() {
        return termID;
    }
    public void setTermID(int termID) {
        this.termID = termID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setCourses(List<Course> courses, GradingScale gradingScale) {
        this.courses = courses;
        updateTotalNumCredits();
        updateGpa(gradingScale);
    }

    public double getTotalNumCredits() {
        return totalNumCredits;
    }
    public void setTotalNumCredits(double totalNumCredits) { this.totalNumCredits = totalNumCredits; }

    public double getGpa() {
        return gpa;
    }
    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public String getGradingScaleName() {
        return gradingScaleName;
    }
    public void setGradingScale(String gradingScaleName) {
        this.gradingScaleName = gradingScaleName;
    }

    public void updateTotalNumCredits() {
        totalNumCredits = 0;
        for (Course course : courses) {
            totalNumCredits += course.getNumCredits();
        }
    }

    public void updateGpa(GradingScale gradingScale) {
        gpa = totalNumCredits != 0 ? getTotalContributionTowardGpa(gradingScale) / totalNumCredits : 0;
    }

    public double getTotalContributionTowardGpa(GradingScale gradingScale) {
        double contribution = 0;
        if (courses != null) {
            ScoreRange scoreRange;
            for (Course course : courses) {
                if ((scoreRange = gradingScale.getScoreRange(course.getOverallScore())) != null) {
                    contribution += course.getNumCredits() * scoreRange.getContribution();
                }
            }
        }
        return contribution;
    }

    public int getYearID() {
        return yearID;
    }
    public void setYearID(int yearID) {
        this.yearID = yearID;
    }

    public int getListIndex() {
        return listIndex;
    }
    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }

    public boolean equals(final Term otherTerm) {
        return Objects.equals(name, otherTerm.name) && Objects.equals(gradingScaleName, otherTerm.gradingScaleName)
                && totalNumCredits == otherTerm.totalNumCredits && gpa == otherTerm.gpa;
    }

    @Dao
    public interface TermDao {
        @Insert
        void insertAll(Term... terms);

        @Delete
        void delete(Term term);

        @Update
        void update(Term term);

        @Query("DELETE FROM term WHERE yearID is :yearID")
        void deleteAllTermsInYear(int yearID);

        @Query("SELECT * FROM term ORDER BY listIndex ASC")
        LiveData<List<Term>> getAllTerms();

        @Query("SELECT * FROM term WHERE yearID IS :yearID ORDER BY listIndex ASC")
        LiveData<List<Term>> getAllTermsInYear(int yearID);

        @Query("SELECT COUNT(*) FROM term WHERE yearID IS :yearID")
        int getNumTermsInYear(int yearID);
    }
}
