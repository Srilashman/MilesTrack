package com.sp.milestrack;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

public class LineChartViewModel extends ViewModel {
    private final MutableLiveData<List<Entry>> chartData = new MutableLiveData<>();

    public LiveData<List<Entry>> getChartData() {
        return chartData;
    }

    public void setChartData(List<Entry> data) {
        chartData.setValue(data);
    }
}
