package com.example.dq.gradesafe;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DQ on 3/31/2018.
 */

public class TermActivity extends AppCompatActivity {

    static final String SELECTED_TERM_KEY = "SELECTED_TERM";
    static final String CORRESPONDING_YEAR_KEY = "CORRESPONDING_YEAR";

    private ActionBar actionBar;

    private TermListViewModel termListViewModel;
    private CourseListViewModel courseListViewModel;
    private Observer<List<Course>> courseListObserver;

    private Term term;
    private Year year;

    private RelativeLayout editTermDialogLayout;
    private EditText newTermName;

    private TextView termName;
    private TextView termGpa;
    private TextView termNumCredits;

    private DecimalFormat gpaFormatter;
    private DecimalFormat numCreditsFormatter;

    private RecyclerView courseRecyclerView;
    private CourseRecyclerViewAdapter courseRecyclerViewAdapter;

    private FloatingActionButton addCourseButton;
    private LinearLayout addCourseDialogLayout;
    private EditText newCourseName;
    private EditText newCourseCredits;
    private CheckBox newCourseAffectsGPA;

    private int NUM_SLIDES = 5;

    private ViewPager termsPager;
    private ScrollView pagerScrollView;

    private PagerAdapter slideshowPagerAdapter;
    private Field slideshowScroller;

    private int currentSlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term);

        termListViewModel = ViewModelProviders.of(this).get(TermListViewModel.class);

        Intent intent = getIntent();
        term = (Term) intent.getSerializableExtra(SELECTED_TERM_KEY);
        year = (Year) intent.getSerializableExtra(CORRESPONDING_YEAR_KEY);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        setUpTermsPager();
        setUpSummary();
        setUpCoursesArea();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_term, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);

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
                    dialog.setTitle("Edit term");
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
                            setTitle(termName);
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
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        courseListViewModel.getCoursesInTerm(term).observe(this, courseListObserver);

        super.onResume();
    }

    @Override
    protected void onPause() {
        courseListViewModel.getCoursesInTerm(term).removeObservers(this);
        courseRecyclerViewAdapter.saveRearrangedCourses();

        super.onPause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setUpSummary() {
    }

    private void updateSummary(boolean allCoursesHaveGrades) {
        termGpa.setText(gpaFormatter.format(term.getGpa()));
        if (!allCoursesHaveGrades) {
            termGpa.setAlpha(0.5f);
        } else {
            termGpa.setAlpha(1.0f);
        }
        double totalNumCredits = term.getTotalNumCredits();
        if (totalNumCredits == 1) {
            termNumCredits.setText("1 credit");
        } else {
            termNumCredits.setText(String.valueOf(numCreditsFormatter.format(totalNumCredits) + " credits"));
        }
    }

    private void setUpTermsPager() {
        termsPager = (ViewPager) findViewById(R.id.pager_slideshow);
        termsPager.setPageTransformer(true, new ZoomOutPageTransformer());
        slideshowPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        termsPager.setAdapter(slideshowPagerAdapter);

        View slideLayout = LayoutInflater.from(this).inflate(R.layout.layout_term_page, null);

        pagerScrollView = (ScrollView) slideLayout.findViewById(R.id.pager_content);

        gpaFormatter = new DecimalFormat("0.000 'GPA'");
        numCreditsFormatter = new DecimalFormat("0.#");

        termName = (TextView) pagerScrollView.findViewById(R.id.textview_name);
        termGpa = (TextView) pagerScrollView.findViewById(R.id.textview_gpa);
        termNumCredits = (TextView) pagerScrollView.findViewById(R.id.textview_num_credits);

        termName.setText(term.getName());
        termGpa.setText(gpaFormatter.format(term.getGpa()));
        termNumCredits.setText(numCreditsFormatter.format(term.getTotalNumCredits()));

        termsPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currentSlide = position;
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        try {
            slideshowScroller = ViewPager.class.getDeclaredField("mScroller");
            slideshowScroller.setAccessible(true);
            slideshowScroller.set(termsPager, new CustomScroller(this, new AccelerateDecelerateInterpolator(), 500));
        } catch (Exception e) {
            e.printStackTrace();
        }

        currentSlide = 0;
    }

    private void setUpCoursesArea() {
        courseRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_courses);

        courseListViewModel = ViewModelProviders.of(this).get(CourseListViewModel.class);

        courseRecyclerViewAdapter = new CourseRecyclerViewAdapter(this, new ArrayList<Course>(), courseListViewModel);
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

        courseListObserver = new Observer<List<Course>>() {

            @Override
            public void onChanged(@Nullable List<Course> courses) {
                courseRecyclerViewAdapter.updateCourses(courses);
                term.setCourses(courses);
                termListViewModel.updateTerm(term);
                int numCourses = 0;
                if (courses != null) {
                    numCourses = courses.size();
                }
                boolean allCoursesHaveGrades = numCourses != 0;
                for (int i = 0; i < numCourses; i++) {
                    if (courses.get(i).getOverallGrade() == null){
                        allCoursesHaveGrades = false;
                        break;
                    }
                }
                // updateSummary(allCoursesHaveGrades);
            }
        };

        courseListViewModel.getCoursesInTerm(term).observe(TermActivity.this, courseListObserver);

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
                                courseRecyclerViewAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });
        }
    }

    private class SlidePagerAdapter extends FragmentStatePagerAdapter {
        SlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new SlidePageFragment();
        }

        @Override
        public int getCount() {
            return NUM_SLIDES;
        }
    }
}
