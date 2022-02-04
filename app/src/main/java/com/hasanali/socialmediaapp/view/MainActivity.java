 package com.hasanali.socialmediaapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hasanali.socialmediaapp.databinding.ActivityMainBinding;
import com.hasanali.socialmediaapp.model.LoadingDialog;

 public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        currentUserCheck();
    }

    private void currentUserCheck() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            intentToFeedAct();
        }
    }

    public void signIn(View view) {
        String email = activityMainBinding.emailText.getText().toString();
        String password = activityMainBinding.passwordText.getText().toString();
        if (email.matches("") || password.matches("")) {
            Toast.makeText(MainActivity.this,"Enter email and password.",Toast.LENGTH_LONG).show();
        } else {
            auth.signInWithEmailAndPassword(email,password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            intentToFeedAct();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    public void createAccount(View view) {
        Intent intentToSignup = new Intent(MainActivity.this,SignupActivity.class);
        startActivity(intentToSignup);
    }

    private void intentToFeedAct() {
        Intent intent = new Intent(MainActivity.this,FeedActivity.class);
        finishAffinity();
        startActivity(intent);
    }
}