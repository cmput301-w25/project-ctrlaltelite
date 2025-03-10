package com.example.ctrlaltelite;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;

    public CustomSpinnerAdapter(Context context, List<String> items) {
        super(context, android.R.layout.simple_spinner_item, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        TextView textView = (TextView) convertView;
        textView.setText(items.get(position));
        textView.setTextColor(getColorForPosition(position));
        textView.setTextSize(18);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }
        TextView textView = (TextView) convertView;
        textView.setText(items.get(position));
        textView.setTextColor(getColorForPosition(position));
        textView.setTextSize(18);
        return convertView;
    }

    private int getColorForPosition(int position) {
        switch (position) {
            case 1: return 0xFFFFC107; // Happy (Amber)
            case 2: return 0xFF2196F3; // Sad (Blue)
            case 3: return 0xFFFF5722; // Surprised (Orange)
            case 4: return 0xFFD32F2F; // Angry (Red)
            case 5: return 0xFF4CAF50; // Disgust (Green)
            case 6: return 0xFF9C27B0; // Confusion (Purple)
            default: return 0xFF616161; // Default (Gray)
        }
    }
}
