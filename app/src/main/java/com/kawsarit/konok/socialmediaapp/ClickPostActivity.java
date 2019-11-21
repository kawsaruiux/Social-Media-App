package com.kawsarit.konok.socialmediaapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescription;
    private Button DeletePostbutton,  EditPostButton;

    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;

    private String PostKey, currentUserID, description, image, databaseUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();


        PostImage = (ImageView) findViewById(R.id.click_post_image);
        PostDescription = (TextView) findViewById(R.id.click_post_description);
        DeletePostbutton = (Button) findViewById(R.id.delete_post_button);
        EditPostButton = (Button) findViewById(R.id.edit_post_button);

        DeletePostbutton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);


        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){     //If we do not use if statement here the app will crash
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();

                    databaseUserID = dataSnapshot.child("uid").getValue().toString();   //We will get the id of the user which are stored as 'uid'

                    PostDescription.setText(description);
                    Picasso.get().load(image).into(PostImage);

                    if (currentUserID.equals(databaseUserID)){

                        DeletePostbutton.setVisibility(View.VISIBLE);
                        EditPostButton.setVisibility(View.VISIBLE);
                    }

                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            EditCurrentPost(description);
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeletePostbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DeleteCurrentPoost();
            }
        });
    }

    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post: ");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post Updated Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_blue_dark);       //set the color of the dialog box
    }

    private void DeleteCurrentPoost() {

        ClickPostRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
