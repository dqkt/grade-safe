package com.example.dq.gradesafe;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by DQ on 3/31/2018.
 */

public class CourseRecyclerViewAdapter extends RecyclerView.Adapter<CourseViewHolder> {

    static final int TYPE_HEADER = 0;
    static final int TYPE_ITEM = 1;

    protected Context context;
    protected LayoutInflater inflater;

    private List<Course> courses;
    private CourseListViewModel courseListViewModel;

    private Term term;

    public static final DecimalFormat scoreFormatter = new DecimalFormat("0.00");
    public static final DecimalFormat numCreditsFormatter = new DecimalFormat("0.#");

    protected boolean isBinding;

    CourseRecyclerViewAdapter(Context context, List<Course> courses, CourseListViewModel courseListViewModel, Term term) {
        // super(context, reactiveRecyclerView);
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        this.courses = courses;
        this.courseListViewModel = courseListViewModel;

        this.term = term;

        this.isBinding = false;

        setHasStableIds(true);
        // this.reactiveRecyclerView.setTouchCallback(this);
        // this.reactiveRecyclerView.setHeaderClass(CoursesHeader.class);
    }

    /*@Override
    public boolean isRearranging() {
        return isRearranging;
    }

    @Override
    public boolean hasHeader() {
        return false;
    }*/

    public void onMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        Course fromCourse, toCourse;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                fromCourse = courses.get(i);
                toCourse = courses.get(i + 1);
                fromCourse.setListIndex(i + 1);
                toCourse.setListIndex(i);
                courseListViewModel.updateCourse(fromCourse);
                courseListViewModel.updateCourse(toCourse);
                Collections.swap(courses, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                fromCourse = courses.get(i);
                toCourse = courses.get(i - 1);
                fromCourse.setListIndex(i - 1);
                toCourse.setListIndex(i);
                courseListViewModel.updateCourse(fromCourse);
                courseListViewModel.updateCourse(toCourse);
                Collections.swap(courses, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        final Course course = ((CourseViewHolder) viewHolder).getCourse();
        final int position = courses.indexOf(course);
        if (position != -1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

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

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(TermActivity.LAYOUT_INFLATER_SERVICE);
            RelativeLayout confirmationDialogLayout = (RelativeLayout) inflater.inflate(R.layout.confirmation_dialog, null);

            TextView message = (TextView) confirmationDialogLayout.findViewById(R.id.textview_message);
            builder.setView(confirmationDialogLayout);

            final AlertDialog dialog = builder.create();

            if (course != null && term != null) {
                String courseName = course.getName();
                String termName = term.getName();
                SpannableStringBuilder stringBuilder = new SpannableStringBuilder("Are you sure you want to delete " + courseName + " and its assignments from " + termName + "?");
                StyleSpan termNameSpan = new StyleSpan(Typeface.BOLD);
                StyleSpan yearNameSpan = new StyleSpan(Typeface.BOLD);
                int courseNameLength = courseName.length();
                stringBuilder.setSpan(termNameSpan, 32, 32 + courseNameLength, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                stringBuilder.setSpan(yearNameSpan, 58 + courseNameLength, 58 + courseNameLength + termName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                message.setText(stringBuilder);
            } else {
                message.setText(String.valueOf("Are you sure you want to delete this term and its courses from " + term.getName() + "?"));
            }
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.setCanceledOnTouchOutside(true);

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    courseListViewModel.removeCourse(course);
                    int numCourses = courses.size();
                    for (int i = position; i < numCourses; i++) {
                        courses.get(i).setListIndex(courses.get(i).getListIndex() - 1);
                    }
                }
            });

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notifyItemChanged(position);
                    dialog.dismiss();
                }
            });
        }
    }

    public void updateCourses(List<Course> courses) {
        Log.d("DEBUG", "update courses");
        DiffUtil.DiffResult difference = DiffUtil.calculateDiff(new CourseListDiffCallback(this.courses, courses));
        this.courses.clear();
        this.courses.addAll(courses);
        difference.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return courses.get(position).getCourseID();
    }

    @Override
    public CourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LinearLayout courseLayout = (LinearLayout) inflater.inflate(R.layout.layout_course, parent, false);
        return new CourseViewHolder(courseLayout, context, term);
    }

    @Override
    public void onBindViewHolder(CourseViewHolder holder, int position) {
        isBinding = true;
        // super.onBindViewHolder(holder, position);
        if (!(holder instanceof CoursesHeader)) {
            final Course currentCourse = courses.get(position);
            holder.updateViewHolder(currentCourse);
        } else {
            CoursesHeader coursesHeader = (CoursesHeader) holder;
        }
        isBinding = false;
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }
}

class CoursesHeader extends CourseViewHolder {

    public CoursesHeader(View view, Context context) {
        super(view);
    }
}

class CourseViewHolder extends RecyclerView.ViewHolder {

    private Context context;

    private Course course;
    private Term term;

    private LinearLayout overallLayout;
    private TextView name;
    private TextView fullName;
    private TextView numCredits;
    private LinearLayout resultsLayout;
    private TextView overallScore;
    private TextView overallGrade;

    public CourseViewHolder(View view) {
        super(view);
    }

    public CourseViewHolder(View view, final Context context, Term term) {
        // super(view, reactiveRecyclerView.getAdapter());
        super(view);

        this.context = context;

        this.term = term;

        overallLayout = (LinearLayout) view;
        name = (TextView) overallLayout.findViewById(R.id.textview_name);
        fullName = (TextView) overallLayout.findViewById(R.id.textview_full_name);
        numCredits = (TextView) overallLayout.findViewById(R.id.textview_num_credits);
        resultsLayout = (LinearLayout) overallLayout.findViewById(R.id.layout_results);
        overallScore = (TextView) overallLayout.findViewById(R.id.textview_score);
        overallGrade = (TextView) overallLayout.findViewById(R.id.textview_grade);

        overallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, CourseActivity.class);
                i.putExtra(CourseActivity.SELECTED_COURSE_KEY, course);
                i.putExtra(CourseActivity.CORRESPONDING_TERM_KEY, CourseViewHolder.this.term);
                /*ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context,
                        Pair.create((View) overallLayout, overallLayout.getTransitionName()));
                context.startActivity(i, options.toBundle());*/
                context.startActivity(i);
            }
        });
    }

    public void updateViewHolder(Course currentCourse) {
        course = currentCourse;
        name.setText(currentCourse.getName());

        if (currentCourse.getFullName() == null) {
            fullName.setVisibility(View.GONE);
        } else {
            fullName.setVisibility(View.VISIBLE);
            fullName.setText(currentCourse.getFullName());
        }

        double numCreditsValue = currentCourse.getNumCredits();
        if (numCreditsValue == 1) {
            numCredits.setText(String.valueOf(CourseRecyclerViewAdapter.numCreditsFormatter.format(numCreditsValue) + " credit"));
        } else {
            numCredits.setText(String.valueOf(CourseRecyclerViewAdapter.numCreditsFormatter.format(numCreditsValue) + " credits"));
        }
        String overallGradeValue;
        if ((overallGradeValue = currentCourse.getOverallGrade()) != null) {
            overallGrade.setText(overallGradeValue);
            overallScore.setText(CourseRecyclerViewAdapter.scoreFormatter.format(currentCourse.getOverallScore()));
        } else {
            overallGrade.setText("");
            overallScore.setText("");
        }
    }

    public Course getCourse() {
        return course;
    }
}

