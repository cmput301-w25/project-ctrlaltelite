package com.example.ctrlaltelite;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class MoodEventAdapter extends ArrayAdapter<MoodEvent> {

    private Context context;
    private List<MoodEvent> moodEvents;

    public MoodEventAdapter(Context context, List<MoodEvent> moodEvents) {
        super(context, 0, moodEvents);
        this.context = context;
        this.moodEvents = moodEvents;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.mood_event_item, parent, false);
        }

        MoodEvent moodEvent = moodEvents.get(position);

        TextView moodText = listItem.findViewById(R.id.mood_text);
        TextView socialSituationText = listItem.findViewById(R.id.social_situation_text);
        TextView triggerText = listItem.findViewById(R.id.trigger_text);
        TextView timestampText = listItem.findViewById(R.id.timestamp_text);

        moodText.setText(moodEvent.getEmotionalState());
        socialSituationText.setText(moodEvent.getSocialSituation());
        triggerText.setText(moodEvent.getTrigger());
        timestampText.setText(moodEvent.getTimestamp());

        return listItem;
    }
}
