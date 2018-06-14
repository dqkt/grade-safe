package com.example.dq.gradesafe;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
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
import java.util.Objects;

/**
 * Created by DQ on 3/19/2018.
 */

@Entity
public class Year implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int yearID;
    private String name;

    private int listIndex;

    Year(String name) {
        this.name = name;
    }

    public int getYearID() {
        return yearID;
    }
    public void setYearID(int yearID) {
        this.yearID = yearID;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getListIndex() {
        return listIndex;
    }
    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }

    public boolean equals(final Year otherYear) {
        return Objects.equals(name, otherYear.name);
    }

    @Dao
    public interface YearDao {
        @Insert
        void insertAll(Year... years);

        @Delete
        void delete(Year year);

        @Update
        void update(Year year);

        @Query("SELECT * FROM year ORDER BY listIndex ASC")
        LiveData<List<Year>> getAllYears();

        @Query("SELECT * FROM year ORDER BY listIndex ASC")
        List<Year> getAllCurrentYears();
    }
}
