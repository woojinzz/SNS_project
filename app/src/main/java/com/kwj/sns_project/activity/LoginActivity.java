package com.kwj.sns_project.activity;

import static com.kwj.sns_project.activity.Util.showToast;

import android.content.Intent;
import android.os.Bundle;
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

public class LoginActivity extends BasicActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setToolbarTitle("로그인");

        // Initialize Firebase Auth // 초기화
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnLogin).setOnClickListener(onClickListener);
        findViewById(R.id.btnPwReset).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                case R.id.btnLogin:
                    login();
                    break;

                case R.id.btnPwReset:
                    Intent intent1 = new Intent(LoginActivity.this, PwResetActivity.class);
                    startActivity(intent1);
                    break;
            }
        }
    };

    private void login() {

        String email = ((EditText) findViewById(R.id.etEmail)).getText().toString();//이메일
        String password = ((EditText) findViewById(R.id.etPw)).getText().toString();//비번

        if (email.length() > 0 && password.length() > 0) {

            final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);//로딩 화면
            loaderLayout.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loaderLayout.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                showToast(LoginActivity.this, "로그인에 성공했습니다.");
                                Intent intent1 = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent1);
                            } else {
                                if (task.getException() != null) {
                                    showToast(LoginActivity.this, task.getException().toString());
                                }

                            }
                        }
                    });
        } else {

            showToast(LoginActivity.this, "이메일 또는 비밀번호를 입력해주세요.");

        }

    }

}