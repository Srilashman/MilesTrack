package com.sp.milestrack;

import static android.graphics.Color.parseColor;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sp.milestrack.ui.edit_info.edit_info;

import java.text.SimpleDateFormat;
import java.util.Date;

public class promptAgeAndGoal extends AppCompatActivity {
    private EditText age;
    private EditText weightLossGoal;
    private Button submit_btn;
    private Database helper = null;
    private double height;
    private double weight;
    private static final String TAG = "MileTrack";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prompt_ag);

        age = findViewById(R.id.age);
        weightLossGoal = findViewById(R.id.weightLossGoal);
        submit_btn = findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(submit);
        submit_btn.setOnFocusChangeListener(checkFocus);
        helper = new Database(this);

        age.addTextChangedListener(blankCheck);
        weightLossGoal.addTextChangedListener(blankCheck);
        submit_btn.setEnabled(false);

        Intent intent = getIntent();
        height = intent.getDoubleExtra("height", 0);
        weight = intent.getDoubleExtra("weight", 0);

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
            age.setTextColor(parseColor("#000000"));
            weightLossGoal.setTextColor(parseColor("#000000"));
        }
    }
    private View.OnClickListener submit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Integer Age = Integer.parseInt(age.getText().toString());
            String WeightLossGoal = weightLossGoal.getText().toString().toLowerCase();

            // Validate Weight Loss Goal input
            if (!helper.isValidWeightLossGoal(WeightLossGoal)) {
                Toast.makeText(promptAgeAndGoal.this, "Please enter a number or 'nil' for the Weight Loss Goal", Toast.LENGTH_SHORT).show();
                helper.close();
                return;
            }

            String WeightLossGoalValue = helper.parseWeightLossGoal(WeightLossGoal);
            Double weightLossGoalDouble = Double.parseDouble(WeightLossGoalValue);

            // Business logic: Check if weight loss goal is valid
            if (weightLossGoalDouble >= weight && WeightLossGoalValue != "0") {
                Toast.makeText(promptAgeAndGoal.this, "Weight loss goal cannot be higher than or equal to your current weight!", Toast.LENGTH_LONG).show();
                helper.close();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = sdf.format(new Date());

            // Insert all values into the database
            long userId = helper.insert(currentDate, height, weight, Age, WeightLossGoalValue);
            Log.d(TAG, "Inserted User ID: " + userId + ", " + currentDate);

            Intent mainIntent = new Intent(promptAgeAndGoal.this, MainActivity.class);
            promptAgeAndGoal.this.startActivity(mainIntent);
            promptAgeAndGoal.this.finish();
        }
    };
    TextWatcher blankCheck = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String Age = age.getText().toString().trim();
            String WeightLossGoal = weightLossGoal.getText().toString().trim();
            submit_btn.setEnabled(!Age.isEmpty() && !WeightLossGoal.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    private View.OnFocusChangeListener checkFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0); // hide keyboard when button is focused
                }
            }
        }
    };
}
