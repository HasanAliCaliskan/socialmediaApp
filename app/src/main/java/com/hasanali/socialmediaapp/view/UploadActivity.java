package com.hasanali.socialmediaapp.view;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hasanali.socialmediaapp.R;
import com.hasanali.socialmediaapp.databinding.ActivityUploadBinding;
import com.hasanali.socialmediaapp.model.LoadingDialog;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    private ActivityUploadBinding activityUploadBinding;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private Map<String, Object> postData;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Uri imageData;
    LoadingDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityUploadBinding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = activityUploadBinding.getRoot();
        setContentView(view);

        imageData = null;
        registerLauncher();

        dialog = new LoadingDialog(UploadActivity.this);
        postData = new HashMap<>();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        user = auth.getCurrentUser();
        getUserData();
    }

    public void postFunc(View view) {
        if (imageData == null && activityUploadBinding.editTextComment.getText().toString().matches("")) {
            Toast.makeText(UploadActivity.this,"!!!!",Toast.LENGTH_LONG).show();
        } else {
            dialog.startLoadingDialog();
            setFirebaseStorage();
        }
    }

    public void getUserData () {
        db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String username = (String) documentSnapshot.get("user_username");
                        String fullname = (String) documentSnapshot.get("user_fullname");
                        String pphoto = (String) documentSnapshot.get("user_pphoto");
                        String email = user.getEmail();
                        postData.put("post_username",username);
                        postData.put("post_fullname",fullname);
                        postData.put("post_email",email);
                        postData.put("post_pphoto",pphoto);

                        activityUploadBinding.postUserName.setText(fullname + " @" + username);
                        if (pphoto != null) {
                            Picasso.get().load(pphoto).into(activityUploadBinding.userPphoto);
                        } else {
                            activityUploadBinding.userPphoto.setImageResource(R.drawable.defaultpp);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = result.getData();
                    imageData = intentFromResult.getData();
                    activityUploadBinding.imagePost.setImageURI(imageData);
                }
            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                } else {
                    Toast.makeText(UploadActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void addPhoto (View view) {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view,"Permission needed for gallery.",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                            }
                        }).show();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    private void setFirebaseStorage() {
        String comment = activityUploadBinding.editTextComment.getText().toString();
        postData.put("post_comment",comment);
        if (imageData != null) {
            UUID uuid = UUID.randomUUID();
            String imageName = "images/" + uuid + ".jpg";
            storageReference.child(imageName).putFile(imageData)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            StorageReference newReferance = firebaseStorage.getReference(imageName);
                            newReferance.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    postData.put("post_image",downloadUrl);
                                    postData.put("post_date",FieldValue.serverTimestamp());
                                    setFirebaseFirestorePost();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadActivity.this, e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            postData.put("post_image",null);
            postData.put("post_date",FieldValue.serverTimestamp());
            setFirebaseFirestorePost();
        }
    }

    private void setFirebaseFirestorePost() {
        db.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                String postId = documentReference.getId();
                postData.put("post_id",postId);
                documentReference.set(postData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dissmissDialog();
                        Intent intentToFeed = new Intent(UploadActivity.this,FeedActivity.class);
                        intentToFeed.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentToFeed);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}