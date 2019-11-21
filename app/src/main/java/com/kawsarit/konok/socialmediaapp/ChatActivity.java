package com.kawsarit.konok.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawsarit.konok.socialmediaapp.Model.Messages;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar ChatToolBar;
    private ImageButton SendMessageButton, SendImageButton;
    private EditText userMessageInput;

    private RecyclerView userMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messageAdapter;

    private String messageReceiverID, messageReceiverName, messageSenderID, saveCurrentDate, saveCurrentTime;

    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;

    private DatabaseReference RootRef, UserRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        messageReceiverID = getIntent().getExtras().get("visited_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();

        InitializeFields();

        DisplayReceiverInfo();  //display the information of which one i am cheating with

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendMessage();
            }
        });


        FetchMessages();
    }



    private void FetchMessages() {

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        if (dataSnapshot.exists()){

                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messageAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }



    private void SendMessage() {

        updateUserStatus("online");

        String messageText = userMessageInput.getText().toString();

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Type any message..", Toast.LENGTH_SHORT).show();
        }
        else {
            String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;


            //Creating a unique key for every messages (push() makes a key here)
            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
            String message_push_id = user_message_key.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");    //here current Date store into the 'currentDate' variable
            saveCurrentDate = currentDate.format(calForDate.getTime());     //save date as string type variable into the 'saveCurrentDate'

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(calForTime.getTime());

            Map messageTextBody = new HashMap();
                messageTextBody.put("message", messageText);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("type", "text");
                messageTextBody.put("from", messageSenderID);


            //Don't understand these lines of code
            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()){

                        Toast.makeText(ChatActivity.this, "Message Sent..", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }
                    else {

                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }
                }
            });
        }
    }



    //This is for showing an user is online or not(green dot for active user)
    public void updateUserStatus(String state){

        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");    //here current Date store into the 'currentDate' variable
        saveCurrentDate = currentDate.format(calForDate.getTime());     //save date as string type variable into the 'saveCurrentDate'

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);


        //add another field to database for user status
        UserRef.child(messageSenderID).child("userState")
                .updateChildren(currentStateMap);
    }



    private void DisplayReceiverInfo() {

        receiverName.setText(messageReceiverName);

        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);

                    String type = dataSnapshot.child("userState").child("type").getValue().toString();
                    String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                    String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (type.equals("online")) {

                        userLastSeen.setText("online");
                    } else {

                        userLastSeen.setText("last seen: " + lastTime + " " + lastDate);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //Initialize all the fields here
    private void InitializeFields() {

        ChatToolBar = (Toolbar) findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChatToolBar);


        //Connect chat_custom_bar with ChatActivity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);


        receiverName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);


        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        SendImageButton = (ImageButton) findViewById(R.id.send_image_file_icon);
        userMessageInput = (EditText) findViewById(R.id.input_message);
        userMessageList = (RecyclerView) findViewById(R.id.message_list_user);


        messageAdapter = new MessagesAdapter(messagesList);
        userMessageList = (RecyclerView) findViewById(R.id.message_list_user);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);
    }
}
