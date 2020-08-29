package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasechat.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ImageButton SendMessageButon, SendImageFileButton;
    EditText UserMessageInput;
    RecyclerView UserMessageList;
    String messageReceiverID, messageReceiverName,messageReceiverProfileImage, messageSenderID,saveDate,saveTime,postRandomName;
    TextView receiverName, userLastSeen;
    CircularImageView receiverProfileImage;
    DatabaseReference RootRef,UserRef;
    FirebaseAuth mAuth;
    final List<Messages> messagesList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        IntializeFields();

        messageReceiverName = getIntent().getStringExtra("username");
        messageReceiverProfileImage = getIntent().getStringExtra("profileimage");
        messageReceiverID = getIntent().getStringExtra("visit_user_id");
        DisplayReceiverInfo();

        SendMessageButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });
        FetchMessages();
    }

    private void FetchMessages() {
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if(dataSnapshot.exists()){
                            Messages messages =  dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    public  void UpdateUserStatus(String state){
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
        UserRef.child(messageSenderID).child("userState").updateChildren(currentStateMap);
    }
    private void SendMessage() {
        UpdateUserStatus("online");
        //
        String messageText = UserMessageInput.getText().toString();
        if(TextUtils.isEmpty(messageText)){
            SendMessageButon.setVisibility(View.INVISIBLE);
            SendMessageButon.setEnabled(true);
        }else {
            String message_sender_ref = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Messages/" + messageReceiverID + "/" + messageSenderID;
            DatabaseReference user_message_ref = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                    .push();
            String message_push_id = user_message_ref.getKey();
            Calendar calFoDate = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            saveDate = simpleDateFormat.format(calFoDate.getTime());

            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm aa");
            saveTime = simpleTimeFormat.format(calFoDate.getTime());
            postRandomName = saveDate + saveTime;

            Map messageTextBody = new HashMap();
                messageTextBody.put("message", messageText);
                messageTextBody.put("time", saveTime);
                messageTextBody.put("date", saveDate);
                messageTextBody.put("type", "Text");
                messageTextBody.put("from", messageSenderID);
             Map messageBodyDetail = new HashMap();
             messageBodyDetail.put(message_sender_ref + "/" + message_push_id, messageTextBody);
             messageBodyDetail.put(message_receiver_ref + "/" + message_push_id, messageTextBody);
            Toast.makeText(this, "Đã vào", Toast.LENGTH_SHORT).show();
             RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Successfully...", Toast.LENGTH_SHORT).show();
                        UserMessageInput.setText("");

                    }else {
                        Toast.makeText(ChatActivity.this, "Send message error...", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
    }

    private void DisplayReceiverInfo() {
        receiverName.setText(messageReceiverName);
        Picasso.with(this).load(messageReceiverProfileImage).into(receiverProfileImage);
        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String type = (String) dataSnapshot.child("userState").child("type").getValue();
                    final String lastDate = (String) dataSnapshot.child("userState").child("date").getValue();
                    final String lastTime = (String) dataSnapshot.child("userState").child("time").getValue();

                    if(type.equals("online")){
                        userLastSeen.setText("Online");
                    }else {
                        userLastSeen.setText("Last seen: "+lastTime+" "+lastDate);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void IntializeFields() {
        mToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(v);

        SendMessageButon = findViewById(R.id.send_message_button);
        SendImageFileButton = findViewById(R.id.send_image_file_button);
        UserMessageInput = findViewById(R.id.input_message);
        UserMessageList = findViewById(R.id.message_list_user);
        receiverName = findViewById(R.id.custom_profile_name);
        receiverProfileImage = findViewById(R.id.custom_profile_image);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        messagesAdapter = new MessagesAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        UserMessageList.setLayoutManager(linearLayoutManager);
        UserMessageList.setHasFixedSize(true);
        UserMessageList.setAdapter(messagesAdapter);
    }

}
