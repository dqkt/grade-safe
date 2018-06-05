package com.example.dq.gradesafe;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class YearListViewModel extends AndroidViewModel {

    private final LiveData<List<Year>> yearList;
    private GradeSafeDatabase gradeSafeDatabase;

    private List<Year> currentYearList;

    public YearListViewModel(Application application) {
        super(application);

        this.gradeSafeDatabase = GradeSafeDatabase.getGradeSafeDatabase(this.getApplication());

        this.yearList = gradeSafeDatabase.yearModel().getAllYears();
    }

    public LiveData<List<Year>> getYearList() {
        return yearList;
    }

    public void addYear(Year year) {
        new InsertAsyncTask(gradeSafeDatabase).execute(year);
    }

    public void removeYear(Year year) {
        new RemoveAsyncTask(gradeSafeDatabase).execute(year);
    }

    public void updateYear(Year year) {
        new UpdateAsyncTask(gradeSafeDatabase).execute(year);
    }

    private static class InsertAsyncTask extends AsyncTask<Year, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        InsertAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Year... params) {
            gradeSafeDatabase.yearModel().insertAll(params);
            return null;
        }
    }

    private static class RemoveAsyncTask extends AsyncTask<Year, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        RemoveAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Year... params) {
            gradeSafeDatabase.yearModel().delete(params[0]);
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Year, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        UpdateAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Year... params) {
            gradeSafeDatabase.yearModel().update(params[0]);
            return null;
        }
    }
}
