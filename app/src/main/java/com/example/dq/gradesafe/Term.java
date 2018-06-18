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
        @ForeignKey(entity = Year.class, parentColumns = "yearID", childColumns = "yearID", onDelete = CASCADE, onUpdate = CASCADE)
        })
public class Term implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int termID;
    private String name;

    @Ignore
    private List<Course> courses;

    private double totalNumCredits;
    private double gpa;

    private int yearID;

    private int listIndex;

    Term(String name, int yearID) {
        this.name = name;

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

    public void updateTotalNumCredits() {
        totalNumCredits = 0;
        for (Course course : courses) {
            totalNumCredits += course.getNumCredits();
        }
    }

    public void updateGpa(GradingScale gradingScale) {
        double contribution = 0;
        double totalNumCreditsContributing = 0;
        if (courses != null) {
            ScoreRange scoreRange;
            double numCredits;
            for (Course course : courses) {
                if (course.countsTowardGPA() && (scoreRange = gradingScale.getScoreRange(course.getOverallScore())) != null) {
                    numCredits = course.getNumCredits();
                    contribution += numCredits * scoreRange.getContribution();
                    totalNumCreditsContributing += numCredits;
                }
            }
        }
        gpa = totalNumCreditsContributing != 0 ? contribution / totalNumCreditsContributing : 0;
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

    public boolean equals(final Object otherObject) {
        if (otherObject instanceof Term) {
            final Term otherTerm = (Term) otherObject;
            return Objects.equals(name, otherTerm.name) && totalNumCredits == otherTerm.totalNumCredits && gpa == otherTerm.gpa;
        }
        return super.equals(otherObject);
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
