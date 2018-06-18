package com.example.dq.gradesafe;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by DQ on 3/19/2018.
 */

@Entity(foreignKeys = {
        @ForeignKey(entity = Term.class, parentColumns = "termID", childColumns = "termID", onDelete = CASCADE, onUpdate = CASCADE),
        @ForeignKey(entity = GradingScale.class, parentColumns = "name", childColumns = "gradingScaleName", onUpdate = CASCADE)
})
public class Course implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int courseID;
    private String name;
    private String fullName;
    private double numCredits;
    private boolean countsTowardGPA;

    @Ignore
    private List<Assignment> assignments;

    private double overallScore;
    private String overallGrade;

    private int termID;
    private String gradingScaleName;

    private int listIndex;

    Course(String name, double numCredits, boolean countsTowardGPA, int termID) {
        this.name = name;
        this.numCredits = numCredits;
        this.countsTowardGPA = countsTowardGPA;
        overallGrade = null;

        this.termID = termID;
    }

    public int getCourseID() {
        return courseID;
    }
    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public double getNumCredits() { return numCredits; }
    public void setNumCredits(double numCredits) { this.numCredits = numCredits; }

    public boolean countsTowardGPA() {
        return countsTowardGPA;
    }
    public void setCountsTowardGPA(boolean countsTowardGPA) {
        this.countsTowardGPA = countsTowardGPA;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
        updateOverallScore();
    }

    public double getOverallScore() {
        return overallScore;
    }
    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public void updateOverallScore() {
        overallScore = 0;
        double totalWeight = 0;
        double weight;
        for (Assignment assignment : assignments) {
            if (assignment.isComplete()) {
                weight = assignment.getWeight();
                totalWeight += weight;
                overallScore += assignment.getScoreNumerator() / assignment.getScoreDenominator() * weight;
            }
        }
        if (totalWeight != 0) {
            overallScore /= Math.min(totalWeight, 100);
        }
        overallScore *= 100;
    }

    String getOverallGrade() {
        return overallGrade;
    }
    public void setOverallGrade(String overallGrade) {
        this.overallGrade = overallGrade;
    }

    public void updateOverallGrade(GradingScale gradingScale) {
        if (assignments != null && !assignments.isEmpty()) {
            overallGrade = gradingScale.getScoreRange(overallScore).getGrade();
        } else {
            overallGrade = null;
        }
    }

    public int getTermID() {
        return termID;
    }
    public void setTermID(int termID) {
        this.termID = termID;
    }

    public String getGradingScaleName() {
        return gradingScaleName;
    }
    public void setGradingScaleName(String gradingScaleName) {
        this.gradingScaleName = gradingScaleName;
    }

    public int getListIndex() {
        return listIndex;
    }
    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }

    public boolean equals(final Object otherObject) {
        if (otherObject instanceof Course) {
            final Course otherCourse = (Course) otherObject;
            return Objects.equals(name, otherCourse.name) && Objects.equals(fullName, otherCourse.fullName)
                    && numCredits == otherCourse.numCredits && countsTowardGPA == otherCourse.countsTowardGPA && overallScore == otherCourse.overallScore
                    && Objects.equals(overallGrade, otherCourse.overallGrade) && Objects.equals(gradingScaleName, otherCourse.gradingScaleName);
        }
        return super.equals(otherObject);
    }

    @Dao
    public interface CourseDao {
        @Insert
        void insertAll(Course... courses);

        @Delete
        void delete(Course course);

        @Update
        void update(Course course);

        @Query("SELECT * FROM course ORDER BY listIndex ASC")
        LiveData<List<Course>> getAllCourses();

        @Query("SELECT * FROM course WHERE termID IS :termID ORDER BY listIndex ASC")
        LiveData<List<Course>> getAllCoursesInTerm(int termID);
    }
}