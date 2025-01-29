package com.sp.milestrack.ui.record;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;


import com.sp.milestrack.MainActivity;
import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentRecordBinding;
import com.sp.milestrack.promptAgeAndGoal;

public class RecordFragment extends Fragment {

    private FragmentRecordBinding binding;
    private Button indoor_btn;
    private Button outdoor_btn;
    private AppBarConfiguration mAppBarConfiguration;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RecordViewModel recordViewModel =
                new ViewModelProvider(this).get(RecordViewModel.class);

        binding = FragmentRecordBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textGallery;
//        recordViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        Spinner spinner = (Spinner) root.findViewById(R.id.sport_choice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.sports_array,
                R.layout.spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setBackgroundColor(Color.rgb(0, 255, 0));
        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);
        indoor_btn = root.findViewById(R.id.indoor_btn);
        indoor_btn.setOnClickListener(indoor_form);
        outdoor_btn = root.findViewById(R.id.outdoor_btn);
        outdoor_btn.setEnabled(false);
        return root;
    }
    private View.OnClickListener indoor_form = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_outdoor_to_indoor);
        }
    };
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}