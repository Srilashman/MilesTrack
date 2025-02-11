package com.sp.milestrack.ui.home;

import static android.graphics.Color.parseColor;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sp.milestrack.LineChartViewModel;
import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private LineChart lineChart;
    private LineChartViewModel lineChartViewModel;
    private TextView planText;
    private ImageView cal_ic;
    private CalendarView cal;
    private LinearLayout cal_layout;
    private boolean isStartDateSelected = false;
    private String startDate = "", endDate = "";
    private TextView selectedDates;
    private TextView calPrompt;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        // Initialize the LineChart
        lineChart = root.findViewById(R.id.chart);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        // Set up the ViewModel
        lineChartViewModel = new ViewModelProvider(this).get(LineChartViewModel.class);

        // Observe the chart data
        lineChartViewModel.getChartData().observe(getViewLifecycleOwner(), this::updateLineChart);
        planText = root.findViewById(R.id.plan);
        cal_ic = root.findViewById(R.id.calendar_ic);
        cal = root.findViewById(R.id.cal);
        cal_layout = root.findViewById(R.id.cal_layout);
        calPrompt = root.findViewById(R.id.cal_prompt);
        selectedDates = root.findViewById(R.id.selected_dates);
        cal_layout.setVisibility(View.GONE);
        cal_ic.setOnClickListener(toggleCal);
        cal.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            if (!isStartDateSelected) {
                startDate = selectedDate;
                isStartDateSelected = true;
                selectedDates.setText("Start Date: " + startDate);
                calPrompt.setText("Enter your end date");
            } else {
                endDate = selectedDate;
                selectedDates.setText("Start Date: " + startDate + "\nEnd Date: " + endDate);
                isStartDateSelected = false; // Reset for next selection
                calPrompt.setVisibility(View.GONE);
            }
        });

        // Simulate data update
        simulateData();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void updateLineChart(List<Entry> entries) {
        if (entries != null) {
            LineDataSet dataSet = new LineDataSet(entries, "Sample Data");
            dataSet.setColor(getResources().getColor(R.color.purple_200));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setValueTextSize(10f);
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
                dataSet.setValueTextColor(parseColor("#FFFFFF"));
                planText.setTextColor(parseColor("#FFFFFF"));
            }
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            setGridLines();
            lineChart.invalidate(); // Refresh the chart
        }
    }

    private void simulateData() {
        // Simulate adding data to the chart
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 1));
        entries.add(new Entry(1, 3));
        entries.add(new Entry(2, 5));
        entries.add(new Entry(3, 2));
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
            lineChart.getAxisLeft().setTextColor(parseColor("#FFFFFF")); // left y-axis
            lineChart.getXAxis().setTextColor(parseColor("#FFFFFF"));
            lineChart.getLegend().setTextColor(parseColor("#FFFFFF"));
            lineChart.getDescription().setTextColor(parseColor("#FFFFFF"));
        }
        lineChartViewModel.setChartData(entries);
    }
    private void setGridLines() {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false); // Remove X axis gridlines
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setAxisLineWidth(dpToPx(1f));   // Set the thickness of the axis line
        xAxis.setAxisLineColor(Color.BLACK); // Set axis line color

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true); // Ensure gridlines are enabled
        leftAxis.setGridLineWidth(dpToPx(0.5f));   // Set the thickness of gridlines
        leftAxis.setGridColor(parseColor("#808080")); // Set gridline color

        leftAxis.setAxisLineWidth(dpToPx(1f));   // Set the thickness of the axis line
        leftAxis.setAxisLineColor(Color.BLACK); // Set axis line color
    }
    float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private View.OnClickListener toggleCal = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (cal_layout.getVisibility() == View.VISIBLE) {
                cal_layout.animate().alpha(0.0f).setDuration(300).withEndAction(() -> cal_layout.setVisibility(View.GONE));
            } else {
                cal_layout.setVisibility(View.VISIBLE);
                cal_layout.animate().alpha(1.0f).setDuration(300);
            }

        }
    };

}