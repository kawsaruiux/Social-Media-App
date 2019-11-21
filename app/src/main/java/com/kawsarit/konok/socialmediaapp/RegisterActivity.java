package com.kawsarit.konok.socialmediaapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton;
    private ProgressDialog LoadingBar;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        UserConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        CreateAccountButton = (Button) findViewById(R.id.register_create_account);

        LoadingBar = new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){

            sendUserToMainActivity();
        }
    }


    private void CreateNewAccount() {
        String email = UserEmail.getText().toString();
        String passwrod = UserPassword.getText().toString();
        String confirmPassword = UserConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Provide Your Email...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(passwrod)){
            Toast.makeText(this,"Enter Your Password...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Confirm Your Password Please...", Toast.LENGTH_SHORT).show();
        }
        else if (!passwrod.equals(confirmPassword)){
            Toast.makeText(this, "Your Password and Confirm Password Must Match...", Toast.LENGTH_LONG).show();
        }
        else {

            LoadingBar.setTitle("Creating New Account");
            LoadingBar.setMessage("Please wait until creating new account...");
            LoadingBar.show();
            LoadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, passwrod)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                SendEmailVerificationMessage();

                                LoadingBar.dismiss();
                            }
                            else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error Occured: " + errorMessage, Toast.LENGTH_SHORT).show();
                                LoadingBar.dismiss();
                            }
                        }
                    });
        }
    }



    //This method will send email to user for verification
    private void SendEmailVerificationMessage(){

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){

            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                Toast.makeText(RegisterActivity.this, "Registration Successful. Please check you email to verify your account", Toast.LENGTH_LONG).show();
                                SendUserToLoginActivity();
                                mAuth.signOut();
                            }
                            else {

                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                            }
                        }
                    });
        }
    }



    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
