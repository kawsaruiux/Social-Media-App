package com.kawsarit.konok.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kawsarit.konok.socialmediaapp.Model.Posts;
import com.kawsarit.konok.socialmediaapp.ViewHolder.MyPostsViewHolder;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;

    private DatabaseReference PostsRef, LikesRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    Boolean LikeChecker = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);


        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("My Posts");


        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);     //for showing the latest post first
        linearLayoutManager.setStackFromEnd(true);      //for showing the latest post first
        myPostsList.setLayoutManager(linearLayoutManager);


        DisplayMyAllPosts();
    }



    private void DisplayMyAllPosts() {

        Query myPostQuery = PostsRef.orderByChild("uid")
                .startAt(currentUserID)
                .endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(myPostQuery, Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MyPostsViewHolder myPostsViewHolder, int i, @NonNull Posts model) {

                        final String PostKey = getRef(i).getKey();   //get the post key from the specific post

                        myPostsViewHolder.username.setText(model.getFullname());
                        myPostsViewHolder.PostTime.setText(model.getTime());
                        myPostsViewHolder.PostDate.setText(model.getDate());
                        myPostsViewHolder.PostDescription.setText(model.getDescription());

                        Picasso.get().load(model.getProfileimage()).placeholder(R.drawable.profile).into(myPostsViewHolder.ProfileImage);
                        Picasso.get().load(model.getPostimage()).into(myPostsViewHolder.PostImage);


                        myPostsViewHolder.PostImage.setOnClickListener(new View.OnClickListener() {        //when user click on any post from his timeline
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey", PostKey);
                                startActivity(clickPostIntent);
                            }
                        });


                        myPostsViewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent commentsIntent = new Intent(MyPostsActivity.this, CommentsActivity.class);
                                commentsIntent.putExtra("PostKey", PostKey);
                                startActivity(commentsIntent);
                            }
                        });


                        myPostsViewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {       //1.when user click on love/like button
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

                        myPostsViewHolder.setLikeButtonStatus(PostKey);
                    }

                    @NonNull
                    @Override
                    public MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout, parent, false);
                        MyPostsViewHolder viewHolder = new MyPostsViewHolder(view);
                        return viewHolder;
                    }
                };
        myPostsList.setAdapter(adapter);
        adapter.startListening();
    }
}
