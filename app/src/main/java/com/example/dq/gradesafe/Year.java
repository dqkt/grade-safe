package com.example.dq.gradesafe;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by DQ on 3/19/2018.
 */

public class Year implements Serializable {

    private String name;
    private ArrayList<Term> terms;

    public Year() {
        this.name = "";
        terms = new ArrayList<>();
    }

    public Year(String name) {
        this.name = name;
        terms = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Term> getTerms() {
        return this.terms;
    }

    public void addTerm(Term term) {
        terms.add(term);
    }

    public void removeTerm(Term term) {
        terms.remove(term);
    }

}
