package com.example.firebasechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.firebasechat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    Toolbar mToolbar;
    ImageButton SearchButton;
    EditText SearchInputText;
    RecyclerView SearchResultList;
    DatabaseReference UserDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        AnhXa();

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = SearchInputText.getText().toString();
                SearchPeopleAndFriends(search);
            }
        });
    }

    private void SearchPeopleAndFriends(String search) {
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show();
        Query SearchPeopleAndFriendsQuery = UserDatabaseRef.orderByChild("fullname").startAt(search).endAt(search + "\uf8ff");

        FirebaseRecyclerAdapter<FindFriends,FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>
                (FindFriends.class,R.layout.all_users_display_layout,FindFriendsViewHolder.class,SearchPeopleAndFriendsQuery) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, FindFriends model, final int i) {
                viewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());
                viewHolder.setFullname(model.getFullname());
                viewHolder.setStatus(model.getStatus());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(i).getKey();
                        Intent profileInten = new Intent(getApplicationContext(), PersonProfileActivity.class);
                        profileInten.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileInten);
                    }
                });
            }
        };
        SearchResultList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setProfileimage(Context context, String profileimage) {
            CircularImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(context).load(profileimage).into(myImage);
        }
        public void setFullname(String fullname) {
            TextView myName = mView.findViewById(R.id.all_users_profile_fullname);
            myName.setText(fullname);
        }
        public void setStatus(String status) {
            TextView myStatus = mView.findViewById(R.id.all_users_status);
            myStatus.setText(status);
        }

    }

    private void AnhXa() {
        mToolbar = findViewById(R.id.find_friends_appBar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search people");
        SearchInputText = findViewById(R.id.search_box_input);
        SearchResultList = findViewById(R.id.search_result_list);
        SearchButton = findViewById(R.id.search_people_friends_button);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));
        UserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

    }
}
