package com.kawsarit.konok.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kawsarit.konok.socialmediaapp.Model.Posts;
import com.kawsarit.konok.socialmediaapp.ViewHolder.PostsViewHolder;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UserDatabaseRef, PostsRef, LikesRef;

    String currentUserID;

    Boolean LikeChecker = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        currentUserID  = mAuth.getCurrentUser().getUid();
        UserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");


        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);


        postList = (RecyclerView) findViewById(R.id.all_user_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);     //for showing the latest post first
        linearLayoutManager.setStackFromEnd(true);      //for showing the latest post first
        postList.setLayoutManager(linearLayoutManager);


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);    //Using navigation menus to the main page
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);      //here using 'navView' infront of 'findVi...' because of ignoring crass the app


        UserDatabaseRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    if (dataSnapshot.hasChild("fullname")){

                        String fullname = dataSnapshot.child("fullname").getValue().toString();     //(fullname = Users fullname)
                        NavProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage")){

                        String image = dataSnapshot.child("profileimage").getValue().toString();    //(profileimage = Users profileimage)
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Profile Image not Exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });
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
        UserDatabaseRef.child(currentUserID).child("userState")
                .updateChildren(currentStateMap);
    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            SendUserToLoginActivity();
        }
        else {
            CheckUserExistance();
        }

        Query SortPostsInDecendingOrder = PostsRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(SortPostsInDecendingOrder, Posts.class)
                .build();


        FirebaseRecyclerAdapter<Posts, PostsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

                final String PostKey = getRef(position).getKey();   //get the post key from the specific post

                holder.username.setText(model.getFullname());
                holder.PostTime.setText(model.getTime());
                holder.PostDate.setText(model.getDate());
                holder.PostDescription.setText(model.getDescription());

                Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).into(holder.ProfileImage);
                Picasso.get().load(model.getPostimage()).into(holder.PostImage);


                holder.PostImage.setOnClickListener(new View.OnClickListener() {        //when user click on any post from his timeline
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(clickPostIntent);
                     }
                });


                holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("PostKey", PostKey);
                        startActivity(commentsIntent);
                    }
                });


                holder.LikePostButton.setOnClickListener(new View.OnClickListener() {       //1.when user click on love/like button
                    @Override
                    public void onClick(View v) {

                        LikeChecker = true;     //2.it will be Liked when cliked on like button

                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (LikeChecker.equals(true)){

                                    if (dataSnapshot.child(PostKey).hasChild(currentUserID)){

                                        LikesRef.child(PostKey).child(currentUserID).removeValue();     //1.Remove previous added data from database like history
                                        LikeChecker = false;        //Unlikes when user clicked on like button second time
                                    }
                                    else {

                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);     //2.Set the new data after unliked the post
                                        LikeChecker = false;    //And still it will be unliked
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                holder.setLikeButtonStatus(PostKey);
            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_post_layout, viewGroup, false);
                PostsViewHolder viewHolder = new PostsViewHolder(view);
                return viewHolder;
            }
        };
        postList.setAdapter(adapter);
        adapter.startListening();

        //define the user status here
        updateUserStatus("online");
    }



    private void CheckUserExistance() {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        UserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(current_user_id)){

                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void UserMenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()){

            case R.id.nav_newpost:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friend:
                SendUserToFriendActivity();
                Toast.makeText(this, "Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friend:
                SendUserToFindFriendActivity();
                Toast.makeText(this, "Find Friend", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_message:
                SendUserToFriendActivity();
                Toast.makeText(this, "Friend List to Send Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_Settings:
                SendUserToSettingsActivity();
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_logout:
                updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }


    private void SendUserToFriendActivity() {

        Intent friendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }


    private void SendUserToPostActivity() {

        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }


    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }


    private void SendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }


    private void SendUserToFindFriendActivity() {
        Intent findFriendIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendIntent);
    }
}
