package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PersonProfileActivity extends AppCompatActivity {
    TextView userName, userProfileName,userCountry,userStatus, userGender,userRelation, userDOB;
    CircularImageView userProfileImage;
    Button SendFriendRequestButton, DeclineFriendRequestButton;
    DatabaseReference FriendRequestRef, UsersRef, FriendRef;
    FirebaseAuth mAuth;
    String sendUserID, receiveUserID, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        AnhXa();
        receiveUserID = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiveUserID);
        UsersRef.addValueEventListener(new ValueEventListener() {
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
                    userName.setText("@"+myUserName);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("Date of birth: "+myDOB);
                    userCountry.setText("Country: "+myCountry);
                    userGender.setText("Gender: "+myGender);
                    userRelation.setText("Relationship: "+myRelationStatus);

                    MaintananceButton();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);
        if(!sendUserID.equals(receiveUserID)){
            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendFriendRequestButton.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequestPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends")){
                        UnfriendAnExiststringFriend();
                    }
                }
            });
        }else {
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void UnfriendAnExiststringFriend() {
        FriendRef.child(sendUserID).child(receiveUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRef.child(receiveUserID).child(sendUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequestButton.setText("Send friend request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }   else {
                            Toast.makeText(PersonProfileActivity.this, "Lỗi"+ task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = simpleDateFormat.format(calendar.getTime());

        FriendRef.child(sendUserID).child(receiveUserID).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    FriendRef.child(receiveUserID).child(sendUserID).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                FriendRequestRef.child(sendUserID).child(receiveUserID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    FriendRequestRef.child(receiveUserID).child(sendUserID).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){
                                                                        SendFriendRequestButton.setEnabled(true);
                                                                        CURRENT_STATE = "friends";
                                                                        SendFriendRequestButton.setText("Unfriend this person");

                                                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                        DeclineFriendRequestButton.setEnabled(false);
                                                                    }
                                                                }
                                                            });
                                                }   else {
                                                    Toast.makeText(PersonProfileActivity.this, "Lỗi"+ task.getException(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CancelFriendRequest() {
        FriendRequestRef.child(sendUserID).child(receiveUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestRef.child(receiveUserID).child(sendUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendFriendRequestButton.setText("Send friend request");
                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }   else {
                            Toast.makeText(PersonProfileActivity.this, "Lỗi"+ task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void MaintananceButton() {
        FriendRequestRef.child(sendUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiveUserID)){
                            String request_type = dataSnapshot.child(receiveUserID).child("request_type").getValue().toString();
                            if(request_type.equals("sent")){
                                CURRENT_STATE = "request_sent";
                                SendFriendRequestButton.setText("Cancel friend request");

                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);
                            }else if(request_type.equals("received")){
                                CURRENT_STATE = "request_received";
                                SendFriendRequestButton.setText("Accept Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestButton.setEnabled(true);

                                DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(PersonProfileActivity.this, "Cancel friend", Toast.LENGTH_SHORT).show();
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }else {
                            FriendRef.child(sendUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiveUserID)){
                                                CURRENT_STATE = "friends";
                                                SendFriendRequestButton.setText("Unfriend this person");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendFriendRequestPerson() {
        FriendRequestRef.child(sendUserID).child(receiveUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestRef.child(receiveUserID).child(sendUserID).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                SendFriendRequestButton.setText("Cancel friend request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }   else {
                            Toast.makeText(PersonProfileActivity.this, "Lỗi"+ task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void AnhXa() {
        userName = findViewById(R.id.person_username);
        userProfileName = findViewById(R.id.person_profile_fullname);
        userCountry = findViewById(R.id.person_country);
        userStatus = findViewById(R.id.person_profile_status);
        userGender = findViewById(R.id.person_gender);
        userRelation = findViewById(R.id.person_relationship_status);
        userDOB = findViewById(R.id.person_dob);
        userProfileImage = findViewById(R.id.person_profile_pic);
        SendFriendRequestButton = findViewById(R.id.person_send_friend_reuqest_btn);
        DeclineFriendRequestButton = findViewById(R.id.person_decline_friend_reuqest_btn);
        mAuth = FirebaseAuth.getInstance();
        sendUserID = mAuth.getCurrentUser().getUid();
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        CURRENT_STATE = "not_friends";
    }
}
