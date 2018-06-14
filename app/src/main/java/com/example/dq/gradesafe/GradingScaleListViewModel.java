package com.example.dq.gradesafe;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class GradingScaleListViewModel extends AndroidViewModel {

    private final LiveData<List<GradingScale>> gradingScaleList;
    private GradeSafeDatabase gradeSafeDatabase;

    public GradingScaleListViewModel(Application application) {
        super(application);

        this.gradeSafeDatabase = GradeSafeDatabase.getGradeSafeDatabase(this.getApplication());

        this.gradingScaleList = gradeSafeDatabase.gradingScaleModel().getAllGradingScales();
    }

    public LiveData<List<GradingScale>> getGradingScaleList() {
        return gradingScaleList;
    }

    public void addGradingScale(GradingScale gradingScale) {
        new GradingScaleListViewModel.InsertAsyncTask(gradeSafeDatabase).execute(gradingScale);
    }

    public void removeGradingScale(GradingScale gradingScale) {
        new GradingScaleListViewModel.RemoveAsyncTask(gradeSafeDatabase).execute(gradingScale);
    }

    public void updateGradingScale(GradingScale gradingScale) {
        new GradingScaleListViewModel.UpdateAsyncTask(gradeSafeDatabase).execute(gradingScale);
    }

    private static class InsertAsyncTask extends AsyncTask<GradingScale, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        InsertAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final GradingScale... params) {
            gradeSafeDatabase.gradingScaleModel().insertAll(params);
            return null;
        }
    }

    private static class RemoveAsyncTask extends AsyncTask<GradingScale, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        RemoveAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final GradingScale... params) {
            gradeSafeDatabase.gradingScaleModel().delete(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<GradingScale, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        UpdateAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final GradingScale... params) {
            gradeSafeDatabase.gradingScaleModel().update(params[0]);
            return null;
        }
    }
}