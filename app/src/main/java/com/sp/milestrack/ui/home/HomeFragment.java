package com.sp.milestrack.ui.home;

import static android.graphics.Color.parseColor;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sp.milestrack.LineChartViewModel;
import com.sp.milestrack.R;
import com.sp.milestrack.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private LineChart lineChart;
    private LineChartViewModel lineChartViewModel;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        // Initialize the LineChart
        lineChart = root.findViewById(R.id.chart);

        // Set up the ViewModel
        lineChartViewModel = new ViewModelProvider(this).get(LineChartViewModel.class);

        // Observe the chart data
        lineChartViewModel.getChartData().observe(getViewLifecycleOwner(), this::updateLineChart);

        // Simulate data update
        simulateData();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void updateLineChart(List<Entry> entries) {
        if (entries != null) {
            LineDataSet dataSet = new LineDataSet(entries, "Sample Data");
            dataSet.setColor(getResources().getColor(R.color.purple_200));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setValueTextSize(10f);
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
                dataSet.setValueTextColor(parseColor("#FFFFFF"));
            }
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.invalidate(); // Refresh the chart
        }
    }

    private void simulateData() {
        // Simulate adding data to the chart
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 1));
        entries.add(new Entry(1, 3));
        entries.add(new Entry(2, 5));
        entries.add(new Entry(3, 2));
        int currentNightMode = getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) { // if user in dark mode, change text color to white
            lineChart.getAxisLeft().setTextColor(parseColor("#FFFFFF")); // left y-axis
            lineChart.getXAxis().setTextColor(parseColor("#FFFFFF"));
            lineChart.getLegend().setTextColor(parseColor("#FFFFFF"));
            lineChart.getDescription().setTextColor(parseColor("#FFFFFF"));
        }
        lineChartViewModel.setChartData(entries);
    }
}