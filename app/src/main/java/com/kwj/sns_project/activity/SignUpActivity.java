package com.kwj.sns_project.activity;

import static com.kwj.sns_project.activity.Util.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kwj.sns_project.R;

public class SignUpActivity extends BasicActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setToolbarTitle("회원가입");

        // Initialize Firebase Auth // 초기화
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnJoin).setOnClickListener(onClickListener);
        findViewById(R.id.btnLogin).setOnClickListener(onClickListener);

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){

                case R.id.btnJoin:
                    sigUp();
                    break;
                case R.id.btnLogin:
                    Intent intent1 = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent1);
                    break;
            }
        }
    };

    private void sigUp(){ //로그인

        String email =((EditText)findViewById(R.id.etEmail)).getText().toString();//이메일
        String password =((EditText)findViewById(R.id.etPw)).getText().toString();//비번
        String passwordCheck =((EditText)findViewById(R.id.etPwCheck)).getText().toString();//비번 체크

        if(email.length() > 0 && password.length() > 0 &&  passwordCheck.length() > 0){
            if(password.equals(passwordCheck)) {
                final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);//로딩 화면
                loaderLayout.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent intent1 = new Intent(SignUpActivity.this, MainActivity.class);
                                    intent1.addFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);//상위 스택 제거
                                    startActivity(intent1);
                                    showToast(SignUpActivity.this,"회원가입에 성공했습니다.");
                                } else {
                                    if(task.getException()!=null)
                                    {
                                        showToast(SignUpActivity.this,task.getException().toString());
                                    }
                                }
                            }
                        });
            }
            else {
                showToast(SignUpActivity.this,"비밀번호가 일치하지 않습니다.");
            }
        }
        else {
            showToast(SignUpActivity.this,"이메일 또는 비밀번호가 일치하지 않습니다.");


        }
    }

}
