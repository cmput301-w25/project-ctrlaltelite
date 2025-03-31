package com.example.ctrlaltelite;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ctrlaltelite.ChatActivity;
import com.example.ctrlaltelite.R;
import com.example.ctrlaltelite.ChatroomModel;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    private Context context;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String currentUserId;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context, String currentUserId) {
        super(options);
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.currentUserId = currentUserId;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        String otherUserId = "";
        for (String id : model.getUserIds()) {
            if (!id.equals(currentUserId)) {
                otherUserId = id;
                break;
            }
        }

        db.collection("users")
                .whereEqualTo("username", otherUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        User otherUser = documentSnapshot.toObject(User.class);

                        holder.usernameText.setText(otherUser.getUsername());

                        boolean lastSentByMe = model.getLastMessageSenderId().equals(currentUserId);
                        holder.lastMessageText.setText(lastSentByMe ? "You: " + model.getLastMessage() : model.getLastMessage());

                        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                                .format(model.getLastMessageTimestamp().toDate());
                        holder.lastMessageTime.setText(time);

                        holder.itemView.setOnClickListener(v -> {
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("chat_user", otherUser);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("username", otherUser.getUsername());
                            intent.putExtra("displayName", otherUser.getDisplayName());
                            context.startActivity(intent);
                        });
                    }
                });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    static class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, lastMessageText, lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view); // From profile_pic_view.xml
        }
    }
}
