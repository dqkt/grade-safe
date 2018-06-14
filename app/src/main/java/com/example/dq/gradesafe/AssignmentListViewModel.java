package com.example.dq.gradesafe;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class AssignmentListViewModel extends AndroidViewModel {

    private final LiveData<List<Assignment>> assignmentList;
    private GradeSafeDatabase gradeSafeDatabase;

    public AssignmentListViewModel(Application application) {
        super(application);

        this.gradeSafeDatabase = GradeSafeDatabase.getGradeSafeDatabase(this.getApplication());

        this.assignmentList = gradeSafeDatabase.assignmentModel().getAllAssignments();
    }

    public LiveData<List<Assignment>> getAssignmentList() {
        return assignmentList;
    }
    public LiveData<List<Assignment>> getAssignmentsInCourse(Course course) {
        return gradeSafeDatabase.assignmentModel().getAllAssignmentsInCourse(course.getCourseID());
    }

    public void addAssignment(Assignment assignment) {
        new AssignmentListViewModel.InsertAsyncTask(gradeSafeDatabase).execute(assignment);
    }

    public void removeAssignment(Assignment assignment) {
        new AssignmentListViewModel.RemoveAsyncTask(gradeSafeDatabase).execute(assignment);
    }

    public void updateAssignment(Assignment assignment) {
        new AssignmentListViewModel.UpdateAsyncTask(gradeSafeDatabase).execute(assignment);
    }

    private static class InsertAsyncTask extends AsyncTask<Assignment, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        InsertAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Assignment... params) {
            gradeSafeDatabase.assignmentModel().insertAll(params);
            return null;
        }
    }

    private static class RemoveAsyncTask extends AsyncTask<Assignment, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        RemoveAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Assignment... params) {
            gradeSafeDatabase.assignmentModel().delete(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Assignment, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        UpdateAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Assignment... params) {
            gradeSafeDatabase.assignmentModel().update(params[0]);
            return null;
        }
    }
}