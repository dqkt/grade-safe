package com.example.dq.gradesafe;

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
import java.util.Collections;
import java.util.List;

/**
 * Created by DQ on 3/31/2018.
 */

public class AssignmentRecyclerViewAdapter extends RecyclerView.Adapter<AssignmentViewHolder> {

    static final int TYPE_HEADER = 0;
    static final int TYPE_ITEM = 1;

    protected Context context;
    protected LayoutInflater inflater;

    private List<Assignment> assignments;
    private AssignmentListViewModel assignmentListViewModel;

    private Course course;

    public static final DecimalFormat weightFormatter = new DecimalFormat("0'%'");
    public static final DecimalFormat scoreFormatter = new DecimalFormat("0.00");

    protected boolean isBinding;

    AssignmentRecyclerViewAdapter(Context context, List<Assignment> assignments, AssignmentListViewModel assignmentListViewModel, Course course) {
        // super(context, reactiveRecyclerView);
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        this.assignments = assignments;
        this.assignmentListViewModel = assignmentListViewModel;

        this.course = course;

        this.isBinding = false;

        setHasStableIds(true);
        // this.reactiveRecyclerView.setTouchCallback(this);
        // this.reactiveRecyclerView.setHeaderClass(AssignmentsHeader.class);
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
        Assignment fromAssignment, toAssignment;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                fromAssignment = assignments.get(i);
                toAssignment = assignments.get(i + 1);
                fromAssignment.setListIndex(i + 1);
                toAssignment.setListIndex(i);
                assignmentListViewModel.updateAssignment(fromAssignment);
                assignmentListViewModel.updateAssignment(toAssignment);
                Collections.swap(assignments, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                fromAssignment = assignments.get(i);
                toAssignment = assignments.get(i - 1);
                fromAssignment.setListIndex(i - 1);
                toAssignment.setListIndex(i);
                assignmentListViewModel.updateAssignment(fromAssignment);
                assignmentListViewModel.updateAssignment(toAssignment);
                Collections.swap(assignments, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        final Assignment assignment = ((AssignmentViewHolder) viewHolder).getAssignment();
        final int position = assignments.indexOf(assignment);
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

            if (assignment != null && course != null) {
                String assignmentName = assignment.getName();
                String courseName = course.getName();
                SpannableStringBuilder stringBuilder = new SpannableStringBuilder("Are you sure you want to delete " + assignmentName + " from " + courseName + "?");
                StyleSpan assignmentNameSpan = new StyleSpan(Typeface.BOLD);
                StyleSpan courseNameSpan = new StyleSpan(Typeface.BOLD);
                int assignmentNameLength = assignmentName.length();
                stringBuilder.setSpan(assignmentNameSpan, 32, 32 + assignmentNameLength, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                stringBuilder.setSpan(courseNameSpan, 38 + assignmentNameLength, 38 + assignmentNameLength + courseName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                message.setText(stringBuilder);
            } else {
                message.setText(String.valueOf("Are you sure you want to delete this assignment from " + course.getName() + "?"));
            }
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.setCanceledOnTouchOutside(true);

            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    assignmentListViewModel.removeAssignment(assignment);
                    int numAssignments = assignments.size();
                    for (int i = position; i < numAssignments; i++) {
                        assignments.get(i).setListIndex(assignments.get(i).getListIndex() - 1);
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

    public void updateAssignments(List<Assignment> assignments) {
        DiffUtil.DiffResult difference = DiffUtil.calculateDiff(new AssignmentListDiffCallback(this.assignments, assignments));
        this.assignments.clear();
        this.assignments.addAll(assignments);
        difference.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return assignments.get(position).getAssignmentID();
    }

    @Override
    public AssignmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View assignmentLayout = inflater.inflate(R.layout.layout_assignment, parent, false);
        return new AssignmentViewHolder(assignmentLayout, context);
    }

    @Override
    public void onBindViewHolder(AssignmentViewHolder holder, int position) {
        isBinding = true;
        // super.onBindViewHolder(holder, position);
        if (!(holder instanceof AssignmentsHeader)) {
            final Assignment currentAssignment = assignments.get(position);
            holder.updateViewHolder(currentAssignment);
        } else {
            AssignmentsHeader assignmentsHeader = (AssignmentsHeader) holder;
        }
        isBinding = false;
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }
}

class AssignmentsHeader extends AssignmentViewHolder {

    public AssignmentsHeader(View view, Context context) {
        super(view);
    }
}

class AssignmentViewHolder extends RecyclerView.ViewHolder {

    private Context context;

    private Assignment assignment;

    private RelativeLayout overallLayout;
    private TextView name;
    private TextView weight;
    private TextView score;
    private TextView grade;

    public AssignmentViewHolder(View view) {
        super(view);
    }

    public AssignmentViewHolder(View view, final Context context) {
        // super(view, reactiveRecyclerView.getAdapter());
        super(view);

        this.context = context;

        overallLayout = (RelativeLayout) view;
        name = (TextView) overallLayout.findViewById(R.id.textview_name);
        weight = (TextView) overallLayout.findViewById(R.id.textview_weight);
        score = (TextView) overallLayout.findViewById(R.id.textview_score);
        // grade = (TextView) overallLayout.findViewById(R.id.textview_grade);
    }

    public void updateViewHolder(Assignment currentAssignment) {
        assignment = currentAssignment;
        name.setText(currentAssignment.getName());

        weight.setText(String.valueOf("Worth " + AssignmentRecyclerViewAdapter.weightFormatter.format(currentAssignment.getWeight())));
        double scoreDenominator;
        if ((scoreDenominator = currentAssignment.getScoreDenominator()) != 0 && assignment.isComplete()) {
            double overallScore = currentAssignment.getScoreNumerator() / scoreDenominator * 100;
            GradingScale gradingScale = GradingScale.createStandardGradingScale();
            ScoreRange scoreRange = gradingScale.getScoreRange(overallScore);
            /*if (scoreRange != null) {
                grade.setText(scoreRange.getGrade());
            }*/
            score.setText(AssignmentRecyclerViewAdapter.scoreFormatter.format(Math.rint(overallScore)));
        } else {
            // grade.setText("");
            score.setText("");
        }
    }

    public Assignment getAssignment() {
        return assignment;
    }
}

