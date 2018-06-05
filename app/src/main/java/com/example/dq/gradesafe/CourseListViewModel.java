package com.example.dq.gradesafe;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CourseListViewModel extends AndroidViewModel {

    private final LiveData<List<Course>> courseList;
    private GradeSafeDatabase gradeSafeDatabase;

    public CourseListViewModel(Application application) {
        super(application);

        this.gradeSafeDatabase = GradeSafeDatabase.getGradeSafeDatabase(this.getApplication());

        this.courseList = gradeSafeDatabase.courseModel().getAllCourses();
    }

    public LiveData<List<Course>> getCourseList() {
        return courseList;
    }
    public LiveData<List<Course>> getCoursesInTerm(Term term) {
        return gradeSafeDatabase.courseModel().getAllCoursesInTerm(term.getTermID());
    }

    public void addCourse(Course course) {
        new CourseListViewModel.InsertAsyncTask(gradeSafeDatabase).execute(course);
    }

    public void removeCourse(Course course) {
        new CourseListViewModel.RemoveAsyncTask(gradeSafeDatabase).execute(course);
    }

    public void updateCourse(Course course) {
        new CourseListViewModel.UpdateAsyncTask(gradeSafeDatabase).execute(course);
    }

    private static class InsertAsyncTask extends AsyncTask<Course, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        InsertAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Course... params) {
            gradeSafeDatabase.courseModel().insertAll(params);
            return null;
        }
    }

    private static class RemoveAsyncTask extends AsyncTask<Course, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        RemoveAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Course... params) {
            gradeSafeDatabase.courseModel().delete(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Course, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        UpdateAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Course... params) {
            gradeSafeDatabase.courseModel().update(params[0]);
            return null;
        }
    }
}