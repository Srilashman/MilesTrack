package com.sp.milestrack;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class promptAgeAndGoal extends AppCompatActivity {
    private EditText age;
    private EditText weightLossGoal;
    private Button submit_btn;
    private Database helper = null;
    private double height;
    private double weight;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prompt_ag);

        age = findViewById(R.id.age);
        weightLossGoal = findViewById(R.id.weightLossGoal);
        submit_btn = findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(submit);
        helper = new Database(this);

        age.addTextChangedListener(blankCheck);
        weightLossGoal.addTextChangedListener(blankCheck);
        submit_btn.setEnabled(false);

        Intent intent = getIntent();
        height = intent.getDoubleExtra("height", 0);
        weight = intent.getDoubleExtra("weight", 0);
    }
    private View.OnClickListener submit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Double Age = Double.parseDouble(age.getText().toString());
            String WeightLossGoal = weightLossGoal.getText().toString();

            // Insert all values into the database
            helper.insert(height, weight, Age, WeightLossGoal);
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
}
