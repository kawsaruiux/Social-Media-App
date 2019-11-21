package com.kawsarit.konok.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button MyPosts, MyFriends;

    private DatabaseReference profileUserRef, FriendsRef, PostsRef;
    private FirebaseAuth mAuth;

    private String currentUserID;
    private int countFriendsNumber = 0, countsPosts = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        userName = (TextView) findViewById(R.id.my_username);
        userProfileName = (TextView) findViewById(R.id.my_profile_fullname);
        userStatus = (TextView) findViewById(R.id.my_profile_status);
        userCountry = (TextView) findViewById(R.id.my_country);
        userGender = (TextView) findViewById(R.id.my_gender);
        userRelation = (TextView) findViewById(R.id.my_relationship_status);
        userDOB = (TextView) findViewById(R.id.my_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_picture);
        MyPosts = (Button) findViewById(R.id.my_posts_button);
        MyFriends = (Button) findViewById(R.id.my_friends_button);



        MyFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendUserToFriendActivity();
            }
        });

        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SendUserToMyPostsActivity();
            }
        });



        FriendsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    countFriendsNumber = (int) dataSnapshot.getChildrenCount();
                    MyFriends.setText(Integer.toString(countFriendsNumber) + " Friends");
                }
                else {

                    MyFriends.setText("No Friend");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        PostsRef.orderByChild("uid")
                .startAt(currentUserID)
                .endAt(currentUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            countsPosts = (int) dataSnapshot.getChildrenCount();
                            MyPosts.setText(Integer.toString(countsPosts) + " Posts");
                        }
                        else {

                            MyPosts.setText("No Posts");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



        profileUserRef.addValueEventListener(new ValueEventListener() {
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SendUserToFriendActivity() {

        Intent friendsIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void SendUserToMyPostsActivity() {

        Intent friendsIntent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(friendsIntent);
    }
}
