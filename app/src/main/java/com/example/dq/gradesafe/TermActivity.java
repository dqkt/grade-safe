package com.example.dq.gradesafe;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
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

public class TermActivity extends AppCompatActivity {

    static final String SELECTED_TERM_KEY = "SELECTED_TERM";
    static final String CORRESPONDING_YEAR_KEY = "CORRESPONDING_YEAR";

    private Toolbar termToolbar;
    private Menu menu;
    private AppBarLayout appBarLayout;

    private TextView title;
    private LinearLayout titleContainer;
    private boolean isTheTitleVisible;

    private List<GradingScale> gradingScales;

    private GradingScaleListViewModel gradingScaleListViewModel;
    private TermListViewModel termListViewModel;
    private CourseListViewModel courseListViewModel;
    private Observer<List<Course>> courseListObserver;

    private Term term;
    private Year year;

    private RelativeLayout editTermDialogLayout;
    private EditText newTermName;

    private TextView termName;
    private TextView yearName;
    private TextView termGpa;
    private TextView termNumCredits;

    private DecimalFormat gpaFormatter;
    private DecimalFormat numCreditsFormatter;

    private GradingScale gradingScale;

    private TextView noCoursesView;
    private RecyclerView courseRecyclerView;
    private CourseRecyclerViewAdapter courseRecyclerViewAdapter;

    private FloatingActionButton addCourseButton;
    private LinearLayout addCourseDialogLayout;
    private EditText newCourseName;
    private EditText newCourseCredits;
    private CheckBox newCourseAffectsGPA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term);

        termListViewModel = ViewModelProviders.of(this).get(TermListViewModel.class);

        Intent intent = getIntent();
        term = (Term) intent.getSerializableExtra(SELECTED_TERM_KEY);
        year = (Year) intent.getSerializableExtra(CORRESPONDING_YEAR_KEY);

        setUpToolbar();
        setUpSummary();
        setUpCoursesArea();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_term, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.edit_term: {
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
                View view = inflater.inflate(R.layout.dialog_add_term, null);

                editTermDialogLayout = (RelativeLayout) view.findViewById(R.id.layout_add_term);

                String termName = term.getName();

                final TextInputLayout addNewItemLayout = (TextInputLayout) editTermDialogLayout.findViewById(R.id.textinput_add_term);
                newTermName = (TextInputEditText) addNewItemLayout.findViewById(R.id.edittext_term_name);
                newTermName.setText(termName);
                builder.setView(editTermDialogLayout);

                final AlertDialog dialog = builder.create();

                if (year != null) {
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder("Modify " + termName);
                    StyleSpan termNameSpan = new StyleSpan(Typeface.BOLD);
                    int termNameLength = termName.length();
                    stringBuilder.setSpan(termNameSpan, 7, 7 + termNameLength, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    dialog.setTitle(stringBuilder);
                } else {
                    dialog.setTitle("Modify term");
                }
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.setCanceledOnTouchOutside(true);

                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String termName = newTermName.getText().toString().replaceAll("^\\s+|\\s+$", "");
                        boolean valid = true;

                        if (termName.isEmpty()) {
                            valid = false;
                        }

                        if (valid) {
                            term.setName(termName);
                            title.setText(termName);
                            termListViewModel.updateTerm(term);
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
            case R.id.delete_term: {
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

                if (term != null && year != null) {
                    String termName = term.getName();
                    String yearName = year.getName();
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder("Are you sure you want to delete " + termName + " and its courses from " + yearName + "?");
                    StyleSpan termNameSpan = new StyleSpan(Typeface.BOLD);
                    StyleSpan yearNameSpan = new StyleSpan(Typeface.BOLD);
                    int termNameLength = termName.length();
                    stringBuilder.setSpan(termNameSpan, 32, 32 + termNameLength, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    stringBuilder.setSpan(yearNameSpan, 54 + termNameLength, 54 + termNameLength + yearName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    message.setText(stringBuilder);
                } else {
                    message.setText(String.valueOf("Are you sure you want to delete this term and its courses from " + year.getName() + "?"));
                }
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.setCanceledOnTouchOutside(true);

                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        termListViewModel.removeTerm(term);
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
            case R.id.add_course: {
                addCourse();
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

        termGpa = findViewById(R.id.textview_term_gpa);
        termNumCredits = findViewById(R.id.textview_term_num_credits);

        gpaFormatter = new DecimalFormat("0.000");
        numCreditsFormatter = new DecimalFormat("0.#");
    }

    private void updateSummary(boolean gradesExist) {
        if (courseRecyclerViewAdapter.getItemCount() > 0) {
            courseRecyclerView.setVisibility(View.VISIBLE);
            noCoursesView.setVisibility(View.GONE);
        } else {
            courseRecyclerView.setVisibility(View.GONE);
            noCoursesView.setVisibility(View.VISIBLE);
        }

        termGpa.setText(gpaFormatter.format(term.getGpa()));

        double totalNumCredits = term.getTotalNumCredits();
        if (totalNumCredits == 1) {
            termNumCredits.setText("1 credit");
        } else {
            termNumCredits.setText(String.valueOf(numCreditsFormatter.format(totalNumCredits) + " credits"));
        }
    }

    private void setUpToolbar() {
        title = findViewById(R.id.term_title);
        titleContainer = findViewById(R.id.term_expanded_title);
        isTheTitleVisible = false;

        setTitle("");
        title.setText(term.getName());

        termToolbar = findViewById(R.id.action_bar_term);
        termToolbar.inflateMenu(R.menu.menu_term);

        setSupportActionBar(termToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        appBarLayout = findViewById(R.id.term_appbar_container);

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
        noCoursesView = (TextView) findViewById(R.id.textview_no_courses);
        courseRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_courses);

        gradingScaleListViewModel = ViewModelProviders.of(this).get(GradingScaleListViewModel.class);
        gradingScaleListViewModel.getGradingScaleList().observe(this, new Observer<List<GradingScale>>() {
            @Override
            public void onChanged(@Nullable List<GradingScale> gradingScales) {
                TermActivity.this.gradingScales = gradingScales;
            }
        });

        courseListViewModel = ViewModelProviders.of(this).get(CourseListViewModel.class);
        courseListObserver = new Observer<List<Course>>() {
            @Override
            public void onChanged(@Nullable List<Course> courses) {
                courseRecyclerViewAdapter.updateCourses(courses);
                term.setCourses(courses, GradingScale.createStandardGradingScale());
                termListViewModel.updateTerm(term);
                int numCourses = 0;
                if (courses != null) {
                    numCourses = courses.size();
                }
                boolean gradesExist = false;
                for (int i = 0; i < numCourses; i++) {
                    if (courses.get(i).getOverallGrade() != null){
                        gradesExist = true;
                        break;
                    }
                }
                updateSummary(gradesExist);
            }
        };
        courseListViewModel.getCoursesInTerm(term).observe(TermActivity.this, courseListObserver);

        courseRecyclerViewAdapter = new CourseRecyclerViewAdapter(this, new ArrayList<Course>(), courseListViewModel, term);
        courseRecyclerView.setAdapter(courseRecyclerViewAdapter);
        LinearLayoutManager coursesLinearLayoutManager = new LinearLayoutManager(this) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (IndexOutOfBoundsException e) {
                    Log.e("DEBUG", "IndexOutOfBoundsException in RecyclerView.");
                }
            }
        };
        coursesLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        courseRecyclerView.setLayoutManager(coursesLinearLayoutManager);
        SwipeUtil swipeUtil = new SwipeUtil(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END, this) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                courseRecyclerViewAdapter.onMove(viewHolder, target);
                return false;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                courseRecyclerViewAdapter.onSwiped(viewHolder, direction);
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
        itemTouchHelper.attachToRecyclerView(courseRecyclerView);

        if (addCourseButton == null) {
            addCourseButton = (FloatingActionButton) findViewById(R.id.button_add_course);

            addCourseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addCourse();
                }
            });
        }
    }

    private void addCourse() {
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

                if (valid) {
                    double numCourseCredits = Double.parseDouble(courseCredits);
                    Course newCourse = new Course(courseName, numCourseCredits, countsTowardGPA, term.getTermID());
                    newCourse.setListIndex(courseRecyclerViewAdapter.getItemCount());
                    courseListViewModel.addCourse(newCourse);
                    dialog.dismiss();
                }
            }
        });
    }
}
