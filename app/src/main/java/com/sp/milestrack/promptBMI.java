package com.sp.milestrack;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class promptBMI extends AppCompatActivity {
    private EditText weight;
    private EditText height;
    private Button next_prompt_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prompt_bmi);

        weight = findViewById(R.id.weightLossGoal);
        height = findViewById(R.id.age);
        next_prompt_btn = findViewById(R.id.submit_btn);
        next_prompt_btn.setOnClickListener(next_prompt);

        weight.addTextChangedListener(blankCheck);
        height.addTextChangedListener(blankCheck);
        next_prompt_btn.setEnabled(false);
    }
    private View.OnClickListener next_prompt = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent mainIntent = new Intent(promptBMI.this, promptAgeAndGoal.class);
            promptBMI.this.startActivity(mainIntent);
            promptBMI.this.finish();
        }
    };
    TextWatcher blankCheck = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String Weight = weight.getText().toString().trim();
            String Height = height.getText().toString().trim();
            next_prompt_btn.setEnabled(!Weight.isEmpty() && !Height.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

}

