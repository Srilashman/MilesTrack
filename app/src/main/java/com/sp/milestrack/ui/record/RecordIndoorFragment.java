package com.sp.milestrack.ui.record;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sp.milestrack.Database;
import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentRecordBinding;


public class RecordIndoorFragment extends Fragment {
    private FragmentRecordBinding binding;
    private Button indoor_btn;
    private Button outdoor_btn;
    private Database helper = null;
    private static final String TAG = "MileTrack";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRecordBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
//        return inflater.inflate(R.layout.fragment_record_indoor, container, false);

        helper = new Database(requireContext());
//        // Set click listener for outdoor button
//        binding.outdoorbtn.setOnClickListener(record_map);
//
//        // Display a sample Toast (you can remove this later)
//        Toast.makeText(getContext(), "Hello", Toast.LENGTH_LONG).show();
//    }
        return root;
    }
    private View.OnClickListener record_map = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_indoor_to_outdoor);
        }
    };
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        indoor_btn = view.findViewById(R.id.indoorbtn);
        outdoor_btn = view.findViewById(R.id.outdoorbtn);
        indoor_btn.setEnabled(false);
        outdoor_btn.setOnClickListener(record_map);
        Toast.makeText(getContext(), "HEllo", Toast.LENGTH_LONG);

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}