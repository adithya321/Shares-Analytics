package com.adithya321.sharesanalysis.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.adithya321.sharesanalysis.R;
import com.adithya321.sharesanalysis.database.Fund;
import com.adithya321.sharesanalysis.database.DatabaseHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FundFlowFragment extends Fragment {

    private int year_start, month_start, day_start, totalFundIn, totalFundOut;
    private DatabaseHandler databaseHandler;
    private TextView fundIn, fundOut;
    private ListView fundsListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_fund_flow, container, false);

        databaseHandler = new DatabaseHandler(getContext());
        fundIn = (TextView) root.findViewById(R.id.fund_in);
        fundOut = (TextView) root.findViewById(R.id.fund_out);
        fundsListView = (ListView) root.findViewById(R.id.funds_list_view);
        setViews();

        FloatingActionButton addFundFab = (FloatingActionButton) root.findViewById(R.id.add_fund_fab);
        addFundFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setTitle("Add Fund Flow");
                dialog.setContentView(R.layout.dialog_add_fund);
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
                dialog.show();

                Calendar calendar = Calendar.getInstance();
                year_start = calendar.get(Calendar.YEAR);
                month_start = calendar.get(Calendar.MONTH) + 1;
                day_start = calendar.get(Calendar.DAY_OF_MONTH);
                final Button selectDate = (Button) dialog.findViewById(R.id.select_date);
                selectDate.setText(new StringBuilder().append(day_start).append("/")
                        .append(month_start).append("/").append(year_start));
                selectDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Dialog dialog = new DatePickerDialog(getActivity(), onDateSetListener,
                                year_start, month_start - 1, day_start);
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                selectDate.setText(new StringBuilder().append(day_start).append("/")
                                        .append(month_start).append("/").append(year_start));
                            }
                        });
                        dialog.show();
                    }
                });

                final EditText amount = (EditText) dialog.findViewById(R.id.amount);
                Button addFundBtn = (Button) dialog.findViewById(R.id.add_fund_btn);
                addFundBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fund fund = new Fund();
                        fund.setId(databaseHandler.getNextKey("fund"));

                        String stringStartDate = year_start + " " + month_start + " " + day_start;
                        DateFormat format = new SimpleDateFormat("yyyy MM dd", Locale.ENGLISH);
                        try {
                            Date date = format.parse(stringStartDate);
                            fund.setDate(date);
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Invalid Date", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {
                            fund.setAmount(Double.parseDouble(amount.getText().toString()));
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Invalid Amount", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (((RadioButton) dialog.findViewById(R.id.radioBtn_fund_in)).isChecked())
                            fund.setType("in");
                        else if (((RadioButton) dialog.findViewById(R.id.radioBtn_fund_out)).isChecked())
                            fund.setType("out");
                        else {
                            Toast.makeText(getActivity(), "Invalid Fund Type", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        databaseHandler.addFund(fund);
                        setViews();
                        dialog.dismiss();
                    }
                });
            }
        });

        return root;
    }

    private DatePickerDialog.OnDateSetListener onDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    year_start = year;
                    month_start = monthOfYear + 1;
                    day_start = dayOfMonth;
                }
            };

    private void setViews() {
        List<Fund> fundList = databaseHandler.getFunds();
        ArrayAdapter<Fund> arrayAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, fundList);
        fundsListView.setAdapter(arrayAdapter);

        totalFundIn = totalFundOut = 0;
        for (Fund fund : fundList) {
            if (fund.getType().equals("in"))
                totalFundIn += fund.getAmount();
            else if (fund.getType().equals("out"))
                totalFundOut += fund.getAmount();
        }

        fundIn.setText("Fund In  : " + totalFundIn);
        fundOut.setText("Fund Out : " + totalFundOut);
    }
}
