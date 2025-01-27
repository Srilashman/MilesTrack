package com.sp.milestrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class editInfo extends AppCompatActivity {
    private EditText editWeight;
    private EditText editHeight;
    private EditText editAge;
    private EditText editWeightLossGoal;
    private Button edit_info_btn;
    private String user_id = "";
    private Database helper = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        editWeight = findViewById(R.id.editweight);
        editHeight = findViewById(R.id.editheight);
        editAge = findViewById(R.id.editage);
        editWeightLossGoal = findViewById(R.id.editweightlossgoal);
        edit_info_btn = findViewById(R.id.save_edit);
        edit_info_btn.setOnClickListener(save_edit);
        helper = new Database(this);
        user_id = getIntent().getStringExtra("ID");

        if (helper.ifPromptsDone()) {
            Intent mainIntent = new Intent(editInfo.this, MainActivity.class);
            editInfo.this.startActivity(mainIntent);
            editInfo.this.finish();
        }
    }
    private View.OnClickListener save_edit = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Double EditHeight = Double.parseDouble(editHeight.getText().toString());
            Double EditWeight = Double.parseDouble(editWeight.getText().toString());
            Double EditAge = Double.parseDouble(editAge.getText().toString());
            String EditWeightLossGoal = editWeightLossGoal.getText().toString();

            helper.update(user_id, EditHeight, EditWeight, EditAge, EditWeightLossGoal);

            Intent mainIntent1 = new Intent(editInfo.this, MainActivity.class);
            editInfo.this.startActivity(mainIntent1);
            editInfo.this.finish();
        }
    };
}