package com.sp.milestrack.ui.about;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentAboutBinding;
import com.sp.milestrack.databinding.FragmentListBinding;
import com.sp.milestrack.databinding.FragmentRecordBinding;

public class About extends Fragment {

    private FragmentAboutBinding binding;

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
