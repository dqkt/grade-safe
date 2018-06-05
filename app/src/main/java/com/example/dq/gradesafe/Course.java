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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by DQ on 3/19/2018.
 */

@Entity(foreignKeys = @ForeignKey(entity = Term.class, parentColumns = "termID", childColumns = "termID", onDelete = CASCADE, onUpdate = CASCADE))
public class Course implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int courseID;
    private String name;
    private String fullName;
    private double numCredits;
    private boolean countsTowardGPA;

    private double overallScore;
    private String overallGrade;

    private int termID;

    private int listIndex;

    Course(String name, double numCredits, boolean countsTowardGPA, int termID) {
        this.name = name;
        this.numCredits = numCredits;
        this.countsTowardGPA = countsTowardGPA;
        overallGrade = null;

        this.termID = termID;
    }

    protected Course(Parcel in) {
        name = in.readString();
        fullName = in.readString();
        numCredits = in.readDouble();
        countsTowardGPA = in.readByte() != 0x00;
        overallScore = in.readDouble();
        overallGrade = in.readString();
        termID = in.readInt();
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

    double getNumCredits() { return numCredits; }
    public void setNumCredits(int numCredits) { this.numCredits = numCredits; }

    public boolean countsTowardGPA() {
        return countsTowardGPA;
    }
    public void setCountsTowardGPA(boolean countsTowardGPA) {
        this.countsTowardGPA = countsTowardGPA;
    }

    public double getOverallScore() {
        return overallScore;
    }
    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public void updateOverallScore() {
        // TODO: Implementation
    }

    String getOverallGrade() {
        return overallGrade;
    }
    public void setOverallGrade(String overallGrade) {
        this.overallGrade = overallGrade;
    }

    public void updateOverallGrade() {
        // TODO: Implementation
    }

    void setUpDefaults() {
        overallScore = 1;
        overallGrade = "A";
    }

    public int getTermID() {
        return termID;
    }
    public void setTermID(int termID) {
        this.termID = termID;
    }

    public int getListIndex() {
        return listIndex;
    }
    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
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