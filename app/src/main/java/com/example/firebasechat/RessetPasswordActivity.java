package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firebasechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RessetPasswordActivity extends AppCompatActivity {

    Toolbar mToolbar;
    EditText ResetEmailInput;
    Button RestPasswordEmailButton;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resset_password);
        ResetEmailInput = findViewById(R.id.rest_password_email);
        RestPasswordEmailButton = findViewById(R.id.rest_password_send_button);
        mToolbar = findViewById(R.id.forget_password_toolbar);
        mAuth = FirebaseAuth.getInstance();
        ActionBar();

        RestPasswordEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ResetEmailInput.getText().toString();
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RessetPasswordActivity.this, "Vui lòng nhập email...", Toast.LENGTH_SHORT).show();
                }else{
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                             if(task.isSuccessful()){
                                 Toast.makeText(RessetPasswordActivity.this, "Vui lòng check email để respassword...", Toast.LENGTH_SHORT).show();
                                 startActivity(new Intent(RessetPasswordActivity.this,LoginActivity.class));
                             }else {
                                 Toast.makeText(RessetPasswordActivity.this, "Lỗi gửi email xác" + task.getException(), Toast.LENGTH_SHORT).show();
                             }
                        }
                    });
                }
            }
        });

    }

    private void ActionBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");
    }
}
