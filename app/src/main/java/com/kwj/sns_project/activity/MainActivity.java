package com.kwj.sns_project.activity;


import static com.kwj.sns_project.activity.Util.showToast;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kwj.sns_project.R;
import com.kwj.sns_project.fragment.Camera2BasicFragment;
import com.kwj.sns_project.fragment.HomeFragment;
import com.kwj.sns_project.fragment.UserInfoFragment;
import com.kwj.sns_project.fragment.UserListFragment;


public class MainActivity extends BasicActivity {

    private static final String TAG = "MainActivity";
    private final long finishtimeed = 1000;    //뒤로가기 버튼 이벤트
    private long presstime = 0;                //뒤로가기 버튼 이벤트

    private  FirebaseUser firebaseuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbarTitle(getResources().getString(R.string.app_name));
        //FirebaseAuth.getInstance().signOut();// 로그아웃
        Init();
    }

    @Override
    protected void onResume() {//엑티비티 재실행
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    //뒤로가기 버튼 이벤트
    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - presstime;

        if (0 <= intervalTime && finishtimeed >= intervalTime) {
            moveTaskToBack(true); // 태스크를 백그라운드로 이동
            finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
            System.exit(0);
        } else {
            presstime = tempTime;
            showToast(MainActivity.this, "한번 더 누르면 앱이 종료됩니다..");
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                //Init();
                break;
        }
    }
    public void Init(){
        firebaseuser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseuser == null) {//사용자 정보 없으면 회원가입 페이지로 이동
            myStartActivity(SignUpActivity.class);
        } else { //사용자 정보 있으면 플레그먼트 화면 호출

            DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users").document(firebaseuser.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                                myStartActivity(MemberInitActivity.class);
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
            HomeFragment homeFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, homeFragment)
                    .commit();

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.home:
                            HomeFragment homeFragment = new HomeFragment();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, homeFragment)
                                    .commit();
                            return true;
                        case R.id.myInfo:
                            UserInfoFragment userInfoFragment = new UserInfoFragment();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, userInfoFragment)
                                    .commit();
                            return true;
                        case R.id.userList:
                            UserListFragment userListFragment = new UserListFragment();
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, userListFragment)
                                    .commit();
                            return true;

                        case R.id.logout:
                            FirebaseAuth.getInstance().signOut();//파이어베이스 로그아웃 후 로그인 회원가입 로그인 페이지로 이동
                            myStartActivity(SignUpActivity.class);
                            finish(); //메인종료
                            break;
                    }

                    return false;

                }
            });
        }
    }
    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivityForResult(intent, 1);
    }


}
