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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hasanali.socialmediaapp.databinding.ActivitySignupBinding;
import com.hasanali.socialmediaapp.model.LoadingDialog;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void signUp (View view) {
        String email = binding.editTextEmail.getText().toString();
        String fullName = binding.editTextFullName.getText().toString();
        String username = binding.editTextUsername.getText().toString();
        String password = binding.editTextPassword.getText().toString();

        if (email.matches("") || fullName.matches("") || username.matches("") || password.matches("")) {
            Toast.makeText(SignupActivity.this,"Fields cannot be left blank.",Toast.LENGTH_SHORT).show();
        } else {
            auth.createUserWithEmailAndPassword(email,password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            setFirebaseFirestoreUsers (email,fullName,username);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignupActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setFirebaseFirestoreUsers(String email, String fullName, String username) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("user_email",email);
        userData.put("user_fullname",fullName);
        userData.put("user_username",username);
        userData.put("user_id",FirebaseAuth.getInstance().getUid());
        userData.put("user_pphoto",null);
        userData.put("user_birth",null);
        userData.put("user_phone",null);
        userData.put("user_bio",null);

        db.collection("Users").document(FirebaseAuth.getInstance().getUid()).set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Intent intent = new Intent(SignupActivity.this,FeedActivity.class);
                        finishAffinity();
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}