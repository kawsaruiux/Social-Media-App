package com.kawsarit.konok.socialmediaapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName, userProfileName, userStatus, userCountry, userGender, userRelation, userDOB;
    private Button UpdateAccountSettongsButton;
    private CircleImageView userProfileImage;

    private Uri imageUri;
    private String myUrl = "";

    private ProgressDialog loadingBar;

    private StorageReference UserProfileImageRef;
    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;

    private StorageTask uploadTask;

    private String currentUserID;

    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText) findViewById(R.id.settings_username);
        userProfileName = (EditText) findViewById(R.id.settings_full_name);
        userStatus = (EditText) findViewById(R.id.settings_status);
        userCountry = (EditText) findViewById(R.id.settings_country);
        userGender = (EditText) findViewById(R.id.settings_gender);
        userRelation = (EditText) findViewById(R.id.settings_relationship_status);
        userDOB = (EditText) findViewById(R.id.settings_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);

        loadingBar = new ProgressDialog(this);

        UpdateAccountSettongsButton  = (Button) findViewById(R.id.update_account_settings_button);


        SettingsUserRef.addValueEventListener(new ValueEventListener() {    //when user click on Settings then previous value will appear which ones are already saved into the database
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

                    userName.setText(myUserName);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText(myDOB);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });


        UpdateAccountSettongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidateAccountInfo();

            }
        });


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)   //jodi sofol vabe image crop hoy
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);    //'result' namer ekta variable e crop image ta rakhlam

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                final Uri resultUri = result.getUri();    //crop hobar por j data pabo seta uri hisabe resultUri e rakhlam

                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        final String downloadUrl = uri.toString();

                                        SettingsUserRef.child("profileimage").setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                            startActivity(selfIntent);

                                                            Toast.makeText(SettingsActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        } else {
                                                            String message = task.getException().getMessage();
                                                            Toast.makeText(SettingsActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });
                                    }
                                });
                            }
                        });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }


    private void ValidateAccountInfo() {

        String username = userName.getText().toString();
        String userprofilename = userProfileName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(userprofilename)){
            Toast.makeText(this, "Please write your full name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(status)){
            Toast.makeText(this, "status field is empty...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(dob)){
            Toast.makeText(this, "Please write your date of birth...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please write your country name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Please write your gender...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(relation)){
            Toast.makeText(this, "Please write relation...", Toast.LENGTH_SHORT).show();
        }
        else {

//            loadingBar.setTitle("Profile Image");
//            loadingBar.setMessage("Please wait, while we updating your profile image...");
//            loadingBar.setCanceledOnTouchOutside(true);
//            loadingBar.show();

            UpdateAccountInfo(username, userprofilename, status, dob, country, gender, relation);
        }
    }

    private void UpdateAccountInfo(String username, String userprofilename, String status, String dob, String country, String gender, String relation) {

        HashMap userMap = new HashMap();
        userMap.put("username", username);
        userMap.put("fullname", userprofilename);
        userMap.put("status", status);
        userMap.put("dob", dob);
        userMap.put("country", country);
        userMap.put("gender", gender);
        userMap.put("relationshipstatus", relation);

        SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()){

                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Your Information update successfully...", Toast.LENGTH_SHORT).show();
                    //loadingBar.dismiss();
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Error Occurs..", Toast.LENGTH_SHORT).show();
                    //loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
