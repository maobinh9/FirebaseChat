package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.firebasechat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    TextView userName, userProfileName,userCountry,userStatus, userGender,userRelation, userDOB;
    CircularImageView userProfileImage;
    DatabaseReference UserProfileRef,FriendsRef, PostRef;
    FirebaseAuth mAuth;
    String currentID;
    Button MyPosts, MyFriends;
    int countFriends = 0, countPost = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        AnhXa();
        UserProfileRef.addValueEventListener(new ValueEventListener() {
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
                    userDOB.setText("Date of birth: "+myDOB);
                    userCountry.setText("Country: "+myCountry);
                    userGender.setText("Gender: "+myGender);
                    userRelation.setText("Relationship: "+myRelationStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        MyFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, FriendsActivity.class));
            }
        });
        FriendsRef.child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    MyFriends.setText(countFriends+" Friends");
                }else {
                    MyFriends.setText("0 Friends");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this,MyPostsActivity.class));
            }
        });
        PostRef.orderByChild("uid").startAt(currentID).endAt(currentID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            countPost = (int) dataSnapshot.getChildrenCount();
                            MyPosts.setText(countPost + " Posts");
                        }else {
                            MyPosts.setText(0 + " Posts");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void AnhXa() {
        userName = findViewById(R.id.my_username);
        userProfileName = findViewById(R.id.my_profile_fullname);
        userCountry = findViewById(R.id.my_country);
        userStatus = findViewById(R.id.my_profile_status);
        userGender = findViewById(R.id.my_gender);
        userRelation = findViewById(R.id.my_relationship_status);
        userDOB = findViewById(R.id.my_dob);
        userProfileImage = findViewById(R.id.my_profile_pic);
        MyPosts = findViewById(R.id.my_post_button);
        MyFriends = findViewById(R.id.my_friend_button);
        mAuth = FirebaseAuth.getInstance();
        currentID = mAuth.getCurrentUser().getUid();
        UserProfileRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentID);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }
}
