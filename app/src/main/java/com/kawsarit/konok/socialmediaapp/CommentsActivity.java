package com.kawsarit.konok.socialmediaapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kawsarit.konok.socialmediaapp.Model.Comments;
import com.kawsarit.konok.socialmediaapp.ViewHolder.CommentsViewHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView ComentsList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;

    private DatabaseReference UserRef, PostRef;
    private FirebaseAuth mAuth;

    private String Post_Key, currentUser_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


        Post_Key = getIntent().getExtras().get("PostKey").toString();      //send post key from MainActivity to CommentsActivity

        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUser_id = mAuth.getCurrentUser().getUid();


        ComentsList = (RecyclerView) findViewById(R.id.comments_list);
        ComentsList.setHasFixedSize(true);      //Why??
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);    //why???
        linearLayoutManager.setReverseLayout(true);     //why???
        linearLayoutManager.setStackFromEnd(true);      //
        ComentsList.setLayoutManager(linearLayoutManager);      //

        CommentInputText = (EditText) findViewById(R.id.comment_input);
        PostCommentButton = (ImageButton) findViewById(R.id.post_a_commnet);


        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserRef.child(currentUser_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            String userName = dataSnapshot.child("username").getValue().toString();

                            ValidateComment(userName);

                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(PostRef, Comments.class)
                        .build();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

                        holder.commentUsername.setText(model.getUsername());
                        holder.commentedText.setText(model.getComment());
                        holder.commnetDate.setText("Date: " + model.getDate());
                        holder.commentTime.setText("Time: " + model.getTime());
                    }

                    @NonNull
                    @Override
                    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout, viewGroup, false);
                        CommentsViewHolder holder = new CommentsViewHolder(view);
                        return holder;
                    }
                };
        ComentsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }



    private void ValidateComment(String userName) {

        String commentInput = CommentInputText.getText().toString();

        if (TextUtils.isEmpty(commentInput)){
            Toast.makeText(this, "Plese write a comment...", Toast.LENGTH_SHORT).show();
        }
        else {

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");    //here current Date store into the 'currentDate' variable
            final String saveCurrentDate = currentDate.format(calForDate.getTime());     //save date as string type variable into the 'saveCurrentDate'

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            final String commentRandomName = currentUser_id + saveCurrentDate + saveCurrentTime;

            HashMap commentHashMap = new HashMap();
                commentHashMap.put("uid", currentUser_id);
                commentHashMap.put("comment", commentInput);
                commentHashMap.put("date", saveCurrentDate);
                commentHashMap.put("time", saveCurrentTime);
                commentHashMap.put("username", userName);

                PostRef.child(commentRandomName).updateChildren(commentHashMap)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()){
                            Toast.makeText(CommentsActivity.this, "Comment Added..", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(CommentsActivity.this, "Error Cccured..", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }
}
