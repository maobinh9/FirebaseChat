package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    ImageButton PostCommentButton;
    EditText CommentInputText;
    RecyclerView CommentList;
    String Post_key;
    DatabaseReference UserRef,PostRef;
    String CurrentUserID,UserName;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        AnhXa();
        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserRef.child(CurrentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            UserName = dataSnapshot.child("username").getValue().toString();
                            ValidateComment(UserName);
                            CommentInputText.setText(" ");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comments,CommentsViewHoler> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<Comments, CommentsViewHoler>
                        (Comments.class,R.layout.all_comments_layout,CommentsViewHoler.class,PostRef) {
                    @Override
                    protected void populateViewHolder(CommentsViewHoler viewHoler, Comments model, int i) {
                        viewHoler.setUsername(model.getUsername());
                        viewHoler.setComments(model.getComment());
                        viewHoler.setDate(model.getDate());
                        viewHoler.setTime(model.getTime());
                    }
                };
        CommentList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHoler extends RecyclerView.ViewHolder{
        View mView;
        public CommentsViewHoler(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setUsername(String username) {
            TextView myUserName = mView.findViewById(R.id.comment_username);
            myUserName.setText(username);
        }
        public void setTime(String time) {
            TextView myTime = mView.findViewById(R.id.comment_time);
            myTime.setText(time);
        }
        public void setDate(String date) {
            TextView myDate = mView.findViewById(R.id.comment_date);
            myDate.setText(date);
        }
        public void setComments(String comments) {
            TextView myComment = mView.findViewById(R.id.comment_text);
            myComment.setText("Comments: "+comments);
        }
    }

    private void ValidateComment(String userName) {
        String commentText = CommentInputText.getText().toString();
        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Vui lòng nhập comment...", Toast.LENGTH_SHORT).show();
        }else {
            Calendar calFoDate = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            final String saveDate = simpleDateFormat.format(calFoDate.getTime());

            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm");
            final String saveTime = simpleTimeFormat.format(calFoDate.getTime());
            final String RandomKey = CurrentUserID + saveDate + saveTime;

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",CurrentUserID);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveDate);
            commentsMap.put("time",saveTime);
            commentsMap.put("username",userName);

            PostRef.child(RandomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this, "You have comment check successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(CommentsActivity.this, "Comment thất bại" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void AnhXa() {
        PostCommentButton = findViewById(R.id.post_comment_btn);
        CommentInputText = findViewById(R.id.comment_input);
        CommentList = findViewById(R.id.comment_list);
        CommentList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        CommentList.setLayoutManager(layoutManager);
        Post_key = getIntent().getExtras().get("Postkey").toString();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_key).child("Comments");
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
    }
}
