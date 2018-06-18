package com.example.dq.gradesafe;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
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

@Entity(foreignKeys = @ForeignKey(entity = Course.class, parentColumns = "courseID", childColumns = "courseID", onDelete = CASCADE, onUpdate = CASCADE))
public class Assignment implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int assignmentID;
    private String name;
    private double weight;
    private double scoreNumerator;
    private double scoreDenominator;

    private boolean complete;

    private int courseID;

    private int listIndex;

    Assignment(String name, double weight, double scoreNumerator, double scoreDenominator, boolean complete, int courseID) {
        this.name = name;
        this.weight = weight;
        this.scoreNumerator = scoreNumerator;
        this.scoreDenominator = scoreDenominator;
        this.complete = complete;

        this.courseID = courseID;
    }

    public int getAssignmentID() {
        return assignmentID;
    }
    public void setAssignmentID(int assignmentID) {
        this.assignmentID = assignmentID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getScoreNumerator() {
        return scoreNumerator;
    }
    public void setScoreNumerator(double scoreNumerator) {
        this.scoreNumerator = scoreNumerator;
    }
    public double getScoreDenominator() {
        return scoreDenominator;
    }
    public void setScoreDenominator(double scoreDenominator) {
        this.scoreDenominator = scoreDenominator;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public int getCourseID() {
        return courseID;
    }
    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public int getListIndex() {
        return listIndex;
    }
    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }

    public boolean equals(final Object otherObject) {
        if (otherObject instanceof Assignment) {
            final Assignment otherAssignment = (Assignment) otherObject;
            return Objects.equals(name, otherAssignment.name) && weight == otherAssignment.weight && complete == otherAssignment.complete
                    && scoreNumerator == otherAssignment.scoreNumerator && scoreDenominator == otherAssignment.scoreDenominator;
        }
        return super.equals(otherObject);
    }

    public String toString() {
        return name + "\nWeight: " + String.valueOf(weight) + "%\nScore: " + String.valueOf(scoreNumerator) + "/" + String.valueOf(scoreDenominator)
                + "\nComplete: " + complete;
    }

    @Dao
    public interface AssignmentDao {
        @Insert
        void insertAll(Assignment... assignments);

        @Delete
        void delete(Assignment assignment);

        @Update
        void update(Assignment assignment);

        @Query("SELECT * FROM assignment ORDER BY listIndex ASC")
        LiveData<List<Assignment>> getAllAssignments();

        @Query("SELECT * FROM assignment WHERE courseID IS :courseID ORDER BY listIndex ASC")
        LiveData<List<Assignment>> getAllAssignmentsInCourse(int courseID);
    }
}
