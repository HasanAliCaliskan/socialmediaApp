package com.hasanali.socialmediaapp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hasanali.socialmediaapp.R;
import com.hasanali.socialmediaapp.adapter.PostAdapter;
import com.hasanali.socialmediaapp.databinding.ActivityFeedBinding;
import com.hasanali.socialmediaapp.model.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private ActivityFeedBinding activityFeedBinding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ArrayList<Post> arrayList;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityFeedBinding = ActivityFeedBinding.inflate(getLayoutInflater());
        View view = activityFeedBinding.getRoot();
        setContentView(view);

        setSupportActionBar(activityFeedBinding.toolbar2);
        arrayList = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        getData();
        setPostAdapter();
    }

    private void setPostAdapter() {
        postAdapter = new PostAdapter(arrayList);
        activityFeedBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityFeedBinding.recyclerView.setAdapter(postAdapter);
    }

    private void getData() {
        db.collection("Posts").orderBy("post_date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(FeedActivity.this,error.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                } else {
                    if (value != null) {
                        for (DocumentSnapshot documentSnapshot: value.getDocuments()) {
                            Map<String,Object> dataFromFirestore = documentSnapshot.getData();
                            String comment = (String) dataFromFirestore.get("post_comment");
                            String image = (String) dataFromFirestore.get("post_image");
                            String username = (String) dataFromFirestore.get("post_username");
                            String fullname = (String) dataFromFirestore.get("post_fullname");
                            String pphoto = (String) dataFromFirestore.get("post_pphoto");

                            Date dataDate = documentSnapshot.getDate("post_date", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.getDefault());
                            String date = sdf.format(dataDate);
                            // Tarih için switch case fonksiyorunu.

                            Post post = new Post(comment, image, pphoto, username, fullname, date);
                            arrayList.add(post);
                        }
                        // VERİLERİ SİL
                        postAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_post) {
            Intent intentToUpload = new Intent(FeedActivity.this,UploadActivity.class);
            // finish() yapılabilir
            startActivity(intentToUpload);
        } else if(item.getItemId() == R.id.settings_page) {
            Intent intentToSettings = new Intent(FeedActivity.this,SettingsActivity.class);
            // finish() yapılabilir
            startActivity(intentToSettings);
        } else {
            Intent intentToMain = new Intent(FeedActivity.this, MainActivity.class);
            startActivity(intentToMain);
            finish();
            auth.signOut();
        }
        return super.onOptionsItemSelected(item);
    }
}