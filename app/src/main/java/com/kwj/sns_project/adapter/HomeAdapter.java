package com.kwj.sns_project.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.kwj.sns_project.PostInfo;
import com.kwj.sns_project.R;
import com.kwj.sns_project.activity.FirebaseHelper;
import com.kwj.sns_project.activity.PostActivity;
import com.kwj.sns_project.activity.WritePostActivity;
import com.kwj.sns_project.view.ReadContentsView;

import java.util.ArrayList;

import listener.OnPostListener;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MainViewHolder> {

    private ArrayList<PostInfo> mDataset;
    private Activity activity;

    private FirebaseHelper firebaseHelper;
    private ArrayList<ArrayList<ExoPlayer>> playerArrayListArrayList = new ArrayList<ArrayList<ExoPlayer>>();

    private final int MORE_INDEX = 2;//더보기 숫자

    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;

        public MainViewHolder(CardView v) {
            super(v);
            cardView = v;


        }
    }

    public HomeAdapter(Activity activity, ArrayList<PostInfo> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;

        firebaseHelper = new FirebaseHelper(activity);
    }

    public void setOnPostListener(OnPostListener onPostListener){
        firebaseHelper.setOnPostListener(onPostListener);
    }

    public int getItemViewType(int position) {
        return position;
    }

    public HomeAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        final MainViewHolder mainViewHolder = new MainViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(activity, PostActivity.class);
                intent.putExtra("postInfo", mDataset.get(mainViewHolder.getAdapterPosition()));
                activity.startActivity(intent);

            }
        });

        cardView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view, mainViewHolder.getAdapterPosition());

            }
        });

        return mainViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MainViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        //제목
        TextView tvTitle = cardView.findViewById(R.id.tvTitle);
        tvTitle.setText(mDataset.get(position).getTitle());

        PostInfo postInfo = mDataset.get(position);
        tvTitle.setText(postInfo.getTitle());

        ReadContentsView readContentsView = cardView.findViewById(R.id.readContentsView);
        LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);

        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(postInfo)) {
//            Log.e("로그","태그");
            contentsLayout.setTag(postInfo);
            contentsLayout.removeAllViews();//삭제

            readContentsView.setMoreIndex(MORE_INDEX);
            readContentsView.setPostInfo(postInfo);

            ArrayList<ExoPlayer> playerArrayList = readContentsView.getPlayerArrayList();
            if(playerArrayList != null){
                playerArrayListArrayList.add(playerArrayList);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void showPopup(View v, int position) {

        PopupMenu popup = new PopupMenu(activity, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.modify:

                        myStartActivity(WritePostActivity.class, mDataset.get(position));
                        return true;
                    case R.id.delete://post id를 가져와서 자움

                        firebaseHelper.storageDelete(mDataset.get(position));

                        return true;
                    default:
                        return false;
                }
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.post, popup.getMenu());
        popup.show();
    }


    private void myStartActivity(Class c, PostInfo postInfo) {
        Intent intent = new Intent(activity, c);
        intent.putExtra("postInfo", postInfo);
        activity.startActivity(intent);
    }

    public void playerStop(){

        for(int i = 0; i < playerArrayListArrayList.size(); i++){
            ArrayList<ExoPlayer> playerArrayList = playerArrayListArrayList.get(i);
            for(int ii = 0; ii < playerArrayList.size(); ii++){
                ExoPlayer player = playerArrayList.get(ii);
                if(player.getPlayWhenReady()){
                    player.setPlayWhenReady(false);

                }
            }
        }
    }


}


