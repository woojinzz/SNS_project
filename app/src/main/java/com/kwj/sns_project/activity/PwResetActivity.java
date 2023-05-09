package com.kwj.sns_project.activity;

import static com.kwj.sns_project.activity.Util.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kwj.sns_project.R;

public class PwResetActivity extends BasicActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pw_reset);
        setToolbarTitle("비밀번호 재설정");

        // Initialize Firebase Auth // 초기화
        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.btnSend).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnSend:
                    send();
                    break;
            }
        }
    };

    private void send() {  // 이메일 보내기

        String email = ((EditText) findViewById(R.id.etEmail)).getText().toString();//이메일
        if (email.length() > 0) {
            final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loaderLayout.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                showToast(PwResetActivity.this,"이메일을 보냈습니다.");
                            }
                        }
                    });
        } else {
            showToast(PwResetActivity.this,"이메일 또는 비밀번호를 입력해주세요.");
        }

    }

}
