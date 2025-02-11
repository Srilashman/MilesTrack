package com.sp.milestrack.ui.home;

import static android.graphics.Color.parseColor;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sp.milestrack.Database;
import com.sp.milestrack.LineChartViewModel;
import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentHomeBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.database.Cursor;
import com.sp.milestrack.Database;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private LineChart lineChart;
    private LineChartViewModel lineChartViewModel;
    private Database helper = null;

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

        helper = new Database(requireContext());
        // Simulate data update
        // Load actual BMI data instead of simulated data
        loadMonthlyAverageBMI();

        binding.addinfobtn.setOnClickListener(addinfo);

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
            }
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            setGridLines();
            lineChart.invalidate(); // Refresh the chart
        }
    }

//    private void simulateData() {
//        // Simulate adding data to the chart
//        List<Entry> entries = new ArrayList<>();
//        entries.add(new Entry(0, 5));  // Jan
//        entries.add(new Entry(1, 7));  // Feb
//        entries.add(new Entry(2, 3));  // Mar
//        entries.add(new Entry(3, 8));  // Apr
//        entries.add(new Entry(4, 6));  // May
//        entries.add(new Entry(5, 4));  // Jun
//        entries.add(new Entry(6, 9));  // Jul
//        entries.add(new Entry(7, 2));  // Aug
//        entries.add(new Entry(8, 5));  // Sep
//        entries.add(new Entry(9, 6));  // Oct
//        entries.add(new Entry(10, 3)); // Nov
//        entries.add(new Entry(11, 7)); // Dec
//
//        int currentNightMode = getResources().getConfiguration().uiMode
//                & Configuration.UI_MODE_NIGHT_MASK;
//        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
//            lineChart.getAxisLeft().setTextColor(parseColor("#FFFFFF")); // left y-axis
//            lineChart.getXAxis().setTextColor(parseColor("#FFFFFF"));
//            lineChart.getLegend().setTextColor(parseColor("#FFFFFF"));
//            lineChart.getDescription().setTextColor(parseColor("#FFFFFF"));
//        }
//        lineChartViewModel.setChartData(entries);
//    }
    private void setGridLines() {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false); // Remove X axis gridlines
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setAxisLineWidth(dpToPx(1f));   // Set the thickness of the axis line
        xAxis.setAxisLineColor(Color.BLACK); // Set axis line color

        // Set month formatter
        xAxis.setValueFormatter(new MonthValueFormatter());
        xAxis.setGranularity(1f); // Ensure the X-axis increments are whole numbers
        xAxis.setLabelCount(12);  // Show all 12 months

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true); // Ensure gridlines are enabled
        leftAxis.setGridLineWidth(dpToPx(0.5f));   // Set the thickness of gridlinea
        leftAxis.setGridColor(parseColor("#808080")); // Set gridline color

        leftAxis.setAxisLineWidth(dpToPx(1f));   // Set the thickness of the axis line
        leftAxis.setAxisLineColor(Color.BLACK); // Set axis line color
    }
    float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private View.OnClickListener addinfo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_nav_home_to_nav_edit_info);
        }
    };

    private class MonthValueFormatter extends ValueFormatter {
        private final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < months.length) {
                return months[index];  // Return month name based on index
            } else {
                return "";  // Return empty if index is out of range
            }
        }
    }

    private void loadMonthlyAverageBMI() {
        Cursor cursor = helper.getAllBMIRecords();
        if (cursor != null && cursor.moveToFirst()) {

            // Initialize arrays to store total BMI and count of entries for each month
            double[] totalBMI = new double[12];  // Jan = 0, Feb = 1, ..., Dec = 11
            int[] countPerMonth = new int[12];

            int dateIndex = cursor.getColumnIndex("date");
            int weightIndex = cursor.getColumnIndex("weight");
            int heightIndex = cursor.getColumnIndex("height");

            if (dateIndex != -1 && weightIndex != -1 && heightIndex != -1){
            do {
                // Get weight and height from the database
                String dateString = cursor.getString(dateIndex);
                double weight = cursor.getDouble(weightIndex);
                double height = cursor.getDouble(heightIndex);

                // Calculate BMI = weight / (height * height) [Assuming height is in meters]
                double bmi = weight / (height * height);

                int monthIndex = getMonthIndex(dateString);  // Helper method to extract month (0 for Jan, 11 for Dec)

                if (monthIndex != -1) {  // Check if the date was parsed successfully
                    totalBMI[monthIndex] += bmi;
                    countPerMonth[monthIndex]++;
                }
            } while (cursor.moveToNext());

            cursor.close();

            // Prepare entries for the LineChart
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                if (countPerMonth[i] > 0) {
                    double avgBMI = totalBMI[i] / countPerMonth[i];
                    entries.add(new Entry(i, (float) avgBMI));
                }
            }

            // Update the LineChart with averaged data
            lineChartViewModel.setChartData(entries);
            } else {
                Log.e("DB_ERROR", "One or more columns are missing in the query result.");
            }

            cursor.close();
        } else {
            lineChartViewModel.setChartData(new ArrayList<>());
        }
    }

    private int getMonthIndex(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateString);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                return calendar.get(Calendar.MONTH);  // Returns 0 for January, 11 for December
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if parsing fails
    }
}