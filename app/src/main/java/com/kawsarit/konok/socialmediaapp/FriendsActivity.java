package com.kawsarit.konok.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawsarit.konok.socialmediaapp.Model.Friends;
import com.kawsarit.konok.socialmediaapp.ViewHolder.FriendsViewHolder;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;

    private DatabaseReference FriendRef, UserRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friiends);


        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");


        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
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
        UserRef.child(online_user_id).child("userState")
                .updateChildren(currentStateMap);
    }


    //When user start the application
    @Override
    protected void onStart() {
        super.onStart();

        updateUserStatus("online");
    }


    //When user stop the application
    @Override
    protected void onStop() {
        super.onStop();

        updateUserStatus("offline");
    }


    //When user minimise or crush the application
    @Override
    protected void onDestroy() {
        super.onDestroy();

        updateUserStatus("offline");
    }


    private void DisplayAllFriends() {

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(UserRef, Friends.class)
                        .build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int i, @NonNull final Friends model) {


                final String usersIDs = getRef(i).getKey();     //get the user key from the specific user

                UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){


                            final String userName = dataSnapshot.child("fullname").getValue().toString();

                            //view green icon for the users(friends) who are online
                            final String type;
                            if (dataSnapshot.hasChild("userState")){

                                type = dataSnapshot.child("userState").child("type").getValue().toString();

                                if (type.equals("online")){

                                    holder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else {

                                    holder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            holder.myFriendName.setText(model.getFullname());
                            Picasso.get().load(model.getProfileimage()).into(holder.myFriendImage);


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CharSequence options[] = new CharSequence[]{
                                            "Go to " + userName + "'s Profile",
                                            "Send Message"
                                    };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (i == 0){

                                                Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                profileIntent.putExtra("visited_user_id", usersIDs);
                                                startActivity(profileIntent);
                                            }
                                            if (i == 1){

                                                Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                chatIntent.putExtra("visited_user_id", usersIDs);
                                                chatIntent.putExtra("userName", userName);
                                                startActivity(chatIntent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };

        myFriendList.setAdapter(adapter);
        adapter.startListening();
    }
}
