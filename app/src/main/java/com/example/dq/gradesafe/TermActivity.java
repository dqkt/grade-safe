package com.example.dq.gradesafe;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by DQ on 3/31/2018.
 */

public class TermActivity extends AppCompatActivity {

    public static final String SELECTED_TERM_KEY = "SELECTED_TERM";

    private ActionBar actionBar;

    private Term term;

    private ReactiveRecyclerView courseRecyclerView;
    private CourseReactiveRecyclerViewAdapter courseRecyclerViewAdapter;

    private FloatingActionButton addCourseButton;
    private LinearLayout addCourseDialogLayout;
    private EditText newCourseName;
    private EditText newCourseCredits;
    private CheckBox newCourseAffectsGPA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term);

        term = (Term) getIntent().getSerializableExtra(SELECTED_TERM_KEY);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        setTitle(term.getName());
    }

    @Override
    protected void onResume() {
        setUpCoursesArea();

        super.onResume();
    }

    @Override
    protected void onPause() {
        term.saveCourses(this);

        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setUpCoursesArea() {
        term.loadCourses(this);

        courseRecyclerView = (ReactiveRecyclerView) findViewById(R.id.recyclerview_courses);

        courseRecyclerViewAdapter = new CourseReactiveRecyclerViewAdapter(this, term.getCourses(), courseRecyclerView);
        courseRecyclerView.setAdapter(courseRecyclerViewAdapter);
        LinearLayoutManager coursesLinearLayoutManager = new LinearLayoutManager(this);
        coursesLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        courseRecyclerView.setLayoutManager(coursesLinearLayoutManager);
        courseRecyclerViewAdapter.notifyDataSetChanged();

        if (addCourseButton == null) {
            addCourseButton = (FloatingActionButton) findViewById(R.id.button_add_course);

            addCourseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(TermActivity.this);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    LayoutInflater inflater = (LayoutInflater) TermActivity.this.getSystemService(OverviewActivity.LAYOUT_INFLATER_SERVICE);
                    View view = inflater.inflate(R.layout.dialog_add_course, null);

                    addCourseDialogLayout = (LinearLayout) view.findViewById(R.id.layout_add_course);

                    final TextInputLayout courseNameLayout = (TextInputLayout) addCourseDialogLayout.findViewById(R.id.textinput_course_name);
                    final TextInputLayout courseCreditsLayout = (TextInputLayout) addCourseDialogLayout.findViewById(R.id.textinput_num_credits);
                    courseNameLayout.setErrorEnabled(true);
                    courseCreditsLayout.setErrorEnabled(true);
                    newCourseName = (TextInputEditText) courseNameLayout.findViewById(R.id.edittext_course_name);
                    newCourseCredits = (TextInputEditText) courseCreditsLayout.findViewById(R.id.edittext_num_credits);
                    newCourseAffectsGPA = (CheckBox) view.findViewById(R.id.checkbox_affects_gpa);
                    builder.setView(addCourseDialogLayout);

                    final AlertDialog dialog = builder.create();

                    dialog.setTitle("Add a course");
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    dialog.setCanceledOnTouchOutside(true);

                    dialog.show();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String courseName = newCourseName.getText().toString().replaceAll("^\\s+|\\s+$", "");
                            String courseCredits = newCourseCredits.getText().toString().replaceAll("^\\s+|\\s+$", "");
                            boolean countsTowardGPA = newCourseAffectsGPA.isChecked();
                            boolean valid = true;

                            if (courseName.isEmpty()) {
                                valid = false;
                                newCourseName.setError("Name cannot be blank");
                            }

                            if (courseCredits.isEmpty()) {
                                valid = false;
                                newCourseCredits.setError("Empty field");
                            }

                            ArrayList<Course> courseList = term.getCourses();
                            for (Course course : courseList) {
                                if (course.getName().equals(courseName)) {
                                    valid = false;
                                    newCourseName.setError("Name already exists");
                                    break;
                                }
                            }

                            if (valid) {
                                Course newCourse = new Course(courseName, Integer.parseInt(courseCredits), countsTowardGPA);
                                term.addCourse(newCourse);
                                courseRecyclerViewAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });
        }
    }
}
