package com.sp.milestrack;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
public class promptBMI extends AppCompatActivity {
    private double weight;
    private double height;
    private Button next_prompt_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prompt_bmi);
        next_prompt_btn = findViewById(R.id.next_prompt_btn);
        next_prompt_btn.setOnClickListener(next_prompt);
    }
    private View.OnClickListener next_prompt = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent mainIntent = new Intent(promptBMI.this, promptAgeAndGoal.class);
            promptBMI.this.startActivity(mainIntent);
            promptBMI.this.finish();
        }
    };
}
