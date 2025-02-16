package com.sp.milestrack.ui.edit_info;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentEditInfoBinding;
import com.sp.milestrack.databinding.FragmentEditWorkoutBinding;
import com.sp.milestrack.Database;
public class editWorkout extends Fragment {
    private FragmentEditWorkoutBinding binding;
    TextView dateView;
    EditText editDist;
    Button finishEdit;
    String sport;
    String date;
    double dist;
    int status;
    CheckBox checkAsComplete;
    private Database helper = null;


    public editWorkout() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentEditWorkoutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Spinner spinner = (Spinner) root.findViewById(R.id.sport_choice_editing);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.sports_array,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears.
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Bundle bundle = getArguments();
        sport = "";
        date = "";
        dist = -1;
        if (bundle != null) {
            sport = bundle.getString("sport", "");
            date = bundle.getString("date", "");
            dist = bundle.getDouble("dist", -2);
            status = bundle.getInt("status", -1);
        }
//        Toast.makeText(getContext(), sport, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getContext(), date, Toast.LENGTH_SHORT).show();
//        Toast.makeText(getContext(), String.valueOf(dist), Toast.LENGTH_SHORT).show();
        spinner.setAdapter(adapter);
        if (sport != null) {
            int spinnerPosition = adapter.getPosition(sport);
            spinner.setSelection(spinnerPosition);
        }
        dateView = root.findViewById(R.id.workout_date);
        dateView.setText(date);
        editDist = root.findViewById(R.id.editDistanceTarget);
        editDist.setHint(String.valueOf(dist));
        finishEdit = root.findViewById(R.id.finish_edit);
        finishEdit.setOnClickListener(checkInputs);
        checkAsComplete = root.findViewById(R.id.checkbox_completed);
        helper = new Database(requireContext());
        return root;
    }
    private View.OnClickListener checkInputs = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Spinner spinner = requireView().findViewById(R.id.sport_choice_editing);
            String text = (spinner.getSelectedItem() != null) ? spinner.getSelectedItem().toString() : sport;
            boolean changeCheckBox = false;
            Bundle bundle = new Bundle();
            if (checkAsComplete.isChecked() && status == 0) {
                if (editDist.getText().toString().isEmpty()) bundle.putDouble("add_dist", dist);
                else bundle.putDouble("add_dist", Double.parseDouble(String.valueOf(editDist.getText())));
                changeCheckBox = true;
            }
            if (!checkAsComplete.isChecked() && status != 0) {
                if (editDist.getText().toString().isEmpty()) bundle.putDouble("minus_dist", dist);
                else bundle.putDouble("minus_dist", Double.parseDouble(String.valueOf(editDist.getText())));
                changeCheckBox = true;
            }
            if (text == sport && editDist.getText() == null && checkAsComplete.isChecked() == (status != 0)) {
                return; // no change Double.parseDouble(String.valueOf(editDist.getText()))
            } else if (editDist.getText().toString().isEmpty()) {
                helper.updateTrainingPlansByDate(date, (checkAsComplete.isChecked()), dist, text);
                NavController navController = Navigation.findNavController(requireView());
                if (changeCheckBox) navController.navigate(R.id.action_finish_edit, bundle);
                else navController.navigate(R.id.action_finish_edit);
            } else {
                helper.updateTrainingPlansByDate(date, (checkAsComplete.isChecked()), Double.parseDouble(editDist.getText().toString()), text);
                NavController navController = Navigation.findNavController(requireView());
                if (changeCheckBox) navController.navigate(R.id.action_finish_edit, bundle);
                else navController.navigate(R.id.action_finish_edit);
            }
        }
    };
}