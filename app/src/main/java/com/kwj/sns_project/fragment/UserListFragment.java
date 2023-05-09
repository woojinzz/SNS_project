package com.kwj.sns_project.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kwj.sns_project.PostInfo;
import com.kwj.sns_project.R;
import com.kwj.sns_project.UserInfo;
import com.kwj.sns_project.activity.WritePostActivity;
import com.kwj.sns_project.adapter.HomeAdapter;
import com.kwj.sns_project.adapter.UserListAdapter;

import java.util.ArrayList;
import java.util.Date;

import listener.OnPostListener;

public class UserListFragment extends Fragment {
    private static final String TAG = "UserListFragment";

    private FirebaseFirestore firebaseFirestore;

    private ArrayList<UserInfo> userList;

    private UserListAdapter userListAdapter;

    private boolean updating;

    private boolean topScrolled;

    public UserListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();//초기화
        userListAdapter = new UserListAdapter(getActivity(), userList);

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(userListAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int firstVisibleItemPosition = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();//화면에 보이는 첫번쨰

                if(newState == 1 && firstVisibleItemPosition == 0){
                    topScrolled = true;
                }
                if(newState == 0 && topScrolled){

                    postsUpdate(true);
                    topScrolled = false;
                }
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                super.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();//화면에 보이는 첫번쨰
                int lastVisibleItemPosition = ((LinearLayoutManager)layoutManager).findLastVisibleItemPosition();//화면에 보이는 마지막

                if(totalItemCount -3 <= lastVisibleItemPosition && !updating){
                    postsUpdate(false);
                }

                if(0 < firstVisibleItemPosition){
                    topScrolled = false;
                }

            }
        });
        postsUpdate(false);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public void onPause(){
        super.onPause();
    }
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

//                case R.id.btnLogout:
//                    FirebaseAuth.getInstance().signOut();//파이어베이스 로그아웃 후 로그인 회원가입 로그인 페이지로 이동
//                    myStartActivity(SignUpActivity.class);
//                    finish(); //메인종료
//                    break;

                case R.id.floatingActionButton:
                    myStartActivity(WritePostActivity.class);
                    break;
            }
        }
    };

    OnPostListener onPostListener = new OnPostListener() {
        @Override
        public void onDelete(PostInfo postInfo) {
            userList.remove(postInfo);
            userListAdapter.notifyDataSetChanged();//postList 변경
            Log.e("로그","삭제 성공");
        }

        @Override
        public void onModify() {
            Log.e("로그","수정 성공");
        }
    };

    private void postsUpdate(final boolean clear) {
        updating = true;
        //Date date = userList.size() == 0 || clear ? new Date() : userList.get(userList.size() - 1).getCreatedAt();
        CollectionReference collectionReference = firebaseFirestore.collection("users");
        collectionReference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(clear){
                                userList.clear();
                            }
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                userList.add(new UserInfo(
                                        document.getData().get("name").toString(),
                                        document.getData().get("hp").toString(),
                                        document.getData().get("birthday").toString(),
                                        document.getData().get("addr").toString(),
                                        document.getData().get("photoUrl") == null ? null : document.getData().get("photoUrl").toString()));
                            }
                            userListAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        updating = false;
                    }
                });
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(getActivity(), c);
        startActivityForResult(intent,0);
    }



}