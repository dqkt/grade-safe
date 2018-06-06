package com.example.dq.gradesafe;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OverviewActivity extends AppCompatActivity
        implements YearReactiveRecyclerViewAdapter.YearActionCallback {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.8f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private YearListViewModel yearListViewModel;
    private Observer<List<Year>> yearListObserver;
    private TermListViewModel termListViewModel;
    private Observer<List<Term>> termListObserver;

    private ReactiveRecyclerView yearRecyclerView;
    private YearReactiveRecyclerViewAdapter yearRecyclerViewAdapter;

    private FloatingActionButton addYearButton;
    private RelativeLayout addYearDialogLayout;
    private EditText newYearName;

    private Menu menu;

    private Toolbar overviewToolbar;
    private AppBarLayout appBarLayout;

    private TextView title;
    private FrameLayout titleContainer;

    private TextView overallGpa;
    private TextView overallNumCredits;

    private boolean isTheTitleVisible = false;

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
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return super.onCreateOptionsMenu(menu);
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
        overviewToolbar = findViewById(R.id.action_bar_overview);
        overviewToolbar.inflateMenu(R.menu.menu_overview);

        setSupportActionBar(overviewToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appBarLayout = findViewById(R.id.overview_appbar_container);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShowingOption = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                int maxScroll = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / ((float) (maxScroll * 0.8));

                titleContainer.setAlpha(1 - percentage);
                handleToolbarTitleVisibility(percentage);
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

    private void updateOverallSummary(List<Term> terms) {
        double contributionTowardGpa = 0;
        double overallTotalNumCredits = 0;
        if (terms != null) {
            for (Term term : terms) {
                contributionTowardGpa += term.getTotalContributionTowardGpa();
                overallTotalNumCredits += term.getTotalNumCredits();
            }
        }

        String titleText = gpaFormatter.format(contributionTowardGpa / overallTotalNumCredits);
        if (overallTotalNumCredits != 0) {
            overallGpa.setText(titleText);
            title.setText(titleText);
        } else {
            overallGpa.setText("0.000 GPA");
            title.setText("0.000 GPA");
        }
        if (overallTotalNumCredits == 1) {
            overallNumCredits.setText("1 credit");
        } else {
            overallNumCredits.setText(String.valueOf(creditsFormatter.format(overallTotalNumCredits) + " credits"));
        }
    }

    private void setUpYearsArea() {
        yearRecyclerView = (ReactiveRecyclerView) findViewById(R.id.recyclerview_years);
        yearListViewModel = ViewModelProviders.of(this).get(YearListViewModel.class);
        termListViewModel = ViewModelProviders.of(this).get(TermListViewModel.class);

        yearRecyclerViewAdapter = new YearReactiveRecyclerViewAdapter(this, new ArrayList<Year>(), yearRecyclerView, yearListViewModel, this);
        yearRecyclerView.setAdapter(yearRecyclerViewAdapter);
        LinearLayoutManager yearsLinearLayoutManager = new LinearLayoutManager(this);
        yearsLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        yearRecyclerView.setLayoutManager(yearsLinearLayoutManager);

        yearListObserver = new Observer<List<Year>>() {
            @Override
            public void onChanged(@Nullable List<Year> years) {
                yearRecyclerViewAdapter.updateYears(years);
            }
        };
        yearListViewModel.getYearList().observe(this, yearListObserver);

        termListObserver = new Observer<List<Term>>() {
            @Override
            public void onChanged(@Nullable List<Term> terms) {
                updateOverallSummary(terms);
            }
        };
        termListViewModel.getTermList().observe(this, termListObserver);

        addYearButton = (FloatingActionButton) findViewById(R.id.button_add_year);
        addYearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
}