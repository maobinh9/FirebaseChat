package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    ImageView PostImage;
    TextView PostDescription;
    Button DeletePostButton, EditPostButton;
    String PostKey,currentUserID, databaseUserID,description;
    DatabaseReference ClickPostRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
        AnhXa();

        Intent intent = getIntent();
        PostKey = intent.getStringExtra("PostKey");
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("description")){
                        description = dataSnapshot.child("description").getValue().toString();
                        PostDescription.setText(description);
                    }
                    if(dataSnapshot.hasChild("postimage")){
                        String image = dataSnapshot.child("postimage").getValue().toString();
                        Picasso.with(getApplicationContext()).load(image).into(PostImage);
                    }
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();

                    if(currentUserID.equals(databaseUserID)){
                        DeletePostButton.setVisibility(View.VISIBLE);
                        EditPostButton.setVisibility(View.VISIBLE);
                    }
                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditCurrentPost(description);
                        }
                    });

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });

    }

    private void EditCurrentPost(String description) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Post");
        final EditText editField = new EditText(this);
        editField.setText(description);
        builder.setView(editField);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ClickPostRef.child("description").setValue(editField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Update thành công" + which, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_orange_light);
        dialog.show();

    }

    private void DeleteCurrentPost() {
        ClickPostRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Bạn đã xóa post", Toast.LENGTH_SHORT).show();
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void AnhXa() {
        PostImage = findViewById(R.id.post_image_click);
        PostDescription = findViewById(R.id.post_description_click);
        DeletePostButton = findViewById(R.id.delete_post_button_click);
        EditPostButton = findViewById(R.id.edit_post_button_click);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);
    }
}