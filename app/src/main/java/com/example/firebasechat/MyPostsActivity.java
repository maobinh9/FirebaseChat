package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.firebasechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostList;
    DatabaseReference UserRef,PostRef,LikesRef;
    String currentUserID = null;
    FirebaseAuth mAuth;
    Boolean LikeChecker = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        AnhXa();

        DisplayMyPost();
    }

    private void DisplayMyPost() {
        Query myPostsQuery = PostRef.orderByChild("uid").equalTo(currentUserID);
        FirebaseRecyclerAdapter<Posts,MyPostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(
                Posts.class,
                R.layout.all_posts_layout,
                MyPostsViewHolder.class,
                myPostsQuery) {
            @Override
            protected void populateViewHolder(MyPostsViewHolder postsViewHolder, Posts model, int i) {
                final String PostKey = getRef(i).getKey();
                postsViewHolder.setFullName(model.getFullname());
                postsViewHolder.setDate(model.getDate());
                postsViewHolder.setTime(model.getTime());
                postsViewHolder.setDescription(model.getDescription());
                postsViewHolder.setPostimage(getApplicationContext(),model.getPostimage());
                postsViewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
                postsViewHolder.setLikeButtonStatus(PostKey);
                postsViewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(LikeChecker.equals(true)){
                                    if(dataSnapshot.child(PostKey).hasChild(mAuth.getCurrentUser().getUid())){
                                        LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        LikeChecker = false;
                                    }else {
                                        LikesRef.child(PostKey).child(mAuth.getCurrentUser().getUid()).setValue(true);
                                        LikeChecker = false;
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                postsViewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(getApplicationContext(),CommentsActivity.class);
                        commentIntent.putExtra("Postkey", PostKey);
                        startActivity(commentIntent);
                    }
                });
            }
        };
        myPostList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class MyPostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageButton LikePostButton,CommentPostButton;
        TextView DisplayNoOfLike;
        int countLikes;
        DatabaseReference LikeRef;
        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            LikePostButton = mView.findViewById(R.id.like_button);
            CommentPostButton = mView.findViewById(R.id.comment_button);
            DisplayNoOfLike  = mView.findViewById(R.id.display_no_of_likes);
            LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        }
        public void setFullName(String fullName){
            TextView username = mView.findViewById(R.id.post_user_name);
            username.setText(fullName);
        }
        public void setProfileimage(Context ctx, String profileimage) {
            CircularImageView image = mView.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);
        }
        public void setTime(String time) {
            TextView posttime = mView.findViewById(R.id.post_time);
            posttime.setText(time);
        }
        public void setDate(String date) {
            TextView postdate = mView.findViewById(R.id.post_date);
            postdate.setText(date);
        }
        public void setDescription(String description) {
            TextView postdescription = mView.findViewById(R.id.post_description1);
            postdescription.setText(description);
        }
        public void setPostimage(Context cxt,String postimage) {
            ImageView post = mView.findViewById(R.id.post_image);
            Picasso.with(cxt).load(postimage).into(post);
        }
        public void setLikeButtonStatus(final String Poskey){
            LikeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(Poskey).hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        countLikes = (int) dataSnapshot.child(Poskey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLike.setText(Integer.toString(countLikes));
                    }else{
                        countLikes = (int) dataSnapshot.child(Poskey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLike.setText(Integer.toString(countLikes));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void AnhXa() {
        mToolbar = findViewById(R.id.my_post_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Posts");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myPostList = findViewById(R.id.my_all_post_list);
        myPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
    }
}
