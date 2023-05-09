package com.kwj.sns_project.activity;

import static com.kwj.sns_project.activity.Util.GALLERY_IMAGE;
import static com.kwj.sns_project.activity.Util.GALLERY_VIDEO;
import static com.kwj.sns_project.activity.Util.INTENT_MEDIA;
import static com.kwj.sns_project.activity.Util.INTENT_PATH;
import static com.kwj.sns_project.activity.Util.isImageFile;
import static com.kwj.sns_project.activity.Util.isStorageUrl;
import static com.kwj.sns_project.activity.Util.isVideoFile;
import static com.kwj.sns_project.activity.Util.showToast;
import static com.kwj.sns_project.activity.Util.storageUrlToName;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kwj.sns_project.R;
import com.kwj.sns_project.PostInfo;
import com.kwj.sns_project.view.ContentsItemView;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;


public class WritePostActivity extends BasicActivity {

    private static final String TAG = "WritePostActivity";
    private FirebaseUser user;
    private StorageReference storageRef;
    private ArrayList<String> pathList = new ArrayList<>();//이미지 경로
    private LinearLayout parent;

    private RelativeLayout buttonsBackgroundLayout;

    private RelativeLayout loaderLayout;

    private ImageView selectedImageVIew;//이미지 저장

    private EditText selectedEditText;
    private EditText etContents;
    private EditText etTitle;
    private PostInfo postInfo;
    private int pathCount;
    private int successCount; //이미지 종료 카운트 pathList 길이랑 같으면 종료

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        setToolbarTitle("게시글 작성");

        parent = findViewById(R.id.contentsLayout);//레이아웃
        buttonsBackgroundLayout = findViewById(R.id.buttonsBackgroundLayout);
        loaderLayout = findViewById(R.id.loaderLayout);//로딩
        etContents = findViewById(R.id.etContents);//로딩
        etTitle = findViewById(R.id.etTitle);//로딩

        findViewById(R.id.btnOk).setOnClickListener(onClickListener); // 확인
        findViewById(R.id.btnImage).setOnClickListener(onClickListener);// 이미지
        findViewById(R.id.btnVideo).setOnClickListener(onClickListener);// 동영상
        findViewById(R.id.btnImgEdit).setOnClickListener(onClickListener);// 이미지 수정
        findViewById(R.id.btnVoidEdit).setOnClickListener(onClickListener);// 동영상 수정
        findViewById(R.id.btnDel).setOnClickListener(onClickListener);// 삭제

        buttonsBackgroundLayout.setOnClickListener(onClickListener);
        etContents.setOnFocusChangeListener(onFocusChangeListener);//내용
        etTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (hasWindowFocus()) {
                    selectedEditText = null;
                }
            }
        });//제목

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
        postInit();
    }

    //parent 안에 레이아웃이 있고 레이아웃 안에 이미지, 텍스트뷰가 있는 구조
    //경로 받아와서 이미지와 텍스트 레이아웃에 넣기
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringExtra(INTENT_PATH);//경로
                    pathList.add(path);//경로가 생성될때마다 추가
                    ContentsItemView contentsItemView = new ContentsItemView(this);

                    if (selectedEditText == null) {
                        parent.addView(contentsItemView);
                    } else {
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            if (parent.getChildAt(i) == selectedEditText.getParent()) {
                                parent.addView(contentsItemView, i + 1);
                                break;
                            }
                        }
                    }
                    contentsItemView.setImage(path);
                    contentsItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            buttonsBackgroundLayout.setVisibility(view.VISIBLE);
                            selectedImageVIew = (ImageView) view;
                        }
                    });
                    contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
                }
                break;

            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringExtra(INTENT_PATH);
                    pathList.set(parent.indexOfChild((View) selectedImageVIew.getParent()) - 1, path);
                    Glide.with(this).load(path).override(1000).into(selectedImageVIew);
                }
                break;
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                case R.id.btnOk://확인
                    storageUpload();
                    break;

                case R.id.btnImage://이미지선택
                    myStartActivity(GalleryActivity.class, GALLERY_IMAGE, 0);
                    break;

                case R.id.btnVideo://비디오선택
                    myStartActivity(GalleryActivity.class, GALLERY_VIDEO, 0);
                    break;

                case R.id.buttonsBackgroundLayout:
                    if (buttonsBackgroundLayout.getVisibility() == View.VISIBLE) {
                        buttonsBackgroundLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.btnImgEdit://이미지수정
                    myStartActivity(GalleryActivity.class, GALLERY_IMAGE, 1);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.btnVoidEdit://비디오수정
                    myStartActivity(GalleryActivity.class, GALLERY_VIDEO, 1);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.btnDel://삭제
                    final View selectedView = (View) selectedImageVIew.getParent();
                    String path = pathList.get(parent.indexOfChild(selectedView) - 1);

                    if(isStorageUrl(path) ){

                        StorageReference desertRef = storageRef.child("posts/" + postInfo.getId() + "/" + storageUrlToName(path));
                        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                showToast(WritePostActivity.this, "파일 삭제 성공.");

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                showToast(WritePostActivity.this, "파일 삭제 실패.");
                            }
                        });
                    }else{
                        pathList.remove(parent.indexOfChild(selectedView) - 1);
                        parent.removeView(selectedView);//View로 형변환
                        buttonsBackgroundLayout.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (hasWindowFocus()) {
                selectedEditText = (EditText) view;

            }
        }
    };

    private void storageUpload() {  //게시판 제목, 내용 업뎃

        final String title = ((EditText) findViewById(R.id.etTitle)).getText().toString();//제목
        if (title.length() > 0) { //제목 공백체크
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentsList = new ArrayList<>();
            final ArrayList<String> formatList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();//사용자 ui
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference(); // 파일 업로드
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = postInfo == null ? firebaseFirestore.collection("posts").document() : firebaseFirestore.collection("posts").document(postInfo.getId());
            final Date date = postInfo == null ? new Date() : postInfo.getCreatedAt();

            for (int i = 0; i < parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(i);
                for (int ii = 0; ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);

                    if (view instanceof EditText) {
                        String text = ((EditText) view).getText().toString();
                        if (text.length() > 0) {
                            contentsList.add(text);
                            formatList.add("text");
                        }
                    } else if (!isStorageUrl(pathList.get(pathCount))) {
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentsList.add(path);

                        if(isImageFile(path)){
                            formatList.add("image");
                        }else if(isVideoFile(path)){
                            formatList.add("video");
                        }else{
                            formatList.add("text");
                        }

                        String[] pathArray = path.split("\\.");
                        final StorageReference mountainImagesRef = storageRef.child("posts/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length - 1]);// 사용자별 저장경로 다르게 설정
                        try {
                            InputStream stream = new FileInputStream(pathList.get(pathCount));
                            StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + (contentsList.size() - 1)).build();
                            UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);//2번째 인자값으로 메타데이터 보내줌
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));

                                    mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            successCount--;
                                            contentsList.set(index, uri.toString());

                                            if (successCount == 0) {
                                                PostInfo postInfo = new PostInfo(title, contentsList, formatList, user.getUid(), date);
                                                storeUpload(documentReference, postInfo);

                                            }
                                        }
                                    });
                                }
                            });
                        } catch (java.io.FileNotFoundException e) {
                            Log.e("로그", "에러: " + e.toString());
                        }
                        pathCount++;
                    }
                }
            }
            if (successCount == 0) {
                storeUpload(documentReference, new PostInfo(title, contentsList, formatList, user.getUid(), date));
            }

        } else {
            showToast(WritePostActivity.this, "제목을 작성해주세요.");
        }
    }

    private void storeUpload(DocumentReference documentReference,final PostInfo postInfo) {//파이어베이스 게시글 업로드

        documentReference.set(postInfo.getPostInfo())//postInfo
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        loaderLayout.setVisibility(View.GONE);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("postInfo",postInfo);
                        setResult(Activity.RESULT_OK,resultIntent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        loaderLayout.setVisibility(View.GONE);
                    }
                });
    }

    private void postInit() {

        if (postInfo != null) {
            etTitle.setText(postInfo.getTitle());
            ArrayList<String> contentList = postInfo.getContents();
            for (int i = 0; i < contentList.size(); i++) {
                String contents = contentList.get(i);
                if (isStorageUrl(contents)) {//url인지 아닌지 구별
                    pathList.add(contents);//경로가 생성될때마다 추가
                    ContentsItemView contentsItemView = new ContentsItemView(this);
                    parent.addView(contentsItemView);

                    contentsItemView.setImage(contents);
                    contentsItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            buttonsBackgroundLayout.setVisibility(view.VISIBLE);
                            selectedImageVIew = (ImageView) view;
                        }
                    });

                    contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
                    if (i < contentList.size() - 1) {
                        String nextContents = contentList.get(i + 1);
                        if (!isStorageUrl(nextContents)) {
                            contentsItemView.setText(nextContents);
                        }
                    }

                } else if (i == 0) {
                    etContents.setText(contents);
                }
            }
        }
    }

    private void myStartActivity(Class c, int media, int requestCode) {
        Intent intent = new Intent(this, c);
        intent.putExtra(INTENT_MEDIA, media);
        startActivityForResult(intent, requestCode);
    }
}
