package com.example.friendster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.friendster.DataStruucture.SearchUser;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{


    FirebaseRecyclerAdapter<SearchUser, SearchViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference mUsersDbRef;
    private Query mQuery;
    private RecyclerView rvSearch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        rvSearch = (RecyclerView) findViewById(R.id.xrvSearch);

        mUsersDbRef = FirebaseDatabase.getInstance().getReference().child("users");
        mQuery = mUsersDbRef.orderByChild("name");

        rvSearch.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        attachRecyclerViewAdapter();
    }

    public void attachRecyclerViewAdapter() {
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<SearchUser, SearchViewHolder>(
                SearchUser.class,
                R.layout.single_row_search,
                SearchViewHolder.class,
                mQuery

        ) {
            @Override
            protected void populateViewHolder(SearchViewHolder viewHolder, final SearchUser model, int position) {
                viewHolder.setSearchUserName(model.getName());
                viewHolder.setSearchUserEmail(model.getEmail());
                viewHolder.setSearchUserImage(getApplicationContext(), model.getProfile_image());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SearchActivity.this, AnotherUserProfileActivity.class);
                        intent.putExtra("feed_uid", model.getU_id());
                        startActivity(intent);
                    }
                });
            }
        };
        rvSearch.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        mQuery = mUsersDbRef.orderByChild("name").startAt(query).endAt(query + "~").limitToFirst(5);
        mQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        mQuery = mUsersDbRef.orderByChild("name").startAt(newText).endAt(newText + "~").limitToFirst(5);
        attachRecyclerViewAdapter();

        mQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search_activity, menu);
        MenuItem searchMenu = menu.findItem(R.id.action_search_activity);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search by name...");

        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);

    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public SearchViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setSearchUserName(String name) {
            TextView searchUserName = (TextView) mView.findViewById(R.id.search_userName);
            searchUserName.setText(name);
        }

        public void setSearchUserEmail(String email) {
            TextView searchUserName = (TextView) mView.findViewById(R.id.search_userEmail);
            searchUserName.setText(email);
        }

        public void setSearchUserImage(Context context, String image) {
            CircleImageView searchUserImage = (CircleImageView) mView.findViewById(R.id.search_userImage);
            Glide.with(context)
                    .load(image)
                    .placeholder(R.mipmap.empty_user)
                    .crossFade()
                    .dontAnimate()
                    .into(searchUserImage);
        }
    }


}