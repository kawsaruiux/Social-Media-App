package com.kawsarit.konok.socialmediaapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;

    private Button SendFriendRequestButton, DeclineFriendRequestButton;

    private DatabaseReference FriendRequestRef, UserRef, FriendRef;
    private FirebaseAuth mAuth;
    private String senderUserID, receiverUserID, CURRENT_STATE, saveCurrentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();     //this will get the current user id who want to send friend request

        receiverUserID = getIntent().getExtras().get("visited_user_id").toString();      //receive the id of the user who send fried request from FindFriendsActivity
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        InitialiseFields();


        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText("@" + myUserName);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB: " + myDOB);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("Gender: " + myGender);
                    userRelation.setText("Relation: " + myRelationStatus);


                    MaintananceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);   //this button will invisible for who send friend request
        DeclineFriendRequestButton.setEnabled(false);

        if (!senderUserID.equals(receiverUserID)){

            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SendFriendRequestButton.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")){

                        SendFriendRequestToPerson();    //
                    }
                    if (CURRENT_STATE.equals("request_sent")){

                        CancelSendRequest();    //
                    }
                    if (CURRENT_STATE.equals("request_received")){

                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends")){

                        UnfiredToExistingFriend();
                    }
                }
            });
        }
        else {
            SendFriendRequestButton.setVisibility(View.INVISIBLE);      //people can't send friend request them self
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        }
    }



    private void UnfiredToExistingFriend() {

        FriendRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            FriendRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequestButton.setText("Send Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }



    private void AcceptFriendRequest() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendRef.child(senderUserID).child(receiverUserID).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            FriendRef.child(receiverUserID).child(senderUserID).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                FriendRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){
                                                                    FriendRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if (task.isSuccessful()){

                                                                                        SendFriendRequestButton.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        SendFriendRequestButton.setText("Unfriend");

                                                                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendRequestButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }



    private void CancelSendRequest() {

        FriendRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            FriendRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequestButton.setText("Send Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }



    private void MaintananceOfButtons() {

        FriendRequestRef.child(senderUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(receiverUserID)){

                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){

                                CURRENT_STATE = "request_sent";
                                SendFriendRequestButton.setText("Cancel Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);
                            }
                            else if (request_type.equals("received")){

                                CURRENT_STATE = "request_received";
                                SendFriendRequestButton.setText("Accept Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestButton.setEnabled(true);

                                DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        CancelSendRequest();
                                    }
                                });
                            }
                        }
                        else {
                            FriendRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.hasChild(receiverUserID)){

                                                CURRENT_STATE = "friends";
                                                SendFriendRequestButton.setText("Unfriend");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }



    private void SendFriendRequestToPerson() {

        FriendRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            FriendRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "request_send";
                                                SendFriendRequestButton.setText("Cancel Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }



    private void InitialiseFields() {

        userName = (TextView) findViewById(R.id.person_username);
        userProfileName = (TextView) findViewById(R.id.person_fullname);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_gender);
        userRelation = (TextView) findViewById(R.id.person_relationship_status);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_picture);

        SendFriendRequestButton = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestButton = (Button) findViewById(R.id.person_decline_friend_request_btn);

        CURRENT_STATE = "not_friends";  //this will help to define the person is already friend of not
    }
}
