package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebasechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import androidx.appcompat.widget.Toolbar;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    NavigationView navigationView;
    DrawerLayout drawerLayout;
    RecyclerView postList;
    Toolbar mToolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    FirebaseAuth mAuth;
    DatabaseReference UserRef,PostRef,LikesRef;
    CircularImageView NavProfileImage;
    TextView NavProfileName;
    ImageButton AddNewPostButton;
    Boolean LikeChecker = false;
    String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AnhXa();
        ActionBar();
        GetImageAndName();

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PostActivity.class));
            }
        });
        DisplayUserPost();
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
        UserRef.child(currentUserID).child("userState").updateChildren(currentStateMap);
    }
    private void DisplayUserPost() {
        Query SortPostIndecendingOrder = PostRef.orderByChild("counter");

        FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                (Posts.class,R.layout.all_posts_layout,PostsViewHolder.class,SortPostIndecendingOrder) {
            @Override
            protected void populateViewHolder(PostsViewHolder postsViewHolder, Posts model, int i) {
                final String PostKey = getRef(i).getKey();
                postsViewHolder.setFullName(model.getFullname());
                postsViewHolder.setDate(model.getDate());
                postsViewHolder.setTime(model.getTime());
                postsViewHolder.setDescription(model.getDescription());
                postsViewHolder.setPostimage(getApplicationContext(),model.getPostimage());
                postsViewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
                postsViewHolder.setLikeButtonStatus(PostKey);
                postsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, ClickPostActivity.class);
                        intent.putExtra("PostKey", PostKey);
                        startActivity(intent);
                    }
                });
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
                        Intent commentIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentIntent.putExtra("Postkey", PostKey);
                        startActivity(commentIntent);
                    }
                });
            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);
        UpdateUserStatus("online");
    }
    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton LikePostButton,CommentPostButton;
        TextView DisplayNoOfLike;
        int countLikes;
        DatabaseReference LikeRef;
        String currentUserID;
        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            LikePostButton = mView.findViewById(R.id.like_button);
            CommentPostButton = mView.findViewById(R.id.comment_button);
            DisplayNoOfLike  = mView.findViewById(R.id.display_no_of_likes);
            LikeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                    if(dataSnapshot.child(Poskey).hasChild(currentUserID)){
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

    private void GetImageAndName() {

                UserRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            if(dataSnapshot.hasChild("profileimage")){
                                String image = dataSnapshot.child("profileimage").getValue().toString();
                                Picasso.with(MainActivity.this).load(image).into(NavProfileImage);
                            }
                            try {
                                NavProfileName.setText(dataSnapshot.child("fullname").getValue().toString());
                            } catch (Exception e) {
                            }

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void ActionBar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = navView.findViewById(R.id.nav_profile_image);
        NavProfileName = navView.findViewById(R.id.nav_user_full_name);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });
    }

    private void AnhXa() {
        navigationView = findViewById(R.id.navigation);
        drawerLayout = findViewById(R.id.drawerlayout);
        mToolbar = findViewById(R.id.main_page_toolbar);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        AddNewPostButton = findViewById(R.id.add_new_post_button);
        postList = findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            SendUserToLoginActivity();
        }else{
            CheckUserExistence();
        }
        UpdateUserStatus("online");
    }
    private void CheckUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateUserStatus("offline");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void UserMenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_post:{
                startActivity(new Intent(MainActivity.this,PostActivity.class));
                break;
            }
            case R.id.nav_profile:{
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            }
            case R.id.nav_home:{
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                break;
            }
            case R.id.nav_friends:{
                startActivity(new Intent(this, FriendsActivity.class));
                break;
            }
            case R.id.nav_find_friends:{
                startActivity(new Intent(MainActivity.this, FindFriendsActivity.class));
                break;
            }
            case R.id.nav_messages:{
                startActivity(new Intent(MainActivity.this,FriendsActivity.class));
                break;
            }
            case R.id.nav_settings:{
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
            case R.id.nav_logout:{
                UpdateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
            }

        }
    }


}
