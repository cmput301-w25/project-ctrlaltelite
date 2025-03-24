package com.example.ctrlaltelite;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;

import java.util.List;

/**
 * Custom adapter for a spinner that changes text color based on the selected mood.
 */
public class
CustomSpinnerAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;

    private int spinnerType;  // 0 for emotional state, 1 for social situation


    /**
     * Constructor for the CustomSpinnerAdapter.
     *
     * @param context The current context.
     * @param items   The list of mood options.
     */
    public CustomSpinnerAdapter(Context context, List<String> items, int spinnerType) {
        super(context, android.R.layout.simple_spinner_item, items);
        this.context = context;
        this.items = items;
        this.spinnerType = spinnerType;
    }


    /**
     * Returns the selected item view with custom text color.
     *
     * @param position    The selected item's position.
     * @param convertView The recycled view.
     * @param parent      The parent ViewGroup.
     * @return The view for the selected item.
     */
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


    /**
     * Returns the dropdown view for each item in the spinner.
     *
     * @param position    The position of the item.
     * @param convertView The recycled view.
     * @param parent      The parent ViewGroup.
     * @return The view for the dropdown item.
     */
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_dropdown_item, parent, false);
        }
        TextView textView = convertView.findViewById(R.id.spinner_dropdown_text);
        textView.setText(items.get(position));
        // Check if it's for emotional state or social situation and set the text color
        if (spinnerType == 0) { // Emotional state spinner
            textView.setTextColor(getColorForPosition(position)); // Set text color to black for emotional state
        } else if (spinnerType == 1) { // Social situation spinner
            textView.setTextColor(Color.BLACK);  // Set text color to gray for social situation
        }
        textView.setTextSize(18);

        // For the first item, adding "drop-up" arrow
        if (position == 0) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_arrow_drop_up_24, 0);
            textView.setCompoundDrawablePadding(8);
        } else {
            // To ensure other items have no compound drawable in case views are recycled
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        return convertView;
    }


    /**
     * Returns the color corresponding to the mood at the given position.
     *
     * @param position The index of the mood in the list.
     * @return The color code for the mood.
     */
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
