package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText UserEmail, UserPassword,ConfirmPassword;
    Button CreateAccountButton;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        AnhXa();

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void CreateNewAccount() {
        String username = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmpassword = ConfirmPassword.getText().toString();
        
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please write email...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write password...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(confirmpassword)){
            Toast.makeText(this, "Please write Confirmpassword...", Toast.LENGTH_SHORT).show();
        }else if(!password.equals(confirmpassword)){
            Toast.makeText(this, "Please write correct Confirmpassword...", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Creating new Account");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);
            mAuth.createUserWithEmailAndPassword(username,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                SendUserToSetUpActivity();
                                Toast.makeText(RegisterActivity.this, "Thành công", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }else {
                                Toast.makeText(RegisterActivity.this, ""+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void SendUserToSetUpActivity() {
        Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void AnhXa() {
        UserEmail = findViewById(R.id.register_Email);
        UserPassword = findViewById(R.id.register_Password);
        ConfirmPassword = findViewById(R.id.register_Confirm_Password);
        CreateAccountButton = findViewById(R.id.register_account);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);
    }
}
