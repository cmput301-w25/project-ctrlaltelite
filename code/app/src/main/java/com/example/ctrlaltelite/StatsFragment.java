package com.example.ctrlaltelite;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//// created with guidance from ChatGPT (OpenAI), March 30, 2025
public class StatsFragment extends Fragment {

    private PieChart moodPieChart;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        moodPieChart = view.findViewById(R.id.moodPieChart);


        loadFirestoreData();

        return view;
    }

    private void loadFirestoreData() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString("user", "");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodEventsRef = db.collection("Mood Events");

        // Get timestamp for 30 days ago
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        Timestamp thirtyDaysAgo = new Timestamp(calendar.getTime());

        // query Firestore for entries in the last 30 days
        moodEventsRef
                .whereEqualTo("username", currentUsername)
                .whereGreaterThanOrEqualTo("timestamp", thirtyDaysAgo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> moodCounts = new HashMap<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String mood = doc.getString("emotionalState");
                        if (mood != null) {
                            moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);
                        }
                    }
                    List<PieEntry> entries = new ArrayList<>();
                    List<Integer> entryColors = new ArrayList<>();

                    // 🎨 Define color per emotion
                    Map<String, Integer> emotionColors = new HashMap<>();
                    emotionColors.put("😊 Happy", 0xFFFFC107);      // Amber
                    emotionColors.put("😢 Sad", 0xFF2196F3);        // Blue
                    emotionColors.put("😲 Surprised", 0xFFFF5722);  // Orange
                    emotionColors.put("😡 Angry", 0xFFD32F2F);      // Red
                    emotionColors.put("🤢 Disgust", 0xFF4CAF50);    // Green
                    emotionColors.put("😕 Confusion", 0xFF9C27B0);  // Purple
                    emotionColors.put("😨 Fear", 0xFF3F51B5);       // Indigo
                    emotionColors.put("😳 Shame", 0xFFFF8DAA);

                    for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
                        entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                        Integer color = emotionColors.get(entry.getKey());
                        if (color != null) {
                            entryColors.add(color);
                        } else {
                            entryColors.add(ColorTemplate.MATERIAL_COLORS[entryColors.size() % ColorTemplate.MATERIAL_COLORS.length]);
                        }
                    }
                    PieDataSet dataSet = new PieDataSet(entries, "");
                    dataSet.setValueTextColor(Color.BLACK);
                    dataSet.setColors(entryColors);
                    PieData data = new PieData(dataSet);
                    data.setValueTextColor(Color.BLACK);
                    moodPieChart.setData(data);
                    moodPieChart.setEntryLabelColor(Color.BLACK);
                    moodPieChart.getDescription().setEnabled(false);
                    moodPieChart.invalidate();

                    Log.d("StatsFragment", "Last 30 days mood data: " + moodCounts.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("StatsFragment", "Error loading mood data", e);
                });


    }


}
