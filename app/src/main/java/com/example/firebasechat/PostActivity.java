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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    ProgressDialog loadingBar;
    Toolbar mToolbar;
    ImageButton SelectPostImage;
    Button UpdatePostButton;
    EditText PostDecription;
    public static final int Gallery_Pick = 1;
    FirebaseAuth mAuth;
    DatabaseReference UserPostRef,PostRef;
    String CurrentUserID;
    StorageReference PostImageReference;
    Uri imageUri;
    String Description;
    String saveDate,saveTime,postRandomName,dowloadUrl;
    int countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        AnhXa();
        ActionBar();
        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
        Description  = PostDecription.getText().toString();
        if(TextUtils.isEmpty(Description) || imageUri == null){
            Toast.makeText(this, "Vui lòng nhập description or select image", Toast.LENGTH_SHORT).show();
        }else{
            StoringImageFirebaseStorage();
        }
    }

    private void StoringImageFirebaseStorage() {
        loadingBar.setTitle("Profile image cropper");
        loadingBar.setMessage("Đang load post vui lòng đợi...");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
        Calendar calFoDate = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        saveDate = simpleDateFormat.format(calFoDate.getTime());

        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm");
        saveTime = simpleTimeFormat.format(calFoDate.getTime());
        postRandomName = saveDate + saveTime;
        
        final StorageReference filePath = PostImageReference.child("Post images").child(imageUri.getLastPathSegment()+postRandomName+".PNG");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                loadingBar.dismiss();
                if(task.isSuccessful()){
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            dowloadUrl = uri.toString();
                            SavePostInfomationToDatabase();
                        }
                    });
                }
            }
        });

    }

    private void SavePostInfomationToDatabase() {

        PostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countPosts = (int) dataSnapshot.getChildrenCount();
                }else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        UserPostRef.child(CurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    HashMap postMap = new HashMap();
                    postMap.put("uid", CurrentUserID);
                    postMap.put("date", saveDate);
                    postMap.put("time", saveTime);
                    postMap.put("description", Description);
                    postMap.put("postimage", dowloadUrl);
                    postMap.put("profileimage", userProfileImage);
                    postMap.put("fullname", fullname);
                    postMap.put("counter", countPosts);
                    PostRef.child(CurrentUserID+postRandomName).setValue(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                startActivity(new Intent(PostActivity.this,MainActivity.class));
                                Toast.makeText(PostActivity.this, "Tải post thành công", Toast.LENGTH_SHORT).show();
                            }     else {
                                Toast.makeText(PostActivity.this, "Tải post lên thất bạn", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            SelectPostImage.setImageURI(imageUri);
        }
    }

    private void AnhXa() {
        mToolbar = findViewById(R.id.update_post_toolbar);
        SelectPostImage = findViewById(R.id.select_post_image);
        UpdatePostButton = findViewById(R.id.update_post_button);
        PostDecription = findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UserPostRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        PostImageReference = FirebaseStorage.getInstance().getReference();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            startActivity(new Intent(this,MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void ActionBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

    }
}
