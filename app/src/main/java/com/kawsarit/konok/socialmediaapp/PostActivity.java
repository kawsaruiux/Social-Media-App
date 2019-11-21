package com.kawsarit.konok.socialmediaapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;
    private Uri ImageUri;

    private ProgressDialog loadingBar;

    private String Description;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, postRef;
    private StorageReference PostImagesStorageRef;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;
    private long countPosts = 0;    //this variable will count the number of post in database

    private static final int Gallery_Pick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        PostImagesStorageRef = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        SelectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        UpdatePostButton = (Button) findViewById(R.id.update_post_button);
        PostDescription = (EditText) findViewById(R.id.post_description);

        loadingBar = new ProgressDialog(this);

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);  //this line for create a back(arrow) button
        getSupportActionBar().setTitle("Update Post");


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatePostInfo();
            }
        });
    }



    private void ValidatePostInfo() {

        Description = PostDescription.getText().toString();

        if (ImageUri == null){
            Toast.makeText(PostActivity.this, "First Select an Image...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Description)){
            Toast.makeText(PostActivity.this, "Write something about your photo..", Toast.LENGTH_SHORT).show();
        }
        else {

            loadingBar.setTitle("Add new Post");
            loadingBar.setMessage("Please wait until your post update... ");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }
    }



    private void StoringImageToFirebaseStorage() {

        //Here we found the current date and time from user devices and then use it to create an unique image name which one uploaded by the users

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");    //here current Date store into the 'currentDate' variable
        saveCurrentDate = currentDate.format(calForDate.getTime());     //save date as string type variable into the 'saveCurrentDate'

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;


        StorageReference filePath = PostImagesStorageRef
                .child("Post Images")   //Storage folder name will be 'Post Image' into the Firebase Storage
                .child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");   //This will be the name of the image which one will store into the firebase storage

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()){

                    downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();    //Here we get the link of firebase stored file
                    //downloadUrl = task.getResult().getStorage().getDownloadUrl();

                    Toast.makeText(PostActivity.this, "Your Image Upload Succesfully to Storage...", Toast.LENGTH_SHORT).show();

                    SavingPostInformationToDatabase();
                }
                else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error Occor: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void SavingPostInformationToDatabase() {

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    countPosts = dataSnapshot.getChildrenCount();   //this will count all the post exist in database
                }
                else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        userRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String userFullName = dataSnapshot.child("fullname").getValue().toString();     //We will get Users fullname
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();     //We will get Users Profile image


                    HashMap postHashMap = new HashMap();
                        postHashMap.put("uid", current_user_id);
                        postHashMap.put("date", saveCurrentDate);
                        postHashMap.put("time", saveCurrentTime);
                        postHashMap.put("description", Description);
                        postHashMap.put("postimage", downloadUrl);
                        postHashMap.put("profileimage", userProfileImage);
                        postHashMap.put("fullname", userFullName);
                        postHashMap.put("counter", countPosts);

                    postRef.child(current_user_id + postRandomName)     //for make a unique name for users every post we using 'postRandomName' & 'current_user_id'
                            .updateChildren(postHashMap)    //update all the info to database
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()){

                                        SendUserToMainActivity();

                                        Toast.makeText(PostActivity.this, "Your new post update succesfully..", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else {
                                        //String message = task.getException().getMessage();
                                        Toast.makeText(PostActivity.this, "Errer Occur: ", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){

            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){

            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }



    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
