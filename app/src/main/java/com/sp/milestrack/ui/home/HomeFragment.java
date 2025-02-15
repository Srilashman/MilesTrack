package com.sp.milestrack.ui.home;

import static android.graphics.Color.parseColor;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.snackbar.Snackbar;
import com.sp.milestrack.Database;
import com.sp.milestrack.LineChartViewModel;
import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentHomeBinding;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.database.Cursor;
import com.sp.milestrack.Database;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private LineChart lineChart;
    private LineChartViewModel lineChartViewModel;
    private TextView planText;
    private TextView recordText;
    private ImageView cal_ic;
    private CalendarView cal;
    private LinearLayout cal_layout;
    private boolean isStartDateSelected = false;
    private String startDate = "", endDate = "";
    private TextView selectedDates;
    private TextView calPrompt;
    private Database helper = null;
    private LinearLayout workoutLayout;
    private static final String TAG = "MileTrack";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        // Initialize the LineChart
        helper = new Database(requireContext());
        lineChart = root.findViewById(R.id.chart);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        // Set up the ViewModel
        lineChartViewModel = new ViewModelProvider(this).get(LineChartViewModel.class);

        // Observe the chart data
        lineChartViewModel.getChartData().observe(getViewLifecycleOwner(), this::updateLineChart);
        planText = root.findViewById(R.id.plan);
        recordText = root.findViewById(R.id.recordbmitext);
        cal_ic = root.findViewById(R.id.calendar_ic);
        cal = root.findViewById(R.id.cal);
        cal_layout = root.findViewById(R.id.cal_layout);
        calPrompt = root.findViewById(R.id.cal_prompt);
        selectedDates = root.findViewById(R.id.selected_dates);
        workoutLayout = root.findViewById(R.id.workout_linearlayout);
        cal_layout.setVisibility(View.GONE);
        cal_ic.setOnClickListener(toggleCal);
        cal.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d", Locale.ENGLISH); // Match the format

            if (!isStartDateSelected) {
                Log.d(TAG, "start date selected");
                if (LocalDate.parse(selectedDate, formatter).isBefore(LocalDate.now())) {
                    Toast.makeText(getContext(), "Start date has to be today or after", Toast.LENGTH_SHORT).show();
                    cal.setDate(today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()); // Reset to today
                    return;
                }
                startDate = selectedDate;
                isStartDateSelected = true;
                selectedDates.setVisibility(View.VISIBLE);
                //selectedDates.setText("Start Date: " + startDate);
                calPrompt.setText("Enter your end date" + ", Start Date: " + startDate);
            } else {
                Log.d(TAG, "end date selected");
                endDate = selectedDate;
                LocalDate sDate = LocalDate.parse(startDate, formatter);
                LocalDate eDate = LocalDate.parse(endDate, formatter);

                if (eDate.isBefore(sDate)) {
                    Toast.makeText(getContext(), "End date has to be after start date", Toast.LENGTH_SHORT).show();
                    cal.setDate(today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()); // Reset to today
                    return;
                }

                if (helper.isPlansSet()) helper.deleteTraining();

                calPrompt.setText("Start Date: " + startDate + "\nEnd Date: " + endDate);
                selectedDates.setVisibility(View.GONE);
                isStartDateSelected = false;
                Cursor cursor = helper.getLastRecord();
                if (cursor != null && cursor.moveToLast()) {
                    double weight = cursor.getColumnIndex("weight");
                    double weightlossgoal = cursor.getColumnIndex("weightlossgoal");
                    double height = cursor.getColumnIndex("height");
                    int age = cursor.getColumnIndex("age");
                    workoutLayout.removeAllViews();
                    workoutLayout.invalidate();
                    boolean b = generateWorkoutPlans(LocalDate.parse(startDate, formatter), LocalDate.parse(endDate, formatter), weight, height, age, weightlossgoal);
                    if (!b) {
                        Toast.makeText(getContext(), "Daily calorie deficit too high", Toast.LENGTH_SHORT).show();
                    }
                }

//                if (helper.isPlansSet()) {
//                    Toast.makeText(getContext(), "Plans set", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(getContext(), "No plans set", Toast.LENGTH_LONG).show();
//                }
            }
        });
        // Simulate data update
        // Load actual BMI data instead of simulated data
        loadMonthlyAverageBMI();

        binding.addinfobtn.setOnClickListener(addinfo);
        if (helper.isPlansSet()) {
            Log.d(TAG, "plan set");
            LocalDate ite_date;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
            int i = 0;
            Cursor cursor = helper.getAllTrainingPlans();
            if (cursor != null) {
                while (cursor.moveToNext()) { // Moves to the next row
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                    String exercise = cursor.getString(cursor.getColumnIndexOrThrow("exercise"));
                    double distance = cursor.getDouble(cursor.getColumnIndexOrThrow("distance"));
                    String intensity = cursor.getString(cursor.getColumnIndexOrThrow("intensity"));
                    int status = cursor.getInt(cursor.getColumnIndexOrThrow("status")); // BIT type stored as INT
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    ite_date = LocalDate.parse(date, formatter);

                    if (i == 2) { // Every 3rd workout, insert a break day
                        i = 0;
                        ite_date = ite_date.minusDays(1); // Move to the next day for the break
                        addWorkoutView(ite_date.format(formatter), "Break Day - Recovery", 0);
                    }
                    i++;
                    addWorkoutView(date, intensity + " " + exercise + ": " + (int) distance + " km", status);
                }
                cursor.close(); // Close cursor to prevent memory leaks
            }
        }




        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void updateLineChart(List<Entry> entries) {
        if (entries != null && !entries.isEmpty()) {
            LineData currentData = lineChart.getData();
            if (currentData == null) {
                currentData = new LineData();
                lineChart.setData(currentData);
            }

            LineDataSet dataSet = (LineDataSet) currentData.getDataSetByIndex(0);
            if (dataSet == null) {
                dataSet = new LineDataSet(new ArrayList<>(), "Sample Data");
                currentData.addDataSet(dataSet);
            }

            for (Entry entry : entries) {
//                Log.d(TAG, "Month: " + entry.getX() + ", BMI: " + entry.getY());
                dataSet.addEntry(entry);
            }
            dataSet.setColor(getResources().getColor(R.color.purple_200));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setValueTextSize(10f);
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
                dataSet.setValueTextColor(parseColor("#FFFFFF"));
                planText.setTextColor(parseColor("#FFFFFF"));
                recordText.setTextColor(parseColor("#FFFFFF"));
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
        Log.d(TAG, "loadMonthlyAverageBMI triggered");
        Cursor cursor = helper.getAllBMIRecords();
        if (cursor != null && cursor.moveToFirst()) {

            // Initialize arrays to store total BMI and count of entries for each month
            double[] totalBMI = new double[12];  // Jan = 0, Feb = 1, ..., Dec = 11
            int[] countPerMonth = new int[12];

            int dateIndex = cursor.getColumnIndex("date");
            int weightIndex = cursor.getColumnIndex("weight");
            int heightIndex = cursor.getColumnIndex("height");

            if (dateIndex != -1 && weightIndex != -1 && heightIndex != -1) {
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

                        Log.d(TAG, "Month: " + (i + 1) + ", Total BMI: " + totalBMI[i] + ", Count: " + countPerMonth[i] + ", Avg BMI: " + avgBMI);
                        entries.add(new Entry(i, (float) avgBMI));
                    }
                }

                // Log the entries list
//                for (Entry entry : entries) {
//                    Log.d(TAG, "Month: " + entry.getX() + ", BMI: " + entry.getY());
//                }
                // Update the LineChart with averaged data
                updateLineChart(entries);

            } else {
                Log.e(TAG, "One or more columns are missing in the query result.");
            }

            cursor.close();
        } else {
            lineChartViewModel.setChartData(new ArrayList<>());
        }

        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
            lineChart.getAxisLeft().setTextColor(parseColor("#FFFFFF")); // left y-axis
            lineChart.getXAxis().setTextColor(parseColor("#FFFFFF"));
            lineChart.getLegend().setTextColor(parseColor("#FFFFFF"));
            lineChart.getDescription().setTextColor(parseColor("#FFFFFF"));
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
    private void addWorkoutView(String date, String description) {
        Log.d(TAG, "Adding workout view for " + startDate + " - " + description);
        addWorkoutView(date, description,0);
    }


    private void addWorkoutView(String date, String description, int status) {
        // Create the parent RelativeLayout
        RelativeLayout relativeLayout = new RelativeLayout(workoutLayout.getContext());
        RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        relativeLayout.setLayoutParams(relLayoutParams);
        relativeLayout.setPadding(Math.round(dpToPx(8)), Math.round(dpToPx(8)), Math.round(dpToPx(8)), Math.round(dpToPx(8)));

        // Background ImageView
        ImageView bgImage = new ImageView(workoutLayout.getContext());
        bgImage.setId(View.generateViewId());
        RelativeLayout.LayoutParams bgParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        bgImage.setImageResource(R.drawable.green_rectangle);
        bgImage.setScaleType(ImageView.ScaleType.FIT_XY);
        bgImage.setLayoutParams(bgParams);
        relativeLayout.addView(bgImage);
        if (status == 0) {
            if (description.equals("Break Day - Recovery")) {
                bgImage.setColorFilter(Color.argb(50, 150, 150, 150), PorterDuff.Mode.MULTIPLY);
            } else {
                bgImage.setColorFilter(Color.argb(100, 255, 0, 0), PorterDuff.Mode.MULTIPLY);
            }
        } else {
            bgImage.setColorFilter(Color.argb(255, 0, 255, 0));
        }

        // Date TextView
        TextView tvDate = new TextView(workoutLayout.getContext());
        tvDate.setId(View.generateViewId());
        tvDate.setText(date);
        tvDate.setTextSize(18);
        tvDate.setTextColor(Color.BLACK);
        tvDate.setTypeface(null, android.graphics.Typeface.BOLD);

        RelativeLayout.LayoutParams dateParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        dateParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        dateParams.setMargins(Math.round(dpToPx(16)), Math.round(dpToPx(16)), 0, 0);
        tvDate.setLayoutParams(dateParams);
        relativeLayout.addView(tvDate);

        // Description TextView
        TextView tvDescription = new TextView(workoutLayout.getContext());
        tvDescription.setId(View.generateViewId());
        tvDescription.setText(description);
        tvDescription.setTextSize(14);
        tvDescription.setTextColor(Color.BLACK);

        RelativeLayout.LayoutParams descParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.addRule(RelativeLayout.BELOW, tvDate.getId());
        descParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        descParams.setMargins(Math.round(dpToPx(16)), Math.round(dpToPx(4)), 0, 0);
        tvDescription.setLayoutParams(descParams);
        relativeLayout.addView(tvDescription);

        // Edit Button (40dp x 40dp)
        ImageView ivEdit = new ImageView(workoutLayout.getContext());
        ivEdit.setId(View.generateViewId());
        ivEdit.setImageResource(R.drawable.edit_btn);
        RelativeLayout.LayoutParams editParams = new RelativeLayout.LayoutParams(Math.round(dpToPx(40)), Math.round(dpToPx(40)));
        editParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        editParams.addRule(RelativeLayout.CENTER_VERTICAL);
        editParams.setMargins(0, 0, Math.round(dpToPx(16)), 0);
        ivEdit.setLayoutParams(editParams);
        relativeLayout.addView(ivEdit);
        // Set OnClickListener for the Edit Button
        ivEdit.setOnClickListener(v -> {
            if (description.equals("Break Day - Recovery")) {
                //Snackbar.make(getView(), "We recommend you taking a break.\nHowever may continue to execrise by going to record tab", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                Toast.makeText(getContext(), "We recommend you taking a break.", Toast.LENGTH_SHORT).show();
                Toast.makeText(getContext(), "However may continue to execrise by going to record tab.", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString("date", date);
            String exercise = helper.getSportFromDate(date);
            double dist = helper.getDistFromDate(date);
            int status_2 = helper.getStatusFromDate(date);
            bundle.putDouble("dist", dist);
            bundle.putString("sport", exercise);
            bundle.putString("status", String.valueOf(status_2));
            // Action when clicking edit button
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_edit_workout, bundle);
        });

        // Add the created RelativeLayout to the LinearLayout
        workoutLayout.addView(relativeLayout);
    }

    private boolean generateWorkoutPlans(LocalDate startDate, LocalDate endDate, double weight, double height, int age, double weightLossGoal) {

        if (startDate == null || endDate == null) {
            Log.e(TAG, "Error: One or both dates are null!");
            return false;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        double bmr = 10 * weight + 6.25 * height - 5 * age + 5;

        double totalCaloriesToBurn = weightLossGoal * 7700;
        double dailyCalorieDeficit = totalCaloriesToBurn / days;

        double runningDistance = Math.round((dailyCalorieDeficit / 60) / 2) * 2;
        double cyclingDistance = Math.round((dailyCalorieDeficit / 30) / 2) * 2;
        double hikingDistance = Math.round((dailyCalorieDeficit / 50) / 2) * 2;
        double swimmingDistance = Math.round((dailyCalorieDeficit / 100) / 2) * 2;
        if (runningDistance == 0) runningDistance = 1;
        if (cyclingDistance == 0) cyclingDistance = 1;
        if (hikingDistance == 0) hikingDistance = 1;
        if (swimmingDistance == 0) swimmingDistance = 1;

        if (dailyCalorieDeficit > 1000) {
            return false;
        }
        helper.insertTrainingDate(startDate.format(formatter), endDate.format(formatter));

        String[] activities = {"Running", "Cycling", "Hiking", "Swimming"};
        double[] distances = {runningDistance, cyclingDistance, hikingDistance, swimmingDistance};

        int mildDays = (int) (days * 0.3);       // First 30% of days: Mild workouts
        int moderateDays = (int) (days * 0.4);   // Middle 40%: Moderate & Mild
        int highDays = (int) (days - mildDays - moderateDays); // Last 30%: High & Mild

        int i = 0;

        for (int d = 0; d < days; d++) {
            if (d % 3 == 2) {  // Every 3rd day (e.g., 2, 5, 8...) is a Break Day
                addWorkoutView(startDate.format(formatter), "Break Day - Recovery");
            } else {
                String intensity = "";
                String activity = "";
                double distance = 0;

                if (d < mildDays) {  // First 30% of days - Mild
                    intensity = "Mild";
                    activity = activities[i];
                    distance = distances[i];
                    addWorkoutView(startDate.format(formatter), intensity + " " + activity + ": " + (int) distance + " km");
                } else if (d < mildDays + moderateDays) {  // Middle 40% - Moderate & Mild
                    intensity = "Moderate";
                    activity = activities[i];
                    distance = distances[i];
                    addWorkoutView(startDate.format(formatter), intensity + " " + activity + ": " + (int) distance + " km");

                    i = (i + 1) % 4;
                    intensity = "Mild";
                    activity = activities[i];
                    distance = distances[i];
                    addWorkoutView(startDate.format(formatter), intensity + " " + activity + ": " + (int) distance + " km");
                } else {  // Last 30% - High & Mild
                    intensity = "High";
                    activity = activities[i];
                    distance = distances[i];
                    addWorkoutView(startDate.format(formatter), intensity + " " + activity + ": " + (int) distance + " km");

                    i = (i + 1) % 4;
                    intensity = "Mild";
                    activity = activities[i];
                    distance = distances[i];
                    addWorkoutView(startDate.format(formatter), intensity + " " + activity + ": " + (int) distance + " km");
                }

                // Insert the training plan into the database
                long result = helper.insertTrainingPlans(activity, distance, intensity, startDate.format(formatter),false);
                if (result == -1) {
                    Log.e(TAG, "Failed to insert training plan for " + startDate);
                } else {
                    Log.d(TAG, "Successfully inserted training plan for " + startDate);
                }

                i = (i + 1) % 4;
            }
            startDate = startDate.plusDays(1);
        }

        return true;
    }


}