package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {

    RecyclerView myFriendList;
    DatabaseReference FriendRef, UserRef;
    FirebaseAuth mAuth;
    String online_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        myFriendList = findViewById(R.id.friend_list);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriend();
    }
    public void UpdateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForDate.getTime());
        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);
        UserRef.child(online_user_id).child("userState").updateChildren(currentStateMap);
    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateUserStatus("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        UpdateUserStatus("offline");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateUserStatus("offline");
    }

    private void DisplayAllFriend() {

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(Friends.class,R.layout.all_users_display_layout,FriendsViewHolder.class,FriendRef) {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends model, int i) {
                        final String usersIDs = getRef(i).getKey();

                        UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()){
                                    if(dataSnapshot.hasChild("userState")){
                                        String type = dataSnapshot.child("userState").child("type").getValue().toString();
                                        if(type.equals("online")){
                                            viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                        }else {
                                            viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    final String username = dataSnapshot.child("fullname").getValue().toString();
                                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                                    viewHolder.setProfileimage(getApplicationContext(),profileImage);
                                    viewHolder.setFullname(username);
                                    viewHolder.setDate("Friend Since: "+model.getDate());

                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            CharSequence option[] = new CharSequence[]
                                                    {
                                                        username+" Profile", "Send message"
                                                    };
                                            AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                            builder.setTitle("Select Option");
                                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which){
                                                        case 0:{
                                                            Intent profileIntent = new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                            profileIntent.putExtra("visit_user_id", usersIDs);
                                                            startActivity(profileIntent);
                                                            break;
                                                        }
                                                        case 1:{
                                                            Intent chatIntent = new Intent(FriendsActivity.this,ChatActivity.class);
                                                            chatIntent.putExtra("visit_user_id", usersIDs);
                                                            chatIntent.putExtra("username", username);
                                                            chatIntent.putExtra("profileimage", profileImage);
                                                            startActivity(chatIntent);
                                                            break;
                                                        }
                                                    }
                                                }

                                            });
                                            builder.show();

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                };
        myFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView onlineStatusView;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusView = mView.findViewById(R.id.all_user_online_icon);
        }
        public void setProfileimage(Context context, String profileimage) {
            CircularImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(context).load(profileimage).into(myImage);
        }
        public void setFullname(String fullname) {
            TextView myName = mView.findViewById(R.id.all_users_profile_fullname);
            myName.setText(fullname);
        }
        public void setDate(String status) {
            TextView myStatus = mView.findViewById(R.id.all_users_status);
            myStatus.setText(status);
        }
    }
}
