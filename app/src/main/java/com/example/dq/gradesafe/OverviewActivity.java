package com.example.dq.gradesafe;

import android.animation.Animator;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OverviewActivity extends AppCompatActivity
        implements YearReactiveRecyclerViewAdapter.YearActionCallback {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.6f;
    private static final float PERCENTAGE_TO_SHOW_ADD_BUTTON        = 0.9f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private List<GradingScale> gradingScales;

    private GradingScaleListViewModel gradingScaleListViewModel;
    private YearListViewModel yearListViewModel;
    private Observer<List<Year>> yearListObserver;
    private CourseListViewModel courseListViewModel;
    private Observer<List<Course>> courseListObserver;

    private RelativeLayout addYearButtonCollapsed;

    private TextView noYearsView;
    private ReactiveRecyclerView yearRecyclerView;
    private YearReactiveRecyclerViewAdapter yearRecyclerViewAdapter;

    private FloatingActionButton addYearButton;
    private RelativeLayout addYearDialogLayout;
    private EditText newYearName;

    private Toolbar overviewToolbar;
    private AppBarLayout appBarLayout;

    private TextView title;
    private FrameLayout titleContainer;

    private TextView overallGpa;
    private TextView overallNumCredits;

    private boolean isTheTitleVisible;
    private boolean isTheAddButtonVisible;

    private DrawerLayout mainDrawer;
    private ActionBarDrawerToggle mainDrawerToggle;

    private DecimalFormat creditsFormatter;
    private DecimalFormat gpaFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        setTitle("");

        setUpToolbar();
        setUpNavigationDrawer();
        setUpOverallSummary();
        setUpYearsArea();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_button_add_year: {
                addYear();
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

    private void setUpToolbar() {
        isTheTitleVisible = false;

        overviewToolbar = findViewById(R.id.action_bar_overview);
        overviewToolbar.inflateMenu(R.menu.menu_overview);

        setSupportActionBar(overviewToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appBarLayout = findViewById(R.id.overview_appbar_container);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int maxScroll = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / ((float) maxScroll);

                titleContainer.setAlpha(1 - percentage);
                handleToolbarTitleVisibility(percentage);
                handleAddButtonVisibility(percentage);
            }
        });
    }

    private void setUpNavigationDrawer() {
        mainDrawer = findViewById(R.id.drawer_layout);
        mainDrawerToggle = new ActionBarDrawerToggle(this, mainDrawer, overviewToolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mainDrawer.addDrawerListener(mainDrawerToggle);

        mainDrawer.post(new Runnable() {
            @Override
            public void run() {
                mainDrawerToggle.syncState();
            }
        });
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (!isTheTitleVisible) {
                startAlphaAnimation(title, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                isTheTitleVisible = true;
            }
        } else {
            if (isTheTitleVisible) {
                startAlphaAnimation(title, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                isTheTitleVisible = false;
            }
        }
    }

    private void handleAddButtonVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_ADD_BUTTON) {
            if (!isTheAddButtonVisible) {
                startAlphaAnimation(addYearButtonCollapsed, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                isTheAddButtonVisible = true;
            }
        } else {
            if (isTheAddButtonVisible) {
                startAlphaAnimation(addYearButtonCollapsed, ALPHA_ANIMATIONS_DURATION, View.GONE);
                isTheAddButtonVisible = false;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    private void setUpOverallSummary() {
        overallGpa = (TextView) findViewById(R.id.textview_overall_gpa);
        overallNumCredits = (TextView) findViewById(R.id.textview_overall_num_credits);

        this.creditsFormatter = new DecimalFormat("0.#");
        this.gpaFormatter = new DecimalFormat("0.000 'GPA'");

        title = (TextView) findViewById(R.id.overview_title);
        titleContainer = (FrameLayout) findViewById(R.id.overview_expanded_title);

        startAlphaAnimation(title, 0, View.INVISIBLE);
    }

    private void updateYearsArea() {
        if (yearRecyclerViewAdapter.getItemCount() > 0) {
            yearRecyclerView.setVisibility(View.VISIBLE);
            noYearsView.setVisibility(View.GONE);
        } else {
            yearRecyclerView.setVisibility(View.GONE);
            noYearsView.setVisibility(View.VISIBLE);
        }
    }

    private void updateOverallSummary(List<Course> courses) {
        double contributionTowardGpa = 0;
        double overallTotalNumCredits = 0, overallTotalNumCreditsContributing = 0;
        GradingScale gradingScale = GradingScale.createStandardGradingScale();
        double numCredits;
        if (courses != null) {
            for (Course course : courses) {
                numCredits = course.getNumCredits();
                if (course.countsTowardGPA()) {
                    contributionTowardGpa += gradingScale.getScoreRange(course.getOverallScore()).getContribution() * numCredits;
                    overallTotalNumCreditsContributing += numCredits;
                }
                overallTotalNumCredits += numCredits;
            }
        }

        if (overallTotalNumCredits != 0) {
            String titleText = gpaFormatter.format(contributionTowardGpa / overallTotalNumCreditsContributing);
            overallGpa.setText(titleText);
        } else {
            overallGpa.setText("0.000 GPA");
        }
        if (overallTotalNumCredits == 1) {
            overallNumCredits.setText("1 credit");
        } else {
            overallNumCredits.setText(String.valueOf(creditsFormatter.format(overallTotalNumCredits) + " credits"));
        }
        title.setText("Career Overview");
    }

    private void setUpYearsArea() {
        isTheAddButtonVisible = false;

        addYearButtonCollapsed = (RelativeLayout) findViewById(R.id.collapsed_button_add_year);
        addYearButtonCollapsed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addYear();
            }
        });
        startAlphaAnimation(addYearButtonCollapsed, 0, View.GONE);

        noYearsView = (TextView) findViewById(R.id.textview_no_years);
        yearRecyclerView = (ReactiveRecyclerView) findViewById(R.id.recyclerview_years);

        yearListViewModel = ViewModelProviders.of(this).get(YearListViewModel.class);
        courseListViewModel = ViewModelProviders.of(this).get(CourseListViewModel.class);
        gradingScaleListViewModel = ViewModelProviders.of(this).get(GradingScaleListViewModel.class);

        gradingScaleListViewModel.addGradingScale(GradingScale.createStandardGradingScale());
        gradingScaleListViewModel.getGradingScaleList().observe(this, new Observer<List<GradingScale>>() {
            @Override
            public void onChanged(@Nullable List<GradingScale> gradingScales) {
                OverviewActivity.this.gradingScales = gradingScales;
            }
        });

        yearRecyclerViewAdapter = new YearReactiveRecyclerViewAdapter(this, new ArrayList<Year>(), yearRecyclerView, yearListViewModel, this);
        yearRecyclerView.setAdapter(yearRecyclerViewAdapter);
        LinearLayoutManager yearsLinearLayoutManager = new LinearLayoutManager(this);
        yearsLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        yearRecyclerView.setLayoutManager(yearsLinearLayoutManager);

        yearListObserver = new Observer<List<Year>>() {
            @Override
            public void onChanged(@Nullable List<Year> years) {
                yearRecyclerViewAdapter.updateYears(years);
                updateYearsArea();
            }
        };
        yearListViewModel.getYearList().observe(this, yearListObserver);

        courseListObserver = new Observer<List<Course>>() {
            @Override
            public void onChanged(@Nullable List<Course> courses) {
                updateOverallSummary(courses);
            }
        };
        courseListViewModel.getCourseList().observe(this, courseListObserver);

        addYearButton = (FloatingActionButton) findViewById(R.id.button_add_year);
        addYearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addYear();
            }
        });
    }

    @Override
    public void modifyYear(Year year, String yearName) {
        year.setName(yearName);
        yearListViewModel.updateYear(year);
    }

    @Override
    public void deleteYear(Year year) {
        yearListViewModel.removeYear(year);
    }

    public void addYear() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this);

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

        LayoutInflater inflater = (LayoutInflater) OverviewActivity.this.getSystemService(OverviewActivity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_add_year, null);

        addYearDialogLayout = (RelativeLayout) view.findViewById(R.id.layout_add_year);

        final TextInputLayout addNewItemLayout = (TextInputLayout) addYearDialogLayout.findViewById(R.id.textinput_add_year);
        newYearName = (TextInputEditText) addNewItemLayout.findViewById(R.id.edittext_year_name);
        addNewItemLayout.setErrorEnabled(true);
        builder.setView(addYearDialogLayout);

        final AlertDialog dialog = builder.create();

        dialog.setTitle("Add a year");
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.setCanceledOnTouchOutside(true);

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String yearName = newYearName.getText().toString().replaceAll("^\\s+|\\s+$", "");
                boolean valid = true;

                if (yearName.isEmpty()) {
                    valid = false;
                    addNewItemLayout.setError("Name cannot be blank");
                }

                if (valid) {
                    Year newYear = new Year(yearName);
                    newYear.setListIndex(yearRecyclerViewAdapter.getItemCount());
                    yearListViewModel.addYear(newYear);
                    dialog.dismiss();
                }
            }
        });
    }
}