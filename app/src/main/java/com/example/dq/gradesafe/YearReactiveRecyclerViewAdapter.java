package com.example.dq.gradesafe;

import android.animation.Animator;
import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;
import static com.example.dq.gradesafe.YearReactiveRecyclerViewAdapter.OPTIONS_TIMEOUT;

public class YearReactiveRecyclerViewAdapter extends ReactiveRecyclerViewAdapter<YearViewHolder>
        implements ReactiveRecyclerView.TouchCallback {

    static final int OPTIONS_TIMEOUT = 5000;

    private List<Year> years;
    private TermListViewModel termListViewModel;
    private YearListViewModel yearListViewModel;
    private YearActionCallback yearActionCallback;

    YearReactiveRecyclerViewAdapter(Context context, List<Year> years, ReactiveRecyclerView reactiveRecyclerView, YearListViewModel yearListViewModel, YearActionCallback yearActionCallback) {
        super(context, reactiveRecyclerView);
        this.years = years;
        this.reactiveRecyclerView.setTouchCallback(this);
        this.yearListViewModel = yearListViewModel;
        this.termListViewModel = ViewModelProviders.of((OverviewActivity) context).get(TermListViewModel.class);
        this.yearActionCallback = yearActionCallback;
    }

    @Override
    public boolean hasHeader() {
        return false;
    }

    @Override
    public boolean onMove(ReactiveRecyclerView.ViewHolder viewHolder, ReactiveRecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        Year fromYear, toYear;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                fromYear = years.get(i);
                toYear = years.get(i + 1);
                fromYear.setListIndex(i + 1);
                toYear.setListIndex(i);
                yearListViewModel.updateYear(fromYear);
                yearListViewModel.updateYear(toYear);
                Collections.swap(years, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                fromYear = years.get(i);
                toYear = years.get(i - 1);
                fromYear.setListIndex(i - 1);
                toYear.setListIndex(i);
                yearListViewModel.updateYear(fromYear);
                yearListViewModel.updateYear(toYear);
                Collections.swap(years, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    public void updateYears(List<Year> years) {
        DiffUtil.DiffResult difference = DiffUtil.calculateDiff(new YearListDiffCallback(this.years, years));
        this.years.clear();
        this.years.addAll(years);
        difference.dispatchUpdatesTo(this);
    }

    public void setYears(List<Year> years) {
        this.years = years;
        notifyDataSetChanged();
    }

    @Override
    public YearViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LinearLayout yearLayout = (LinearLayout) inflater.inflate(R.layout.layout_year, parent, false);
        YearViewHolder yearViewHolder = new YearViewHolder(yearLayout, context, reactiveRecyclerView, termListViewModel);
        yearViewHolder.setYearActionCallback(yearActionCallback);
        return yearViewHolder;
    }

    @Override
    public void onBindViewHolder(YearViewHolder holder, int position) {
        isBinding = true;
        super.onBindViewHolder(holder, position);

        final Year currentYear = years.get(position);
        holder.year = currentYear;
        holder.setUpTermListViewModel();
        holder.updateTermListView();

        holder.name.setText(String.valueOf(currentYear.getName()));

        isBinding = false;
    }

    @Override
    public int getItemCount() {
        return years.size();
    }

    public interface YearActionCallback {
        void modifyYear(Year year, String yearName);
        void deleteYear(Year year);
    }
}

class YearViewHolder extends ReactiveRecyclerView.ViewHolder {

    public Context context;

    public Year year;

    public LinearLayout overallLayout;
    public LinearLayout regularLayout;
    public TextView name;

    public TermListViewModel termListViewModel;

    public RecyclerView termRecyclerView;
    public TermRecyclerViewAdapter termRecyclerViewAdapter;
    public TextView noTerms;

    public RelativeLayout editYear;
    public RelativeLayout deleteYear;
    public RelativeLayout addTerm;

    private Runnable optionsTimeout;
    private Handler optionsHandler;
    private Animator.AnimatorListener optionsAnimation;
    public LinearLayout yearOptions;
    public boolean isShowingOptions;

    private YearReactiveRecyclerViewAdapter.YearActionCallback yearActionCallback;

    private RelativeLayout addTermDialogLayout;
    private EditText newTermName;

    public YearViewHolder(View view) {
        super(view);
    }

    public YearViewHolder(View view, Context context, ReactiveRecyclerView reactiveRecyclerView, TermListViewModel termListViewModel) {
        super(view, reactiveRecyclerView.getAdapter());

        this.context = context;

        overallLayout = (LinearLayout) view;
        regularLayout = (LinearLayout) view.findViewById(R.id.layout_regular);
        name = (TextView) regularLayout.findViewById(R.id.textview_name);
        termRecyclerView = (RecyclerView) regularLayout.findViewById(R.id.recyclerview_terms);
        termRecyclerViewAdapter = new TermRecyclerViewAdapter(context, new ArrayList<Term>(), termListViewModel, year);
        termRecyclerView.setAdapter(termRecyclerViewAdapter);

        noTerms = (TextView) regularLayout.findViewById(R.id.textview_no_terms);

        editYear = (RelativeLayout) regularLayout.findViewById(R.id.button_edit_year);
        deleteYear = (RelativeLayout) regularLayout.findViewById(R.id.button_delete_year);
        addTerm = (RelativeLayout) regularLayout.findViewById(R.id.button_add_term);

        optionsTimeout = new Runnable() {
            @Override
            public void run() {
                hideOptions();
            }
        };
        optionsHandler = new Handler();
        optionsAnimation = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                yearOptions.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        yearOptions = (LinearLayout) overallLayout.findViewById(R.id.layout_year_options);
        isShowingOptions = false;

        overallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowingOptions) {
                    hideOptions();

                    optionsHandler.removeCallbacks(optionsTimeout);
                } else {
                    yearOptions.setVisibility(View.VISIBLE);
                    yearOptions.animate().setDuration(200).alpha(1.0f).setListener(null);
                    isShowingOptions = true;

                    optionsHandler.postDelayed(optionsTimeout, OPTIONS_TIMEOUT);
                }
            }
        });

        this.termListViewModel = termListViewModel;
        setUpOptions();
    }

    public void setUpTermListViewModel() {
        Year currentYear = year == null ? new Year("") : year;
        termRecyclerViewAdapter = new TermRecyclerViewAdapter(context, new ArrayList<Term>(), termListViewModel, year);
        termRecyclerView.setAdapter(termRecyclerViewAdapter);
        LinearLayoutManager termsLinearLayoutManager = new LinearLayoutManager(context);
        termsLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        termRecyclerView.setLayoutManager(termsLinearLayoutManager);

        termListViewModel.getTermsInYear(currentYear).observe((OverviewActivity) context, new Observer<List<Term>>() {
            @Override
            public void onChanged(@Nullable List<Term> terms) {
                termRecyclerViewAdapter.updateTerms(terms);
                updateTermListView();
            }
        });

        SwipeUtil swipeUtil = new SwipeUtil(ItemTouchHelper.END | ItemTouchHelper.START, 0, context) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                termRecyclerViewAdapter.onMove(viewHolder, target);
                return false;
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.END | ItemTouchHelper.START, 0);
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeUtil);
        itemTouchHelper.attachToRecyclerView(termRecyclerView);
    }

    public void updateTermListView() {
        int numTerms = termRecyclerViewAdapter.getItemCount();
        if (numTerms == 0) {
            termRecyclerView.setVisibility(GONE);
            noTerms.setVisibility(View.VISIBLE);
        } else {
            termRecyclerView.setVisibility(View.VISIBLE);
            noTerms.setVisibility(GONE);
        }
    }

    private void hideOptions() {
        yearOptions.animate().setDuration(200).alpha(0.0f).setListener(optionsAnimation);
        isShowingOptions = false;
    }

    private void setUpOptions() {
        editYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsHandler.removeCallbacks(optionsTimeout);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

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

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(OverviewActivity.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_add_year, null);

                RelativeLayout editYearDialogLayout = (RelativeLayout) view.findViewById(R.id.layout_add_year);

                final TextInputLayout editItemLayout = (TextInputLayout) editYearDialogLayout.findViewById(R.id.textinput_add_year);
                final TextInputEditText newYearName = (TextInputEditText) editItemLayout.findViewById(R.id.edittext_year_name);
                editItemLayout.setErrorEnabled(true);
                newYearName.setText(year.getName());
                builder.setView(editYearDialogLayout);

                final AlertDialog dialog = builder.create();

                if (year != null) {
                    dialog.setTitle("Edit " + year.getName());
                } else {
                    dialog.setTitle("Edit year");
                }
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
                            editItemLayout.setError("Name cannot be blank");
                        }

                        if (valid) {
                            dialog.dismiss();
                            optionsHandler.postDelayed(optionsTimeout, OPTIONS_TIMEOUT);
                            yearActionCallback.modifyYear(year, yearName);
                        }
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        optionsHandler.postDelayed(optionsTimeout, OPTIONS_TIMEOUT);
                    }
                });
            }
        });

        deleteYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsHandler.removeCallbacks(optionsTimeout);

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

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(OverviewActivity.LAYOUT_INFLATER_SERVICE);
                RelativeLayout confirmationDialogLayout = (RelativeLayout) inflater.inflate(R.layout.confirmation_dialog, null);

                TextView message = (TextView) confirmationDialogLayout.findViewById(R.id.textview_message);
                builder.setView(confirmationDialogLayout);

                final AlertDialog dialog = builder.create();

                if (year != null) {
                    String yearName = year.getName();
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder("Are you sure you want to delete " + yearName + " and its terms?");
                    StyleSpan span = new StyleSpan(Typeface.BOLD);
                    stringBuilder.setSpan(span, 32, 32 + yearName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    message.setText(stringBuilder);
                } else {
                    message.setText("Are you sure you want to delete this year and its terms?");
                }
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.setCanceledOnTouchOutside(true);

                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        yearActionCallback.deleteYear(year);
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        optionsHandler.postDelayed(optionsTimeout, OPTIONS_TIMEOUT);
                    }
                });
            }
        });

        addTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsHandler.removeCallbacks(optionsTimeout);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);

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

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(OverviewActivity.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_add_term, null);

                addTermDialogLayout = (RelativeLayout) view.findViewById(R.id.layout_add_term);

                final TextInputLayout addNewItemLayout = (TextInputLayout) addTermDialogLayout.findViewById(R.id.textinput_add_term);
                newTermName = (TextInputEditText) addNewItemLayout.findViewById(R.id.edittext_term_name);
                builder.setView(addTermDialogLayout);

                final AlertDialog dialog = builder.create();

                if (year != null) {
                    dialog.setTitle("Add a term to " + year.getName());
                } else {
                    dialog.setTitle("Add a term");
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
                            optionsHandler.postDelayed(optionsTimeout, OPTIONS_TIMEOUT);
                            Term newTerm = new Term(termName, "Standard Grading Scale", year.getYearID());
                            newTerm.setListIndex(termRecyclerViewAdapter.getItemCount());
                            termListViewModel.addTerm(newTerm);
                            dialog.dismiss();
                        }
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        optionsHandler.postDelayed(optionsTimeout, OPTIONS_TIMEOUT);
                    }
                });
            }
        });
    }

    public void setYearActionCallback(YearReactiveRecyclerViewAdapter.YearActionCallback yearActionCallback) {
        this.yearActionCallback = yearActionCallback;
    }
}
