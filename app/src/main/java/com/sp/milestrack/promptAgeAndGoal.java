package com.sp.milestrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class promptAgeAndGoal extends AppCompatActivity {
    private Button submit_btn;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prompt_ag);
        submit_btn = findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(submit);
    }
    private View.OnClickListener submit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent mainIntent = new Intent(promptAgeAndGoal.this, MainActivity.class);
            promptAgeAndGoal.this.startActivity(mainIntent);
            promptAgeAndGoal.this.finish();
        }
    };
}
