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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hasanali.socialmediaapp.R;
import com.hasanali.socialmediaapp.databinding.ActivitySettingsBinding;
import com.hasanali.socialmediaapp.model.LoadingDialog;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Map<String, Object> settingsData;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Uri pphotoImage;
    LoadingDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        binding.buttonSave.setVisibility(View.INVISIBLE);
        setVisibility(false);

        dialog = new LoadingDialog(SettingsActivity.this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        settingsData = new HashMap<>();

        registerLaunchers();
        getSettingsData();
    }

    public void registerLaunchers() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intent = result.getData();
                    pphotoImage = intent.getData();
                    binding.imageView.setImageURI(pphotoImage);
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
                    Toast.makeText(SettingsActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getSettingsData() {
        db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String fullname = (String) documentSnapshot.get("user_fullname");
                        String birth = (String) documentSnapshot.get("user_birth");
                        String phone = (String) documentSnapshot.get("user_phone");
                        String bio = (String) documentSnapshot.get("user_bio");
                        String pphoto = (String) documentSnapshot.get("user_pphoto");
                        String email = (String) documentSnapshot.get("user_email");
                        String username = (String) documentSnapshot.get("user_username");
                        settingsData.put("user_username",username);
                        settingsData.put("user_email",email);
                        settingsData.put("user_fullname",fullname);
                        settingsData.put("user_birth",birth);
                        settingsData.put("user_phone",phone);
                        settingsData.put("user_bio",bio);
                        settingsData.put("user_pphoto",pphoto);
                        setSettingsData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setSettingsData() {
        binding.settingsFullName.setText(settingsData.get("user_fullname").toString());
        if (settingsData.get("user_birth") != null) {
            binding.settingsBirth.setText(settingsData.get("user_birth").toString());
        }
        if (settingsData.get("user_phone") != null) {
            binding.settingsPhone.setText(settingsData.get("user_phone").toString());
        }
        if (settingsData.get("user_bio") != null) {
            binding.settingsBiography.setText(settingsData.get("user_bio").toString());
        }
        if (settingsData.get("user_pphoto") != null) {
            Picasso.get().load(settingsData.get("user_pphoto").toString()).into(binding.imageView);
        } else {
            binding.imageView.setImageResource(R.drawable.defaultpp);
        }
    }

    public void selectPphoto(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view,"Permission needed for gallery.",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give permission", new View.OnClickListener() {
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

    public void clickUpdate(View view) {
        setVisibility(true);
        binding.buttonUpdate.setVisibility(View.INVISIBLE);
        binding.buttonSave.setVisibility(View.VISIBLE);
    }

    public void clickSave(View view) {
        settingsData.put("user_birth",binding.settingsBirth.getText().toString());
        settingsData.put("user_phone",binding.settingsPhone.getText().toString());
        settingsData.put("user_bio",binding.settingsBiography.getText().toString());
        if (settingsData.get("user_fullname").toString().matches("")) {
            Toast.makeText(SettingsActivity.this,"Please enter your full name.",Toast.LENGTH_SHORT).show();
        } else {
            dialog.startLoadingDialog();
            setStorage();
        }
    }

    public void setStorage() {
        if (pphotoImage != null) {
            UUID uuid = UUID.randomUUID();
            String imageName = "images/" + uuid + ".jpg";
            storageReference.child(imageName).putFile(pphotoImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            StorageReference newReferance = firebaseStorage.getReference(imageName);
                            newReferance.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String pPhotoFromUrl = uri.toString();
                                    settingsData.put("user_pphoto",pPhotoFromUrl);
                                    setFirestore();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SettingsActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            setFirestore();
        }
    }

    public void setFirestore() {
        db.collection("Users").document(user.getUid()).set(settingsData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dissmissDialog();
                        Intent intentToFeed = new Intent(SettingsActivity.this,FeedActivity.class);
                        intentToFeed.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intentToFeed);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setVisibility(Boolean control) {
        binding.settingsFullName.setEnabled(control);
        binding.settingsBirth.setEnabled(control);
        binding.settingsPhone.setEnabled(control);
        binding.settingsBiography.setEnabled(control);
        binding.imageView.setEnabled(control);
    }
}