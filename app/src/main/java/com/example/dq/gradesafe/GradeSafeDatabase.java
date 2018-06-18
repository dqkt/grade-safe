package com.example.dq.gradesafe;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

@Database(entities = {Year.class, Term.class, Course.class, Assignment.class, GradingScale.class}, version = 7)
public abstract class GradeSafeDatabase extends RoomDatabase {
    private static GradeSafeDatabase INSTANCE;

    public static GradeSafeDatabase getGradeSafeDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), GradeSafeDatabase.class, "grades-database")
                    .fallbackToDestructiveMigration()
                    .addMigrations()
                    .build();
        }
        return INSTANCE;
    }

/*    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS terms_new (termID INTEGER NOT NULL, name TEXT, totalNumCredits REAL NOT NULL, gpa REAL NOT NULL, " +
                    "yearID INTEGER NOT NULL, listIndex INTEGER NOT NULL, PRIMARY KEY(termID), FOREIGN KEY(yearID) REFERENCES Year(yearID) ON DELETE CASCADE ON UPDATE CASCADE)");
            database.execSQL("INSERT INTO terms_new (termID, name, totalNumCredits, gpa, yearID, listIndex) " +
                    "SELECT termID, name, totalNumCredits, gpa, yearID, listIndex FROM term");
            database.execSQL("DROP TABLE term");
            database.execSQL("ALTER TABLE terms_new RENAME TO term");

            database.execSQL("ALTER TABLE course ADD COLUMN gradingScaleName TEXT DEFAULT 'Standard Grading Scale' REFERENCES GradingScale(name) ON UPDATE CASCADE");
        }
    };*/

/*    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS gradingScale (gradingScaleID INTEGER NOT NULL, name TEXT, scoreRanges TEXT, PRIMARY KEY(gradingScaleID))");

            database.execSQL("CREATE TABLE IF NOT EXISTS terms_new (termID INTEGER NOT NULL, name TEXT, totalNumCredits REAL NOT NULL, gpa REAL NOT NULL, " +
                    "yearID INTEGER NOT NULL, gradingScaleID INTEGER NOT NULL DEFAULT 0, listIndex INTEGER NOT NULL, PRIMARY KEY(termID), FOREIGN KEY(yearID) REFERENCES Year(yearID) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY(gradingScaleID) REFERENCES GradingScale(gradingScaleID) ON UPDATE CASCADE)");
            database.execSQL("INSERT INTO terms_new (termID, name, totalNumCredits, gpa, yearID, listIndex) " +
                    "SELECT termID, name, totalNumCredits, gpa, yearID, listIndex FROM term");
            database.execSQL("DROP TABLE term");
            database.execSQL("ALTER TABLE terms_new RENAME TO term");
        }
    };*/

    public static void destroyGradeSafeDatabase() {
        INSTANCE = null;
    }

    public abstract Year.YearDao yearModel();
    public abstract Term.TermDao termModel();
    public abstract Course.CourseDao courseModel();
    public abstract Assignment.AssignmentDao assignmentModel();
    public abstract GradingScale.GradingScaleDao gradingScaleModel();
}
