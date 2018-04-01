package com.example.dq.gradesafe;

import android.app.Activity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by DQ on 3/19/2018.
 */

public class Term implements Serializable {

    private String name;
    private ArrayList<Course> courses;
    private GradingScale gradingScale;

    private int totalNumCredits;
    private double gpa;

    public Term() {
        this.name = "";
        courses = new ArrayList<>();
        this.gradingScale = new GradingScale();
        gradingScale.addScoreRange("A", new ScoreRange(90, Double.MAX_VALUE, 4));
        gradingScale.addScoreRange("B", new ScoreRange(80, 90, 3));
        gradingScale.addScoreRange("C", new ScoreRange(70, 80, 2));
        gradingScale.addScoreRange("D", new ScoreRange(60, 70, 1));
        gradingScale.addScoreRange("F", new ScoreRange(0, 60, 0));
    }

    Term(String name) {
        this.name = name;
        courses = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    ArrayList<Course> getCourses() {
        return this.courses;
    }

    void addCourse(Course course) {
        courses.add(course);
        totalNumCredits += course.getNumCredits();
    }
    public void removeCourse(Course course) {
        totalNumCredits -= course.getNumCredits();
        courses.remove(course);
    }

    public int getTotalNumCredits() {
        return totalNumCredits;
    }

    public void updateTotalNumCredits() {
        totalNumCredits = 0;
        for (Course course : courses) {
            totalNumCredits += course.getNumCredits();
        }
    }

    public double getGPA() {
        return gpa;
    }

    public void updateGPA() {
        gpa = 0;
        for (Course course : courses) {
            gpa += course.getNumCredits() * gradingScale.getScoreRange(course.getOverallGrade()).getContribution();
        }
        gpa /= totalNumCredits;
    }

    void loadCourses(Activity activity) {
        courses = Maintenance.loadArrayList(activity, Course.class, coursesSharedPreferences(), courseListKey());
    }

    void saveCourses(Activity activity) {
        Maintenance.saveArrayList(activity, courses, coursesSharedPreferences(), courseListKey());
    }

    private String coursesSharedPreferences() {
        return getName().toUpperCase() + "_COURSES";
    }
    private String courseListKey() {
        return getName().toUpperCase() + "_COURSE_LIST";
    }
}
