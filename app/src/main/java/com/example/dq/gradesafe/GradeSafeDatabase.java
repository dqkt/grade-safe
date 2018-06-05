package com.example.dq.gradesafe;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Year.class, Term.class, Course.class, Assignment.class}, version = 3)
public abstract class GradeSafeDatabase extends RoomDatabase {
    private static GradeSafeDatabase INSTANCE;

    public static GradeSafeDatabase getGradeSafeDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), GradeSafeDatabase.class, "grades-database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyGradeSafeDatabase() {
        INSTANCE = null;
    }

    public abstract Year.YearDao yearModel();
    public abstract Term.TermDao termModel();
    public abstract Course.CourseDao courseModel();
    public abstract Assignment.AssignmentDao assignmentModel();
}
