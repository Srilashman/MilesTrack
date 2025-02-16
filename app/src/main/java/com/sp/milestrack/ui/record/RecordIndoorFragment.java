package com.sp.milestrack.ui.record;

import static android.graphics.Color.parseColor;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sp.milestrack.Database;
import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentRecordBinding;
import com.sp.milestrack.databinding.FragmentRecordIndoorBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RecordIndoorFragment extends Fragment {
    private FragmentRecordIndoorBinding binding;
    private Button indoor_btn;
    private Button outdoor_btn;
    private Database helper = null;
    private static final String TAG = "MileTrack";
    private CalendarView calendar;
    private ImageView cal;
    private TextView dateInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRecordIndoorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
//        return inflater.inflate(R.layout.fragment_record_indoor, container, false);

        helper = new Database(requireContext());

        // Set the Save button's OnClickListener using binding
        binding.saveInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecord();
            }
        });

        int currentNightMode = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
            binding.textView.setTextColor(parseColor("#FFFFFF"));
            binding.textView5.setTextColor(parseColor("#FFFFFF"));
            binding.textView9.setTextColor(parseColor("#FFFFFF"));
            binding.textView14.setTextColor(parseColor("#FFFFFF"));
            binding.textView14.setTextColor(parseColor("#FFFFFF"));

        }
        calendar = root.findViewById(R.id.calendar);
        cal = root.findViewById(R.id.cal_icon);
        dateInput = root.findViewById(R.id.dateInput);
        return binding.getRoot();
    }
    private View.OnClickListener record_map = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_indoor_to_outdoor);
        }
    };
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        indoor_btn = view.findViewById(R.id.indoorbtn);
        outdoor_btn = view.findViewById(R.id.outdoorbtn);
        indoor_btn.setEnabled(false);
        outdoor_btn.setOnClickListener(record_map);
        calendar.setVisibility(View.GONE);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Handle the date selection event
                String selectedDate = dayOfMonth + "-" + (month + 1) + "-" + year;
                //Toast.makeText(getContext(), "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();
                dateInput.setText(selectedDate);
                calendar.setVisibility(View.GONE);
            }
        });
        cal.setOnClickListener(toggleCal);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void saveRecord() {
        // Get the values from the EditText fields using binding
        String date = binding.dateInput.getText().toString();
        String time = binding.timeInput.getText().toString();
        String sport = binding.sportsInput.getText().toString();
        String distance = binding.distanceInput.getText().toString();
        String duration = binding.durationInput.getText().toString();
        String calories = binding.caloriesInput.getText().toString();

        // Validate inputs (you can enhance this with more validations)
        if (date.isEmpty() || time.isEmpty() || sport.isEmpty() || distance.isEmpty() || duration.isEmpty() || calories.isEmpty()) {
            // Show an error message (You can show a Toast or an AlertDialog here)
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate the time format
        if (!isValidTimeFormat(time)) {
            Toast.makeText(getContext(), "Invalid time format. Please use HH:mm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format the date and time together
        String formattedDateTime = formatDateTime(date, time);

        // Insert data into the database (assuming you have a method to insert a record in your Database helper)
        long isInserted = helper.insertrecord(formattedDateTime, Double.parseDouble(distance), duration, Double.parseDouble(calories), sport);

        // Show a message based on whether the insert was successful
//        if (isInserted != -1) {
//            Toast.makeText(getContext(), "Record saved successfully", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getContext(), "Failed to save record", Toast.LENGTH_SHORT).show();
//        }

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.nav_list);
    }

    private String formatDateTime(String date, String time) {
        try {
            // Combine the date and time into one string
            String dateTimeString = date + " " + time;

            // Define the format of the input date and time (adjust based on your input format)
            SimpleDateFormat inputFormat = new SimpleDateFormat("d/M/yyyy h:mm"); // Input format: 3/2/2025 2:35
            Date parsedDate = inputFormat.parse(dateTimeString); // Parse the combined string to Date

            // Define the desired output format
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy  h:mm a"); // Output format: 03 Feb 2025 2:35 PM

            // Return the formatted date-time string
            return outputFormat.format(parsedDate);
        } catch (ParseException e) {

            e.printStackTrace();
            return null; // Return null in case of error
        }
    }

    private boolean isValidTimeFormat(String time) {
        // Define the 24-hour time format
        SimpleDateFormat timeFormat24Hour = new SimpleDateFormat("HH:mm");
        timeFormat24Hour.setLenient(false);  // Set lenient to false to prevent invalid times like "25:00"

        try {
            // Try parsing the time with the 24-hour format
            timeFormat24Hour.parse(time);

            // Check if hours and minutes are within valid ranges (00-23 for hours, 00-59 for minutes)
            String[] timeParts = time.split(":");
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);

            if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
                return true;
            } else {
                return false; // Invalid time if hours or minutes are out of range
            }
        } catch (ParseException e) {
            // If parsing fails, it's an invalid time format
            return false;
        }
    }
    private View.OnClickListener toggleCal = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (calendar.getVisibility() == View.VISIBLE) {
                calendar.animate().alpha(0.0f).setDuration(300).withEndAction(() -> calendar.setVisibility(View.GONE));
            } else {
                calendar.setVisibility(View.VISIBLE);
                calendar.animate().alpha(1.0f).setDuration(300);
            }

        }
    };
}