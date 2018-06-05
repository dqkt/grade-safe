package com.example.dq.gradesafe;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TermListViewModel extends AndroidViewModel {

    private final LiveData<List<Term>> termList;
    private GradeSafeDatabase gradeSafeDatabase;

    private Integer numTerms;

    public TermListViewModel(Application application) {
        super(application);

        this.gradeSafeDatabase = GradeSafeDatabase.getGradeSafeDatabase(this.getApplication());

        this.termList = gradeSafeDatabase.termModel().getAllTerms();
    }

    public LiveData<List<Term>> getTermList() {
        return termList;
    }
    public LiveData<List<Term>> getTermsInYear(Year year) {
        return gradeSafeDatabase.termModel().getAllTermsInYear(year.getYearID());
    }

    public void addTerm(Term term) {
        new TermListViewModel.InsertAsyncTask(gradeSafeDatabase).execute(term);
    }

    public void removeTerm(Term term) {
        new TermListViewModel.RemoveAsyncTask(gradeSafeDatabase).execute(term);
    }

    public void removeAllTermsInYear(Year year) {
        new TermListViewModel.RemoveAllTermsInYearAsyncTask(gradeSafeDatabase).execute(year);
    }

    public void updateTerm(Term term) {
        new TermListViewModel.UpdateAsyncTask(gradeSafeDatabase).execute(term);
    }

    private static class InsertAsyncTask extends AsyncTask<Term, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        InsertAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Term... params) {
            gradeSafeDatabase.termModel().insertAll(params);
            return null;
        }
    }

    private static class RemoveAsyncTask extends AsyncTask<Term, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        RemoveAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Term... params) {
            gradeSafeDatabase.termModel().delete(params[0]);
            return null;
        }
    }

    private static class RemoveAllTermsInYearAsyncTask extends AsyncTask<Year, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        RemoveAllTermsInYearAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Year... params) {
            gradeSafeDatabase.termModel().deleteAllTermsInYear(params[0].getYearID());
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Term, Void, Void> {

        private GradeSafeDatabase gradeSafeDatabase;

        UpdateAsyncTask(GradeSafeDatabase gradeSafeDatabase) {
            this.gradeSafeDatabase = gradeSafeDatabase;
        }

        @Override
        protected Void doInBackground(final Term... params) {
            gradeSafeDatabase.termModel().update(params[0]);
            return null;
        }
    }
}