package com.kwj.sns_project.activity;



import static com.kwj.sns_project.activity.Util.INTENT_PATH;
import static com.kwj.sns_project.activity.Util.showToast;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kwj.sns_project.UserInfo;
import com.kwj.sns_project.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MemberInitActivity extends BasicActivity {

    private static final String TAG = "MemberInitActivity";
    private ImageView ivProfile;
    private String profilePath;
    private FirebaseUser user;
    private RelativeLayout loaderLayout;
    private RelativeLayout buttonsBackgroundLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_init);
        setToolbarTitle("회원정보");

        loaderLayout = findViewById(R.id.loaderLayout);//로딩
        ivProfile = findViewById(R.id.ivProfile);
        buttonsBackgroundLayout = findViewById(R.id.buttonsBackgroundLayout);
        buttonsBackgroundLayout.setOnClickListener(onClickListener);
        ivProfile.setOnClickListener(onClickListener);

        findViewById(R.id.btnOk).setOnClickListener(onClickListener);//확인
        findViewById(R.id.btnFilming).setOnClickListener(onClickListener);//사진촬영
        findViewById(R.id.btnGallery).setOnClickListener(onClickListener);//갤러리
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);
        switch (requestCode){
            case 0 : {
                if(resultCode == Activity.RESULT_OK){
                    profilePath = data.getStringExtra(INTENT_PATH);
                    Glide.with(this).load(profilePath).centerCrop().override(500).into(ivProfile);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){


                case R.id.btnOk: //확인
                    storageUploader();
                    break;

                case R.id.ivProfile: //프로필사진
                    buttonsBackgroundLayout.setVisibility(View.VISIBLE);
                    break;

                case R.id.buttonsBackgroundLayout:
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                    break;

                case R.id.btnFilming: // 촬영
                    myStartActivity(CameraActivity.class);
                    break;

                case R.id.btnGallery: // 갤러리
                    myStartActivity(GalleryActivity.class);
                    break;
            }
        }
    };

    private void storageUploader(){  //사용자 정보 업뎃

        final String name =((EditText)findViewById(R.id.etName)).getText().toString();//이름
        final String hp =((EditText)findViewById(R.id.etHp)).getText().toString();//번호
        final String birthday =((EditText)findViewById(R.id.etBirthDay)).getText().toString();//생년월일
        final String addr =((EditText)findViewById(R.id.etAddr)).getText().toString();//주소

        if(name.length() > 0 && hp.length() > 9 && birthday.length()> 5 && addr.length() > 0){ //공백체크
            loaderLayout.setVisibility(View.VISIBLE);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference(); // 파일 업로드
            user = FirebaseAuth.getInstance().getCurrentUser();//사용자 ui
            final StorageReference mountainImagesRef = storageRef.child("users/"+user.getUid()+"/mountains.jpg");// 사용자별 저장경로 다르게 설정

            if(profilePath == null){
                UserInfo userInfo = new UserInfo(name, hp, birthday, addr);
                storeUploader(userInfo);
            }
            else {
                try{
                    InputStream stream = new FileInputStream(new File(profilePath));
                    UploadTask uploadTask = mountainImagesRef.putStream(stream);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            return mountainImagesRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();

                                UserInfo userInfo = new UserInfo(name, hp, birthday, addr,downloadUri.toString());
                                storeUploader(userInfo);
                            } else {
                                showToast(MemberInitActivity.this,"회원정보를 보내는데 실패했습니다.");
                            }
                        }
                    });
                }catch (java.io.FileNotFoundException e){
                    Log.e("로그","에러: "+ e.toString());
                }
            }
        }
        else {
            showToast(MemberInitActivity.this,"회원정보를 입력해주세요.");
        }
    }
    private void storeUploader(UserInfo userInfo){//파이어베이스 회원정보 업로드
        FirebaseFirestore db = FirebaseFirestore.getInstance();//초기화
        db.collection("users").document(user.getUid()).set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        showToast(MemberInitActivity.this,"회원정보 등록이 완료되었습니다.");
                        loaderLayout.setVisibility(View.GONE);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        loaderLayout.setVisibility(View.VISIBLE);
                        showToast(MemberInitActivity.this,"회원정보 등록을 실패했습니다.");
                    }
                });
    }
    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivityForResult(intent, 0);
    }

}
