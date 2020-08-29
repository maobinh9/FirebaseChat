package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class SetupActivity extends AppCompatActivity {

    EditText UserName,FullName, CountryName;
    Button SaveInfomationButton;
    CircularImageView ProfileImage;
    FirebaseAuth mAuth;
    DatabaseReference UsersRef;
    String currentUserId;
    ProgressDialog loadingBar;
    final static int Galleyry_Pick = 1;
    StorageReference UserProfileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        AnhXa();

        SaveInfomationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInfomation();
            }
        });
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleyryIntent = new Intent();
                galleyryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleyryIntent.setType("image/*");
                startActivityForResult(galleyryIntent,Galleyry_Pick);
            }
        });
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                   try {
                       String image = dataSnapshot.child("profileimage").getValue().toString();
                       Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.profile).into(ProfileImage);
                   } catch (Exception e) {
                       ProfileImage.setImageAlpha(R.drawable.profile);
                   }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SetupActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
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
                    loadingBar.setMessage("Đang lưu thông tin...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    Uri resultUri = result.getUri();
                    Log.d("BBB", resultUri.toString());
                    final StorageReference filePath = UserProfileImageRef.child(currentUserId+".PNG");
                    filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            loadingBar.dismiss();
                            Toast.makeText(SetupActivity.this, "Save image successfull", Toast.LENGTH_SHORT).show();
                        }
                    });
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            loadingBar.dismiss();
                            UsersRef.child("profileimage").setValue(uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SetupActivity.this, "Save database successfull", Toast.LENGTH_SHORT).show();
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

    private void SaveAccountSetupInfomation() {
        String username =  UserName.getText().toString();
        String fullname =  FullName.getText().toString();
        String country =  CountryName.getText().toString();
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please write username...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Please write fullname...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please write country...", Toast.LENGTH_SHORT).show();
        }else{
            HashMap userMap = new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("country",country);
            userMap.put("status","Hey, i am mao");
            userMap.put("gender","none");
            userMap.put("dob","");
            userMap.put("relationshipstatus","none");

            loadingBar.setTitle("Saving imfomation");
            loadingBar.setMessage("Please wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    loadingBar.dismiss();
                    if(task.isSuccessful()){
                        Toast.makeText(SetupActivity.this, "Account is created successfully", Toast.LENGTH_SHORT).show();
                        SendUserToMainActivity();
                    }else {
                        Toast.makeText(SetupActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void AnhXa() {
        UserName = findViewById(R.id.setup_username);
        FullName = findViewById(R.id.setup_fullname);
        CountryName = findViewById(R.id.setup_country_name);
        ProfileImage = findViewById(R.id.setup_profile_image);
        SaveInfomationButton = findViewById(R.id.setup_infomation_button);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        loadingBar = new ProgressDialog(this);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }
}
