package com.example.dq.gradesafe;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DQ on 3/31/2018.
 */

public class CourseActivity extends AppCompatActivity {

    static final String SELECTED_COURSE_KEY = "SELECTED_COURSE";
    static final String CORRESPONDING_TERM_KEY = "CORRESPONDING_TERM";

    private Toolbar courseToolbar;
    private Menu menu;
    private AppBarLayout appBarLayout;

    private TextView title;
    private RelativeLayout titleContainer;
    private boolean isTheTitleVisible;

    private CourseListViewModel courseListViewModel;
    private AssignmentListViewModel assignmentListViewModel;
    private Observer<List<Assignment>> assignmentListObserver;

    private Course course;
    private Term term;

    private LinearLayout editCourseDialogLayout;
    private EditText newCourseName;
    private EditText newNumCourseCredits;

    private TextView termName;
    private TextView yearName;
    private TextView courseGrade;
    private TextView courseScore;

    private DecimalFormat scoreFormatter;

    private RecyclerView assignmentRecyclerView;
    private AssignmentRecyclerViewAdapter assignmentRecyclerViewAdapter;

    private FloatingActionButton addAssignmentButton;
    private LinearLayout addAssignmentDialogLayout;
    private EditText newAssignmentName;
    private EditText newAssignmentWeight;
    private EditText newAssignmentScoreNumerator;
    private EditText newAssignmentScoreDenominator;
    private CheckBox newAssignmentComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        courseListViewModel = ViewModelProviders.of(this).get(CourseListViewModel.class);

        Intent intent = getIntent();
        course = (Course) intent.getSerializableExtra(SELECTED_COURSE_KEY);
        term = (Term) intent.getSerializableExtra(CORRESPONDING_TERM_KEY);

        setUpToolbar();
        setUpSummary();
        setUpCoursesArea();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.edit_course: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

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

                LayoutInflater inflater = (LayoutInflater) getSystemService(OverviewActivity.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_add_course, null);

                editCourseDialogLayout = (LinearLayout) view.findViewById(R.id.layout_add_course);

                String courseName = course.getName();
                double numCourseCredits = course.getNumCredits();

                final TextInputLayout newCourseNameLayout = (TextInputLayout) editCourseDialogLayout.findViewById(R.id.textinput_course_name);
                final TextInputLayout newNumCourseCreditsLayout = (TextInputLayout) editCourseDialogLayout.findViewById(R.id.textinput_num_credits);
                newCourseName = (TextInputEditText) newCourseNameLayout.findViewById(R.id.edittext_course_name);
                newNumCourseCredits = (TextInputEditText) newNumCourseCreditsLayout.findViewById(R.id.edittext_num_credits);
                newCourseName.setText(courseName);
                newNumCourseCredits.setText(String.valueOf(numCourseCredits));
                builder.setView(editCourseDialogLayout);

                final AlertDialog dialog = builder.create();

                if (term != null) {
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder("Modify " + courseName);
                    StyleSpan courseNameSpan = new StyleSpan(Typeface.BOLD);
                    int courseNameLength = courseName.length();
                    stringBuilder.setSpan(courseNameSpan, 7, 7 + courseNameLength, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    dialog.setTitle(stringBuilder);
                } else {
                    dialog.setTitle("Modify course");
                }
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.setCanceledOnTouchOutside(true);

                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String courseName = newCourseName.getText().toString().replaceAll("^\\s+|\\s+$", "");
                        boolean valid = true;

                        if (courseName.isEmpty()) {
                            valid = false;
                        }

                        if (valid) {
                            course.setName(courseName);
                            setTitle(courseName);
                            courseListViewModel.updateCourse(course);
                            dialog.dismiss();
                        }
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                return true;
            }
            case R.id.delete_course: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                LayoutInflater inflater = (LayoutInflater) getSystemService(OverviewActivity.LAYOUT_INFLATER_SERVICE);
                RelativeLayout confirmationDialogLayout = (RelativeLayout) inflater.inflate(R.layout.confirmation_dialog, null);

                TextView message = (TextView) confirmationDialogLayout.findViewById(R.id.textview_message);
                builder.setView(confirmationDialogLayout);

                final AlertDialog dialog = builder.create();

                if (course != null && term != null) {
                    String courseName = course.getName();
                    String termName = term.getName();
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder("Are you sure you want to delete " + courseName + " and its assignments from " + termName + "?");
                    StyleSpan courseNameSpan = new StyleSpan(Typeface.BOLD);
                    StyleSpan termNameSpan = new StyleSpan(Typeface.BOLD);
                    int courseNameLength = courseName.length();
                    stringBuilder.setSpan(courseNameSpan, 32, 32 + courseNameLength, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    stringBuilder.setSpan(termNameSpan, 58 + courseNameLength, 58 + courseNameLength + termName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    message.setText(stringBuilder);
                } else {
                    message.setText(String.valueOf("Are you sure you want to delete this course and its assignments from " + term.getName() + "?"));
                }
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.setCanceledOnTouchOutside(true);

                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        courseListViewModel.removeCourse(course);
                        onBackPressed();
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                return true;
            }
            case R.id.add_assignment: {
                addAssignment();
                return true;
            }
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setUpSummary() {
        // termName = findViewById(R.id.textview_term_name);
        // yearName = findViewById(R.id.textview_year_name);

        // termName.setText(term.getName());
        // yearName.setText(year.getName());

        courseGrade = findViewById(R.id.textview_course_grade);
        courseScore = findViewById(R.id.textview_course_score);

        scoreFormatter = new DecimalFormat("0.00");
    }

    private void updateSummary(boolean scoresExist) {
        course.updateOverallScore();
        course.updateOverallGrade(GradingScale.createStandardGradingScale());
        courseGrade.setText(course.getOverallGrade());
        courseScore.setText(scoreFormatter.format(course.getOverallScore()));
    }

    private void setUpToolbar() {
        title = findViewById(R.id.course_title);
        titleContainer = findViewById(R.id.course_expanded_title);
        isTheTitleVisible = false;

        setTitle("");
        title.setText(course.getName());

        courseToolbar = findViewById(R.id.action_bar_course);
        courseToolbar.inflateMenu(R.menu.menu_course);

        setSupportActionBar(courseToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        appBarLayout = findViewById(R.id.course_appbar_container);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                int maxScroll = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / ((float) (maxScroll * 0.6));

                titleContainer.setAlpha(1 - percentage);
            }
        });
    }

    private void setUpCoursesArea() {
        assignmentRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_assignments);

        assignmentListViewModel = ViewModelProviders.of(this).get(AssignmentListViewModel.class);
        assignmentListObserver = new Observer<List<Assignment>>() {
            @Override
            public void onChanged(@Nullable List<Assignment> assignments) {
                assignmentRecyclerViewAdapter.updateAssignments(assignments);
                course.setAssignments(assignments);
                courseListViewModel.updateCourse(course);
                int numAssignments = 0;
                if (assignments != null) {
                    numAssignments = assignments.size();
                }
                boolean gradesExist = false;
                for (int i = 0; i < numAssignments; i++) {
                    if (assignments.get(i).getScoreDenominator() != 0){
                        gradesExist = true;
                        break;
                    }
                }
                updateSummary(gradesExist);
            }
        };
        assignmentListViewModel.getAssignmentsInCourse(course).observe(CourseActivity.this, assignmentListObserver);

        assignmentRecyclerViewAdapter = new AssignmentRecyclerViewAdapter(this, new ArrayList<Assignment>(), assignmentListViewModel, course);
        assignmentRecyclerView.setAdapter(assignmentRecyclerViewAdapter);
        LinearLayoutManager assignmentsLinearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    Log.e("DEBUG", "IndexOutOfBoundsException in RecyclerView.");
                }
            }
        };
        assignmentsLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        assignmentRecyclerView.setLayoutManager(assignmentsLinearLayoutManager);
        SwipeUtil swipeUtil = new SwipeUtil(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END, this) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                assignmentRecyclerViewAdapter.onMove(viewHolder, target);
                return false;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                assignmentRecyclerViewAdapter.onSwiped(viewHolder, direction);
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeUtil);
        itemTouchHelper.attachToRecyclerView(assignmentRecyclerView);

        if (addAssignmentButton == null) {
            addAssignmentButton = (FloatingActionButton) findViewById(R.id.button_add_assignment);

            addAssignmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addAssignment();
                }
            });
        }
    }

    private void addAssignment() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(CourseActivity.this);

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

        LayoutInflater inflater = (LayoutInflater) CourseActivity.this.getSystemService(OverviewActivity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_add_assignment, null);

        addAssignmentDialogLayout = (LinearLayout) view.findViewById(R.id.layout_add_assignment);

        final TextInputLayout assignmentNameLayout = (TextInputLayout) addAssignmentDialogLayout.findViewById(R.id.textinput_name);
        final TextInputLayout assignmentWeightLayout = (TextInputLayout) addAssignmentDialogLayout.findViewById(R.id.textinput_weight);
        final TextInputLayout assignmentScoreNumeratorLayout = (TextInputLayout) addAssignmentDialogLayout.findViewById(R.id.textinput_score_numerator);
        final TextInputLayout assignmentScoreDenominatorLayout = (TextInputLayout) addAssignmentDialogLayout.findViewById(R.id.textinput_score_denominator);
        assignmentNameLayout.setErrorEnabled(true);
        assignmentWeightLayout.setErrorEnabled(true);
        newAssignmentName = (TextInputEditText) assignmentNameLayout.findViewById(R.id.edittext_name);
        newAssignmentWeight = (TextInputEditText) assignmentWeightLayout.findViewById(R.id.edittext_weight);
        newAssignmentScoreNumerator = (TextInputEditText) assignmentScoreNumeratorLayout.findViewById(R.id.edittext_score_numerator);
        newAssignmentScoreDenominator = (TextInputEditText) assignmentScoreDenominatorLayout.findViewById(R.id.edittext_score_denominator);
        newAssignmentComplete = (CheckBox) view.findViewById(R.id.checkbox_complete);
        builder.setView(addAssignmentDialogLayout);

        final AlertDialog dialog = builder.create();

        dialog.setTitle("Add an assignment");
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.setCanceledOnTouchOutside(true);

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String assignmentName = newAssignmentName.getText().toString().replaceAll("^\\s+|\\s+$", "");
                String assignmentWeight = newAssignmentWeight.getText().toString().replaceAll("^\\s+|\\s+$", "");
                String assignmentScoreNumerator = newAssignmentScoreNumerator.getText().toString().replaceAll("^\\s+|\\s+$", "");
                String assignmentScoreDenominator = newAssignmentScoreDenominator.getText().toString().replaceAll("^\\s+|\\s+$", "");
                boolean assignmentComplete = newAssignmentComplete.isChecked();
                boolean valid = true;

                if (assignmentName.isEmpty()) {
                    valid = false;
                    newAssignmentName.setError("Name cannot be blank");
                }

                if (assignmentWeight.isEmpty()) {
                    valid = false;
                    newAssignmentWeight.setError("Empty field");
                }

                if (valid) {
                    double assignmentWeightValue = Double.parseDouble(assignmentWeight);
                    double assignmentScoreNumeratorValue = Double.parseDouble(assignmentScoreNumerator);
                    double assignmentScoreDenominatorValue = Double.parseDouble(assignmentScoreDenominator);
                    Assignment newAssignment = new Assignment(assignmentName, assignmentWeightValue, assignmentScoreNumeratorValue, assignmentScoreDenominatorValue, assignmentComplete, course.getCourseID());
                    newAssignment.setListIndex(assignmentRecyclerViewAdapter.getItemCount());
                    assignmentListViewModel.addAssignment(newAssignment);
                    course.updateOverallScore();
                    course.updateOverallGrade(GradingScale.createStandardGradingScale());
                    dialog.dismiss();
                }
            }
        });
    }
}
