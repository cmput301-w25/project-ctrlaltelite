package com.example.ctrlaltelite;

import static androidx.test.InstrumentationRegistry.getContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.easychat.adapter.ChatRecyclerAdapter;
//import com.example.easychat.adapter.SearchUserRecyclerAdapter;
//import com.example.easychat.model.ChatMessageModel;
import com.example.ctrlaltelite.ChatroomModel;
//import com.example.easychat.model.UserModel;
//import com.example.easychat.utils.AndroidUtil;
//import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

//import org.checkerframework.checker.units.qual.C;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Time;
import java.util.Arrays;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;

/**
 * Functionality for chatting with other users directly (direct messaging)
 */
public class ChatActivity extends AppCompatActivity {

    String chatroomId;

    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageView;
    ChatRecyclerAdapter adapter;

    /**
     * Functionality to create the chat activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        recyclerView = findViewById(R.id.chat_recycler_view);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        messageInput = findViewById(R.id.chat_message_input); // Replace with actual ID

        String displayNameofOther = getIntent().getStringExtra("displayName");
        String OtherUserUserName = getIntent().getStringExtra("username");

        // Get data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString("display_name", "");  // Default to empty string if not found // Get the display name of the logged-in user

        String currentUserID = sharedPreferences.getString("user", "");

        // Get the clicked user's username and display name from intent
        String otherUsernameId = getIntent().getStringExtra("username");

        if (displayNameofOther != null) {
            otherUsername.setText(displayNameofOther);
        } else {
            Toast.makeText(this, "Username not found!", Toast.LENGTH_SHORT).show();
        }

        // Generate unique chatroom ID using unique usernames or IDs
        chatroomId = generateChatroomId(currentUserID, otherUsernameId);

        backBtn.setOnClickListener((v) -> onBackPressed());

        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMessageToUser(message);
        }));

        getOrCreateChatroomModel(); // proceed to load chat
        setupChatRecyclerView();

    }

    /**
     * Setting up the chat recycler view
     */
    void setupChatRecyclerView() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestore query to fetch messages in descending order by timestamp
        Query query = db.collection("chatrooms")
                .document(chatroomId)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class)
                .setLifecycleOwner(this)  // So you don’t need to manually start/stop adapter
                .build();

        adapter = new ChatRecyclerAdapter(options, this); // Pass context correctly

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true); // Most recent messages on top
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        // Optional: auto-scroll when a new message appears
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    /**
     * Generating a chatroom ID between two users that are chatting
     * @param user1
     * @param user2
     * @return a string which becomes the ID
     */
    private String generateChatroomId(String user1, String user2) {
        if (user1.compareTo(user2) < 0) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }

    /**
     * A getter for the chatroom model, or creating one if it does not exist yet
     */
    void getOrCreateChatroomModel() {
        // Get user info from SharedPreferences (current user)
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("user", ""); // Your app stores "username" here

        // Get the other user’s ID from intent
        String otherUsernameId = getIntent().getStringExtra("username");

        if (currentUserId.isEmpty() || otherUsernameId == null || otherUsernameId.isEmpty()) {
            Toast.makeText(this, "Missing user information", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate chatroom ID (always alphabetical order)
        chatroomId = generateChatroomId(currentUserId, otherUsernameId);

        // Try to fetch the chatroom from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chatrooms")
                .document(chatroomId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ChatroomModel chatroomModel = task.getResult().toObject(ChatroomModel.class);
                        if (chatroomModel == null) {
                            // First time chatting — create a new chatroom
                            chatroomModel = new ChatroomModel(
                                    chatroomId,
                                    Arrays.asList(currentUserId, otherUsernameId),
                                    Timestamp.now(),
                                    "" // lastMessage can be empty
                            );
                            db.collection("chatrooms")
                                    .document(chatroomId)
                                    .set(chatroomModel)
                                    .addOnSuccessListener(unused -> Log.d("Chat", "New chatroom created"))
                                    .addOnFailureListener(e -> Log.e("Chat", "Failed to create chatroom", e));
                        } else {
                            Log.d("Chat", "Chatroom already exists");
                        }
                    } else {
                        Log.e("Chat", "Failed to fetch chatroom", task.getException());
                    }
                });
    }

    /**
     * Functionality for sending messages directly to another user
     * @param message - the message the user wants to send
     */
    void sendMessageToUser(String message) {
        // Get current user's info from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("user", "");
        String otherUsernameId = getIntent().getStringExtra("username");

        if (chatroomId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "User info or chatroom is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Chatroom metadata
        ChatroomModel chatroomModel = new ChatroomModel(
                chatroomId,
                Arrays.asList(currentUserId, otherUsernameId), // Assuming the other user's ID is passed via intent
                Timestamp.now(),
                currentUserId
        );
        chatroomModel.setLastMessage(message);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update the chatroom metadata in Firestore
        db.collection("chatrooms")
                .document(chatroomId)
                .set(chatroomModel)
                .addOnSuccessListener(unused -> Log.d("Chat", "Chatroom updated"))
                .addOnFailureListener(e -> Log.e("Chat", "Failed to update chatroom", e));

        // Create the message object
        ChatMessageModel chatMessage = new ChatMessageModel(
                message,
                currentUserId,
                Timestamp.now()
        );

        Log.d("Chat", "chatroomId: " + chatroomId);
        // Save the message in the "chats" subcollection
        db.collection("chatrooms")
                .document(chatroomId)
                .collection("chats")
                .add(chatMessage)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageInput.setText("");
                        // You can later implement this if needed
                        // sendNotification(message);
                    } else {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}















