package com.hasanali.socialmediaapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hasanali.socialmediaapp.R;
import com.hasanali.socialmediaapp.databinding.RecyclerRowBinding;
import com.hasanali.socialmediaapp.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    private ArrayList<Post> arrayList;

    public PostAdapter (ArrayList<Post> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PostHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.PostHolder holder, int position) {
        holder.recyclerRowBinding.fullNameUsernameText
                .setText(arrayList.get(position).fullname + " @" + arrayList.get(position).username);
        holder.recyclerRowBinding.userCommentText.setText(arrayList.get(position).comment);
        holder.recyclerRowBinding.postDateText.setText(arrayList.get(position).date.toString());
        Picasso.get().load(arrayList.get(position).image).into(holder.recyclerRowBinding.userPostImage);
        if (arrayList.get(position).pphoto != null) {
            Picasso.get().load(arrayList.get(position).pphoto).into(holder.recyclerRowBinding.userPphoto);
        } else {
            holder.recyclerRowBinding.userPphoto.setImageResource(R.drawable.defaultpp);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class PostHolder extends RecyclerView.ViewHolder{
        RecyclerRowBinding recyclerRowBinding;
        public PostHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }
}
