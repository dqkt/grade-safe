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

@Entity(foreignKeys = @ForeignKey(entity = Year.class, parentColumns = "yearID", childColumns = "yearID", onDelete = CASCADE, onUpdate = CASCADE))
@TypeConverters(GradingScaleConverter.class)
public class Term implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int termID;
    private String name;

    private GradingScale gradingScale;

    @Ignore
    List<Course> courses;

    private double totalNumCredits;
    private double gpa;

    private int yearID;

    private int listIndex;

    Term(String name, int yearID) {
        this.name = name;

        this.yearID = yearID;
        this.gradingScale = new GradingScale("Standard Grading Scale");
        this.gradingScale.addScoreRange("A", new ScoreRange(90, Double.MAX_VALUE, 4));
        this.gradingScale.addScoreRange("B", new ScoreRange(80, 90, 3));
        this.gradingScale.addScoreRange("C", new ScoreRange(70, 80, 2));
        this.gradingScale.addScoreRange("D", new ScoreRange(60, 70, 1));
        this.gradingScale.addScoreRange("F", new ScoreRange(0, 60, 0));
    }

    protected Term(Parcel in) {
        name = in.readString();
        gradingScale = (GradingScale) in.readValue(GradingScale.class.getClassLoader());
        totalNumCredits = in.readDouble();
        gpa = in.readDouble();
        yearID = in.readInt();
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

    public void setCourses(List<Course> courses) {
        this.courses = courses;
        updateTotalNumCredits();
        updateGpa();
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

    public GradingScale getGradingScale() {
        return gradingScale;
    }
    public void setGradingScale(GradingScale gradingScale) {
        this.gradingScale = gradingScale;
    }

    public void updateTotalNumCredits() {
        totalNumCredits = 0;
        for (Course course : courses) {
            totalNumCredits += course.getNumCredits();
        }
    }

    public void updateGpa() {
        gpa = totalNumCredits != 0 ? getTotalContributionTowardGpa() / totalNumCredits : 0;
    }

    public double getTotalContributionTowardGpa() {
        double contribution = 0;
        if (courses != null) {
            ScoreRange scoreRange;
            for (Course course : courses) {
                if ((scoreRange = gradingScale.getScoreRange(course.getOverallGrade())) != null) {
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
        return Objects.equals(name, otherTerm.name) && gradingScale.equals(otherTerm.gradingScale)
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
