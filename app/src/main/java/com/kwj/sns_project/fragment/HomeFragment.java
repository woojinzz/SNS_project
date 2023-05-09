package com.kwj.sns_project.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.kwj.sns_project.PostInfo;
import com.kwj.sns_project.R;
import com.kwj.sns_project.activity.WritePostActivity;
import com.kwj.sns_project.adapter.HomeAdapter;

import java.util.ArrayList;
import java.util.Date;

import listener.OnPostListener;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FirebaseFirestore firebaseFirestore;

    private ArrayList<PostInfo> postList;

    private HomeAdapter mainAdapter;

    private boolean updating;

    private boolean topScrolled;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);


        firebaseFirestore = FirebaseFirestore.getInstance();//초기화

        postList = new ArrayList<>();//초기화
        mainAdapter = new HomeAdapter(getActivity(), postList);
        mainAdapter.setOnPostListener(onPostListener);

        final RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        view.findViewById(R.id.floatingActionButton).setOnClickListener(onClickListener);// + 버튼

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mainAdapter);
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

                    postUpdate(false);
                    topScrolled = false;
                    Log.e("로그","업뎃!");
                }
                Log.e("로그","업뎃룰루!");
            }

            public  void onScrolled(RecyclerView recyclerView, int dx, int dy){
                super.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();//아이템 수
                int firstVisibleItemPosition = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();//화면에 보이는 첫번쨰
                int lastVisibleItemPosition = ((LinearLayoutManager)layoutManager).findLastVisibleItemPosition();//화면에 보이는 마지막

                if(totalItemCount -3 <= lastVisibleItemPosition && !updating){
                    postUpdate(false);
                }

                if(0 < firstVisibleItemPosition){
                    topScrolled = false;
                }

                Log.e("로그","onScrolled");
            }
        });
        postUpdate(false);
        return view;
    }
    @Override
    public void onPause(){
        super.onPause();
        mainAdapter.playerStop();
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
            postList.remove(postInfo);
            mainAdapter.notifyDataSetChanged();//postList 변경
            Log.e("로그","삭제 성공");
        }

        @Override
        public void onModify() {
            Log.e("로그","수정 성공");
        }
    };

    private void postUpdate(final boolean clear) {

        updating = true;
        Date date = postList.size() == 0 || clear ? new Date() : postList.get(postList.size() - 1).getCreatedAt();
        CollectionReference collectionReference = firebaseFirestore.collection("posts");
        collectionReference.orderBy("createdAt", Query.Direction.DESCENDING).whereLessThan("createdAt",date).limit(10).get()//게시글 오늘 날짜기준 10개

                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(clear){
                                postList.clear();//데이터 추가 되는 거 삭제
                            }

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                postList.add(new PostInfo(
                                        document.getData().get("title").toString(),
                                        (ArrayList<String>) document.getData().get("contents"),
                                        (ArrayList<String>) document.getData().get("formats"),
                                        document.getData().get("publisher").toString(),
                                        new Date(document.getDate("createdAt").getTime()),
                                        document.getId()));
                            }
                            mainAdapter.notifyDataSetChanged();//postList 변경
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