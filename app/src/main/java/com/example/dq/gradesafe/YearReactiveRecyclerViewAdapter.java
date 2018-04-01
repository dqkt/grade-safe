package com.example.dq.gradesafe;

import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
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
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;

public class YearReactiveRecyclerViewAdapter extends ReactiveRecyclerViewAdapter<YearViewHolder>
        implements ReactiveRecyclerView.TouchCallback, YearViewHolder.YearActionCallback {

    private ArrayList<Year> years;

    public YearReactiveRecyclerViewAdapter(Context context, ArrayList<Year> years, ReactiveRecyclerView reactiveRecyclerView) {
        super(context, reactiveRecyclerView);
        this.years = years;
        this.reactiveRecyclerView.setTouchCallback(this);
        this.reactiveRecyclerView.setHeaderClass(MainHeader.class);
    }

    @Override
    public boolean hasHeader() {
        return true;
    }

    @Override
    public boolean onMove(ReactiveRecyclerView.ViewHolder viewHolder, ReactiveRecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        if (!(target instanceof MainHeader)) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(years, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(years, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }
        return false;
    }

    @Override
    public void modifyYear(Year year, String yearName) {
        year.setName(yearName);
        notifyItemChanged(years.indexOf(year));
    }

    @Override
    public void deleteYear(Year year) {
        int position = years.indexOf(year);
        years.remove(year);
        notifyItemRemoved(position);
    }

    @Override
    public void addTerm(Year year, Term term) {
        year.addTerm(term);
        notifyItemChanged(years.indexOf(year));
    }

    @Override
    public YearViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            final ViewPager header = (ViewPager) inflater.inflate(R.layout.layout_slideshow, parent, false);
            return new MainHeader(header, context);
        } else if (viewType == TYPE_ITEM) {
            final RelativeLayout yearLayout = (RelativeLayout) inflater.inflate(R.layout.layout_year, parent, false);
            YearViewHolder yearViewHolder = new YearViewHolder(yearLayout, context, reactiveRecyclerView);
            yearViewHolder.setYearActionCallback(this);
            return yearViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(YearViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (!(holder instanceof MainHeader)) {
            final Year currentYear = years.get(position);

            holder.year = currentYear;
            if (holder.year.getTerms().size() == 0) {
                holder.termRecyclerView.setVisibility(GONE);
                holder.noTerms.setVisibility(View.VISIBLE);
            } else {
                holder.termRecyclerView.setVisibility(View.VISIBLE);
                holder.noTerms.setVisibility(GONE);
            }

            holder.name.setText(currentYear.getName());

            holder.termRecyclerViewAdapter = new TermReactiveRecyclerViewAdapter(context, currentYear.getTerms(), holder.termRecyclerView);
            holder.termRecyclerView.setAdapter(holder.termRecyclerViewAdapter);
            LinearLayoutManager termsLinearLayoutManager = new LinearLayoutManager(context);
            termsLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            holder.termRecyclerView.setLayoutManager(termsLinearLayoutManager);

            holder.termRecyclerViewAdapter.notifyDataSetChanged();
        } else {
            MainHeader mainHeader = (MainHeader) holder;
        }
    }

    @Override
    public int getItemCount() {
        return years.size();
    }
}

class MainHeader extends YearViewHolder {

    private int NUM_SLIDES = 5;

    private ViewPager slideshowPager;
    private ScrollView pagerScrollView;

    private PagerAdapter slideshowPagerAdapter;
    private Field slideshowScroller;
    private Handler handler;
    private Runnable slideshowUpdate;
    private Timer slideshowTimer;
    private TimerTask slideshowTimerTask;

    private int currentSlide;

    public MainHeader(View view, Context context) {
        super(view);

        slideshowPager = (ViewPager) view;
        slideshowPager.setPageTransformer(true, new ZoomOutPageTransformer());
        slideshowPagerAdapter = new SlidePagerAdapter(((OverviewActivity) context).getSupportFragmentManager());
        slideshowPager.setAdapter(slideshowPagerAdapter);

        View slideLayout = LayoutInflater.from(context).inflate(R.layout.fragment_slide, null);

        pagerScrollView = (ScrollView) slideLayout.findViewById(R.id.pager_content);

        slideshowPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
            slideshowScroller.set(slideshowPager, new CustomScroller(context, new AccelerateDecelerateInterpolator(), 500));
        } catch (Exception e) {
            e.printStackTrace();
        }

        handler = new Handler();
        slideshowTimer = new Timer();
        slideshowTimerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(slideshowUpdate);
            }
        };
        currentSlide = 0;

        slideshowUpdate = new Runnable() {
            public void run() {
                if (currentSlide == NUM_SLIDES - 1) {
                    currentSlide = 0;
                }
                slideshowPager.setCurrentItem(currentSlide++, true);
            }
        };

        slideshowTimer.schedule(slideshowTimerTask, 0, 5000);
    }

    private class SlidePagerAdapter extends FragmentStatePagerAdapter {
        public SlidePagerAdapter(FragmentManager fm) {
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

class YearViewHolder extends ReactiveRecyclerView.ViewHolder {

    public Context context;

    public Year year;

    public RelativeLayout overallLayout;
    public LinearLayout regularLayout;
    public TextView name;

    public ReactiveRecyclerView termRecyclerView;
    public TermReactiveRecyclerViewAdapter termRecyclerViewAdapter;
    public TextView noTerms;

    public RelativeLayout editYear;
    public RelativeLayout deleteYear;
    public RelativeLayout addTerm;

    private Runnable optionsTimeout;
    private Handler optionsHandler;
    private Animator.AnimatorListener optionsAnimation;
    public LinearLayout yearOptions;
    public boolean isShowingOptions;

    private YearActionCallback yearActionCallback;

    private RelativeLayout addTermDialogLayout;
    private EditText newTermName;

    public YearViewHolder(View view) {
        super(view);
    }

    public YearViewHolder(View view, Context context, ReactiveRecyclerView reactiveRecyclerView) {
        super(view, reactiveRecyclerView.getAdapter());

        this.context = context;

        overallLayout = (RelativeLayout) view;
        regularLayout = (LinearLayout) view.findViewById(R.id.layout_regular);
        name = (TextView) regularLayout.findViewById(R.id.textview_name);
        termRecyclerView = (ReactiveRecyclerView) regularLayout.findViewById(R.id.recyclerview_terms);

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

                    optionsHandler.postDelayed(optionsTimeout, 5000);
                }
            }
        });

        setUpOptions();
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
                            optionsHandler.postDelayed(optionsTimeout, 5000);
                            yearActionCallback.modifyYear(year, yearName);
                        }
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        optionsHandler.postDelayed(optionsTimeout, 5000);
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
                        optionsHandler.postDelayed(optionsTimeout, 5000);
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
                            dialog.dismiss();
                            optionsHandler.postDelayed(optionsTimeout, 5000);
                            yearActionCallback.addTerm(year, new Term(termName));
                        }
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        optionsHandler.postDelayed(optionsTimeout, 5000);
                    }
                });
            }
        });
    }

    public void setYearActionCallback(YearActionCallback yearActionCallback) {
        this.yearActionCallback = yearActionCallback;
    }

    public interface YearActionCallback {
        void modifyYear(Year year, String yearName);
        void deleteYear(Year year);
        void addTerm(Year year, Term term);
    }
}
