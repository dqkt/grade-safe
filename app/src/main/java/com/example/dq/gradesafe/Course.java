package com.example.dq.gradesafe;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by DQ on 3/19/2018.
 */

public class Course implements Serializable {

    private String name;
    private String fullName;
    private int numCredits;
    private ArrayList<Assignment> assignments;
    private boolean countsTowardGPA;

    private double overallScore;
    private String overallGrade;

    public Course() {
        this.name = "";
        this.numCredits = 0;
        assignments = new ArrayList<>();
        this.countsTowardGPA = false;
    }

    public Course(String name, int numCredits, boolean countsTowardGPA) {
        this.name = name;
        this.numCredits = numCredits;
        assignments = new ArrayList<>();
        this.countsTowardGPA = countsTowardGPA;
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

    int getNumCredits() { return numCredits; }
    public void setNumCredits(int numCredits) { this.numCredits = numCredits; }

    public ArrayList<Assignment> getAssignments() {
        return this.assignments;
    }

    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
    }
    public void removeAssignment(Assignment assignment) {
        assignments.remove(assignment);
    }

    public boolean countsTowardGPA() {
        return countsTowardGPA;
    }
    public void setCountsTowardGPA(boolean countsTowardGPA) {
        this.countsTowardGPA = countsTowardGPA;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void updateOverallScore() {
        // TODO: Implementation
    }

    String getOverallGrade() {
        return overallGrade;
    }

    public void updateOverallGrade() {
        // TODO: Implementation
    }

    void setUpDefaults() {
        overallScore = 1;
        overallGrade = "A";
    }
}