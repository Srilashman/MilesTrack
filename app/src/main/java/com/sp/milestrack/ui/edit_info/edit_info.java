package com.sp.milestrack.ui.edit_info;

import static android.graphics.Color.parseColor;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sp.milestrack.Database;
import com.sp.milestrack.MainActivity;
import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentAboutBinding;
import com.sp.milestrack.databinding.FragmentEditInfoBinding;
import com.sp.milestrack.promptAgeAndGoal;

import java.text.SimpleDateFormat;
import java.util.Date;

public class edit_info extends Fragment {

    private FragmentEditInfoBinding binding;
    private String user_id = "";
    private Database helper = null;
    private static final String TAG = "MileTrack";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        binding = FragmentEditInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        helper = new Database(requireContext());

//        if (getArguments() != null) {
//            user_id = getArguments().getString("ID");
//        } else {
//            Log.e(TAG, "Arguments are null. Make sure to pass user   ID when creating the fragment.");
//        }
        binding.saveEdit.setOnClickListener(save_edit);

        if (user_id != null) {
            load();
        }
        else {
            Log.d(TAG, "Superman");
        }

        int currentNightMode = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
            binding.textView10.setTextColor(parseColor("#FFFFFF"));
            binding.textView11.setTextColor(parseColor("#FFFFFF"));
            binding.textView12.setTextColor(parseColor("#FFFFFF"));
            binding.textView13.setTextColor(parseColor("#FFFFFF"));
        }
        return root;
    }
    private void load() {
        Cursor c = helper.getLastRecord();
        if (c != null && c.moveToLast()) {
            binding.editheight.setText(String.valueOf(helper.getHeight(c)));
            binding.editweight.setText(String.valueOf(helper.getWeight(c)));
            binding.editage.setText(String.valueOf(helper.getAge(c)));
            binding.editweightlossgoal.setText(String.valueOf(helper.getWeightLossGoal(c)));

            String date = helper.getDate(c);  // Fetching the date
            Log.d(TAG,"Date: " + date);
            c.close();
        } else {
            Log.d(TAG, "No data found for user ID: " + user_id);
        }
        if (c != null) {
            c.close(); // Always close the cursor to prevent memory leaks
        }
    }
    private View.OnClickListener save_edit = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Cursor c = helper.getLastRecord();
            // Ensure cursor is valid and contains data
            if (c != null && c.moveToLast()) {
                if (binding.editheight.getText().toString().isEmpty() ||
                        binding.editweight.getText().toString().isEmpty() ||
                        binding.editage.getText().toString().isEmpty() ||
                        binding.editweightlossgoal.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    c.close(); // Close cursor to prevent memory leaks
                    return;
                }

                Double EditHeight = Double.parseDouble(binding.editheight.getText().toString());
                Double EditWeight = Double.parseDouble(binding.editweight.getText().toString());
                Double EditAge = Double.parseDouble(binding.editage.getText().toString());
                String EditWeightLossGoal = binding.editweightlossgoal.getText().toString().toLowerCase();

                // Validate Weight Loss Goal input
                if (!helper.isValidWeightLossGoal(EditWeightLossGoal)) {
                    Toast.makeText(getContext(), "Please enter a number or 'nil' for the Weight Loss Goal", Toast.LENGTH_SHORT).show();
                    c.close();
                    return;
                }

                double WeightLossGoalValue = helper.parseWeightLossGoal(EditWeightLossGoal);

                // Business logic: Check if weight loss goal is valid
                if (WeightLossGoalValue >= EditWeight && WeightLossGoalValue != 0) {
                    Toast.makeText(getContext(), "Weight loss goal cannot be higher than or equal to your current weight!", Toast.LENGTH_LONG).show();
                    c.close();
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String currentDate = sdf.format(new Date());


                String user_id = helper.getID(c);
                if (user_id != null) {
                    helper.insert(currentDate, EditHeight, EditWeight, EditAge, WeightLossGoalValue);
                    Log.d(TAG, "User_id not null" + user_id);
                } else {
                    Log.d(TAG, EditWeightLossGoal);
                    Log.d(TAG, "User_id null");
                }

                c.close(); // Always close the cursor after use

                Intent mainIntent1 = new Intent(getActivity(), MainActivity.class);
                startActivity(mainIntent1);
                requireActivity().finish();
            } else {
                Log.d(TAG, "No records found. Cannot update.");
                if (c != null) c.close();
            }
        }
    };
}