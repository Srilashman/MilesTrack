package com.sp.milestrack.ui.list;

import static android.graphics.Color.parseColor;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.sp.milestrack.Database;
import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentListBinding;
import androidx.cursoradapter.widget.CursorAdapter;


public class ListFragment extends Fragment {

    private FragmentListBinding binding;
    private Cursor model = null;
    private RecordAdapter adapter = null;
    private ListView list;
    private Database helper = null;
    private static final String TAG = "MileTrack";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ListViewModel listViewModel =
                new ViewModelProvider(this).get(ListViewModel.class);

        binding = FragmentListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        helper = new Database(getContext());
        list = binding.records;
        model = helper.getAll();
        adapter = new RecordAdapter(getContext(), model, 0);
        list.setAdapter(adapter);

        // Handle visibility of "No Data Recorded" TextView
        TextView noDataTextView = binding.textSlideshow;
        if (model != null && model.getCount() > 0) {
            noDataTextView.setVisibility(View.GONE);  // Hide the text if data exists
        } else {
            noDataTextView.setVisibility(View.VISIBLE);  // Show the text if no data
        }

//        final TextView textView = binding.textSlideshow;
//        listViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public class RecordAdapter extends CursorAdapter {

        public RecordAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Bind the data from the cursor to the TextViews in your row.xml layout
            TextView dateTextView = view.findViewById(R.id.listdate);  // Date TextView
            TextView timeTextView = view.findViewById(R.id.textView29);  // Duration TextView
            TextView distanceTextView = view.findViewById(R.id.textView27);  // Distance TextView
            TextView caloriesTextView = view.findViewById(R.id.textView30);  // Calories TextView
            ImageView activity_icon = view.findViewById(R.id.imageView5);
            ImageView routeImageView = view.findViewById(R.id.imageView10);  // Your ImageView for the route
            ImageView deleteImageView = view.findViewById(R.id.imageView8);  // This is the delete button

            TextView distanceText = view.findViewById(R.id.textView26);  // Duration TextView
            TextView caloriesText = view.findViewById(R.id.textView31);  // Distance TextView
            TextView durationText = view.findViewById(R.id.textView28);  // Calories TextView
            View dividerView = view.findViewById(R.id.view); // Divider view
            View dividerView1 = view.findViewById(R.id.view1); // Another divider view

            // Retrieve values from the cursor
            // Safely get column indices
            int dateIndex = cursor.getColumnIndex("date");  // Adjust column name
            int distanceIndex = cursor.getColumnIndex("distance");
            int durationIndex = cursor.getColumnIndex("duration");
            int caloriesIndex = cursor.getColumnIndex("calories");
            int activityIndex = cursor.getColumnIndex("activity");
            int idIndex = cursor.getColumnIndex("_id");  // Assuming there's an _id column for the record

            // Initialize variables with default values in case columns are missing
            String date = (dateIndex != -1) ? cursor.getString(dateIndex) : "N/A";
            String distance = (distanceIndex != -1) ? cursor.getString(distanceIndex) : "N/A";
            String duration = (durationIndex != -1) ? cursor.getString(durationIndex) : "N/A";
            String calories = (caloriesIndex != -1) ? cursor.getString(caloriesIndex) : "N/A";
            String activity = (activityIndex != -1) ? cursor.getString(activityIndex) : "N/A";
            final long id = (idIndex != -1) ? cursor.getLong(idIndex) : -1;  // Get the record ID

            if (!distance.equals("N/A")) {
                double dist = Double.parseDouble(distance);
                distance = String.format("%.2f", dist);  // Format to 2 decimal places
            }
            if (!calories.equals("N/A")) {
                double cal = Double.parseDouble(calories);
                calories = String.format("%.2f", cal);  // Format to 2 decimal places
            }

            // Set the values to the TextViews
            dateTextView.setText(date);
            timeTextView.setText(duration + "min");
            distanceTextView.setText(distance + " km");
            caloriesTextView.setText(calories);

            if (activity.equals("Running")) {
                activity_icon.setImageResource(R.drawable.running_icon); }
            else if (activity.equals("Swimming")) {
                activity_icon.setImageResource(R.drawable.swimming_icon); }
            else if (activity.equals("Hiking")) {
                activity_icon.setImageResource(R.drawable.hiking_icon); }
            else if (activity.equals("Cycling")) {
                activity_icon.setImageResource(R.drawable.cycling_icon); }

            int currentNightMode = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
                dateTextView.setTextColor(parseColor("#FFFFFF"));
                timeTextView.setTextColor(parseColor("#FFFFFF"));
                distanceTextView.setTextColor(parseColor("#FFFFFF"));
                caloriesTextView.setTextColor(parseColor("#FFFFFF"));
                distanceText.setTextColor(parseColor("#FFFFFF"));
                caloriesText.setTextColor(parseColor("#FFFFFF"));
                durationText.setTextColor(parseColor("#FFFFFF"));
                dividerView.setBackgroundColor(Color.WHITE);  // Change View color
                dividerView1.setBackgroundColor(Color.WHITE);  // Change second View color
            }

            // Set up the delete button click listener
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (id != -1) {
                        // Delete the record from the database
                        helper.deleteRecord(id);
                        Cursor newCursor = helper.getAll();
                        // Notify the adapter that the data set has changed
                        swapCursor(helper.getAll());
                        // Show "No Data Recorded" if no records remain
                        TextView noDataTextView = ((Activity) context).findViewById(R.id.text_slideshow);
                        if (newCursor.getCount() == 0) {
                            noDataTextView.setVisibility(View.VISIBLE);
                        } else {
                            noDataTextView.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // Inflate the row layout for each item in the list
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(R.layout.row, parent, false);
        }
    }
}