package com.example.dq.gradesafe;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class OverviewActivity extends AppCompatActivity {

    private static final String YEARS_SHARED_PREFERENCES = "YEARS";
    private static final String YEAR_LIST_KEY = "YEAR_LIST";

    private ArrayList<Year> yearList;

    private ReactiveRecyclerView yearRecyclerView;
    private YearReactiveRecyclerViewAdapter yearRecyclerViewAdapter;

    private FloatingActionButton addYearButton;
    private RelativeLayout addYearDialogLayout;
    private EditText newYearName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        setTitle("");
    }

    @Override
    protected void onResume() {
        setUpYearsArea();

        super.onResume();
    }

    @Override
    protected void onPause() {
        Maintenance.saveArrayList(this, yearList, YEARS_SHARED_PREFERENCES, YEAR_LIST_KEY);

        super.onPause();
    }

    private void setUpYearsArea() {
        if (yearList == null) {
            yearList = Maintenance.loadArrayList(this, Year.class, YEARS_SHARED_PREFERENCES, YEAR_LIST_KEY);
            if (yearList.isEmpty()) {
                yearList.add(new Year("Header"));
            }

            yearRecyclerView = (ReactiveRecyclerView) findViewById(R.id.recyclerview_years);

            yearRecyclerViewAdapter = new YearReactiveRecyclerViewAdapter(this, yearList, yearRecyclerView);
            yearRecyclerView.setAdapter(yearRecyclerViewAdapter);
            LinearLayoutManager yearsLinearLayoutManager = new LinearLayoutManager(this);
            yearsLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            yearRecyclerView.setLayoutManager(yearsLinearLayoutManager);
            yearRecyclerViewAdapter.notifyDataSetChanged();
        }

        if (addYearButton == null) {
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

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            String yearName = newYearName.getText().toString().replaceAll("^\\s+|\\s+$", "");
                            boolean valid = true;

                            if (yearName.isEmpty()) {
                                valid = false;
                                addNewItemLayout.setError("Name cannot be blank");
                            }

                            for (Year year : yearList) {
                                if (year.getName().equals(yearName)) {
                                    valid = false;
                                    addNewItemLayout.setError("Name already exists");
                                    break;
                                }
                            }

                            if (valid) {
                                yearList.add(new Year(yearName));
                                dialog.dismiss();
                            }
                        }
                    });
                }
            });
        }
    }
}
