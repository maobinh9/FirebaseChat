package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firebasechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    Toolbar mToolbar;
    EditText userName, userProfileName,userCountry,userStatus, userGender,userRelation, userDOB;
    Button updateAccountSettingButton;
    CircularImageView userProfileImage;
    DatabaseReference SettingUserRef;
    FirebaseAuth mAuth;
    String currentUserID;
    final static int Galleyry_Pick = 1;
    ProgressDialog loadingBar;
    StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        AnhXa();
        ActionBar();
        DisplayAccount();
        updateAccountSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Galleyry_Pick);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Galleyry_Pick && resultCode == RESULT_OK && data != null){
            Uri uriImage = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1)
                    .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Profile image cropper");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.setMessage("Đang loading ảnh vui lòng đợi...");
                loadingBar.show();

                Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImageRef.child(currentUserID+".PNG");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        loadingBar.dismiss();
                        Toast.makeText(SettingsActivity.this, "Save image successfull", Toast.LENGTH_SHORT).show();
                    }
                });
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        loadingBar.dismiss();
                        SettingUserRef.child("profileimage").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SettingsActivity.this, "Save database successfull", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SettingsActivity.this,SettingsActivity.class));
                            }
                        });
                    }
                });
            }else {
                loadingBar.dismiss();
                Toast.makeText(this, "Lỗi cắt ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String profilename = userProfileName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Vui lòng nhập username...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(profilename)){
            Toast.makeText(this, "Vui lòng nhập fullname...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "Vui lòng nhập status...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(dob)){
            Toast.makeText(this, "Vui lòng nhập ngày sinh...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Vui lòng nhập country...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Vui lòng nhập gender...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(relation)){
            Toast.makeText(this, "Vui lòng nhập relationship...", Toast.LENGTH_SHORT).show();
        }else {
            loadingBar.setTitle("Profile image cropper");
            loadingBar.setMessage("Đang lưu thông tin...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            UpdateAccountInfo(username,profilename,status,dob,country,gender,relation);
        }
    }

    private void UpdateAccountInfo(String username, String profilename, String status, String dob, String country, String gender, String relation) {
        HashMap usermap = new HashMap();
        usermap.put("username", username);
        usermap.put("fullname", profilename);
        usermap.put("status", status);
        usermap.put("dob", dob);
        usermap.put("country", country);
        usermap.put("gender", gender);
        usermap.put("relationshipstatus", relation);
        SettingUserRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                loadingBar.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(SettingsActivity.this, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this,MainActivity.class));
                }else {
                    Toast.makeText(SettingsActivity.this, "Lưu thất bại" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void DisplayAccount() {
        SettingUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.with(getApplicationContext()).load(myProfileImage).into(userProfileImage);
                    userName.setText(myUserName);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText(myDOB);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationStatus);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ActionBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account settings");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void AnhXa() {
        mToolbar = findViewById(R.id.settings_toolbar);
        userName = findViewById(R.id.settings_username);
        userProfileName = findViewById(R.id.settings_profile_fullname);
        userCountry = findViewById(R.id.settings_country);
        userStatus = findViewById(R.id.settings_status);
        userGender = findViewById(R.id.settings_gender);
        userRelation = findViewById(R.id.settings_relationship_status);
        userDOB = findViewById(R.id.settings_dob);
        updateAccountSettingButton = findViewById(R.id.update_account_settings_button);
        userProfileImage = findViewById(R.id.settings_progile_image);
        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        SettingUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }
}
