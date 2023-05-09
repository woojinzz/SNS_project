package com.kwj.sns_project.activity;

import static com.kwj.sns_project.activity.Util.isStorageUrl;
import static com.kwj.sns_project.activity.Util.showToast;
import static com.kwj.sns_project.activity.Util.storageUrlToName;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kwj.sns_project.PostInfo;

import java.util.ArrayList;

import listener.OnPostListener;

public class FirebaseHelper {
    private Activity activity;
    private OnPostListener onPostListener;
    private int successCount;

    public FirebaseHelper(Activity activity){
        this.activity = activity;
    }
    public void setOnPostListener(OnPostListener onPostListener){

        this.onPostListener = onPostListener;
    }
    public void storageDelete(final PostInfo postInfo){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        final String id = postInfo.getId();
        ArrayList<String> contentList = postInfo.getContents();
        for (int i = 0; i < contentList.size(); i++) {
            String contents = contentList.get(i);
            if (isStorageUrl(contents)) {//url인지 아닌지 구별
                successCount++;

                StorageReference desertRef = storageRef.child("posts/" + id + "/" + storageUrlToName(contents));
                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        successCount--;
                        storeDelete(id, postInfo);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        showToast(activity, "삭제하지 못했습니다.");
                    }
                });

            }
        }
        storeDelete(id, postInfo);

    }

    private void storeDelete(String id,final PostInfo postInfo) {

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();//초기화
        if (successCount == 0) {

            firebaseFirestore.collection("posts").document(id)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            showToast(activity, "게시글을 삭제했습니다.");
                            onPostListener.onDelete(postInfo);
                           // postUpdate();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Log.w(TAG, "Error deleting document", e);
                            showToast(activity, "게시글을 못삭제했습니다.");
                        }
                    });
        }

    }
}