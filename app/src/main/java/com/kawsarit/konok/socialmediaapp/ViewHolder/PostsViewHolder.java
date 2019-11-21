package com.kawsarit.konok.socialmediaapp.ViewHolder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawsarit.konok.socialmediaapp.R;

import de.hdodenhof.circleimageview.CircleImageView;


public class PostsViewHolder extends RecyclerView.ViewHolder{

    public TextView username, PostTime, PostDate, PostDescription;
    public CircleImageView ProfileImage;
    public ImageView PostImage;

    public ImageButton LikePostButton, CommentPostButton;
    public TextView NumberOfLikes;
    public int countLikes;
    public String currentUserId;
    public DatabaseReference LikesRef;

    public PostsViewHolder(View itemView) {

        super(itemView);

        LikePostButton = (ImageButton) itemView.findViewById(R.id.like_button);
        CommentPostButton = (ImageButton) itemView.findViewById(R.id.comment_button);
        NumberOfLikes = (TextView) itemView.findViewById(R.id.number_of_likes);

        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        username = itemView.findViewById(R.id.post_user_name);
        PostTime = itemView.findViewById(R.id.post_time);
        PostDate = itemView.findViewById(R.id.post_date);
        PostDescription = itemView.findViewById(R.id.post_description);

        ProfileImage = itemView.findViewById(R.id.post_profile_image);
        PostImage = itemView.findViewById(R.id.post_image);
    }


    public void setLikeButtonStatus(final String PostKey) {

        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(PostKey).hasChild(currentUserId)){

                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();       //This will count the number of likes of a post
                    LikePostButton.setImageResource(R.drawable.love);
                    NumberOfLikes.setText((Integer.toString(countLikes) + (" Likes")));
                }
                else {
                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    LikePostButton.setImageResource(R.drawable.love_empty);
                    NumberOfLikes.setText(Integer.toString(countLikes) + (" Likes"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

