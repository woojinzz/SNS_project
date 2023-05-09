package com.kwj.sns_project.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.kwj.sns_project.PostInfo;
import com.kwj.sns_project.R;
import com.kwj.sns_project.UserInfo;
import com.kwj.sns_project.activity.FirebaseHelper;
import com.kwj.sns_project.activity.PostActivity;
import com.kwj.sns_project.activity.WritePostActivity;
import com.kwj.sns_project.view.ReadContentsView;

import java.util.ArrayList;

import listener.OnPostListener;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MainViewHolder> {

    private ArrayList<UserInfo> mDataset;
    private Activity activity;

    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;

        public MainViewHolder(CardView v) {
            super(v);
            cardView = v;


        }
    }

    public UserListAdapter(Activity activity, ArrayList<UserInfo> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;
    }

    public int getItemViewType(int position) {
        return position;
    }

    public UserListAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
        final MainViewHolder mainViewHolder = new MainViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

            }
        });

        return mainViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MainViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        //제목
        ImageView photoImageView = cardView.findViewById(R.id.photoImageView);
        TextView nameTextView = cardView.findViewById(R.id.tvName);//이름
        TextView addressTextView = cardView.findViewById(R.id.tvAddr);//지역

        UserInfo userInfo = mDataset.get(position);
        if(mDataset.get(position).getPhotoUrl() != null){

            Glide.with(activity).load(mDataset.get(position).getPhotoUrl()).centerCrop().override(500).into(photoImageView);

        }
        nameTextView.setText(userInfo.getName());
        addressTextView.setText(userInfo.getAddr());

    }



    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }



}


