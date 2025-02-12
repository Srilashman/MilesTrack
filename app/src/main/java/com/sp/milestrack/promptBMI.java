package com.sp.milestrack;

import static android.graphics.Color.parseColor;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class promptBMI extends AppCompatActivity {
    private EditText weight;
    private EditText height;

    private Button next_prompt_btn;
    private Database helper = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prompt_bmi);

        weight = findViewById(R.id.weight);
        height = findViewById(R.id.height);
        next_prompt_btn = findViewById(R.id.submit_btn);
        next_prompt_btn.setOnClickListener(next_prompt);
        next_prompt_btn.setOnFocusChangeListener(checkFocus);
        helper = new Database(this);

        weight.addTextChangedListener(blankCheck);
        height.addTextChangedListener(blankCheck);
        next_prompt_btn.setEnabled(false);
        if (helper.ifPromptsDone()) {
            Intent mainIntent = new Intent(promptBMI.this, MainActivity.class);
            promptBMI.this.startActivity(mainIntent);
            promptBMI.this.finish();
        }

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
            height.setTextColor(parseColor("#000000"));
            weight.setTextColor(parseColor("#000000"));
        }
    }
    private View.OnClickListener next_prompt = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Double Height = Double.parseDouble(height.getText().toString());
            Double Weight = Double.parseDouble(weight.getText().toString());

            Intent mainIntent1 = new Intent(promptBMI.this, promptAgeAndGoal.class);
            mainIntent1.putExtra("height", Height);
            mainIntent1.putExtra("weight", Weight);
            promptBMI.this.startActivity(mainIntent1);
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

