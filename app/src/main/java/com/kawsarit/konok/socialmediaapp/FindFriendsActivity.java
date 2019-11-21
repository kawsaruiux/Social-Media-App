package com.kawsarit.konok.socialmediaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kawsarit.konok.socialmediaapp.Model.FindFriend;
import com.kawsarit.konok.socialmediaapp.ViewHolder.FindFriendViewHolder;
import com.squareup.picasso.Picasso;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private ImageButton SearchButton;
    private EditText SearchInputText;
    private RecyclerView SearchResultList;

    private String SearchInput;     //we will convert 'SearchInputText' to String and save it to the 'SearchInput'

    private DatabaseReference allUserDatabaseRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        allUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");


        mToolbar = (Toolbar) findViewById(R.id.find_friend_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //this line for create a back(arrow) button
        getSupportActionBar().setTitle("Find Friend");

        SearchButton = (ImageButton) findViewById(R.id.search_friend_button);
        SearchInputText = (EditText) findViewById(R.id.search_box_input);

        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));
        
        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                SearchInput = SearchInputText.getText().toString();

                onStart();
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseRecyclerOptions<FindFriend> options =       //first configure the adapter by building FirebaseRecyclerOptions.
                new FirebaseRecyclerOptions.Builder<FindFriend>()
                .setQuery(reference.orderByChild("fullname").startAt(SearchInput), FindFriend.class)
                .build();

        //The FirebaseRecyclerAdapter binds a Query to a RecyclerView. When data is added, removed, or changed these updates are automatically applied to your UI in real time.
        FirebaseRecyclerAdapter<FindFriend, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<FindFriend, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull FindFriend model) {

                        holder.findFriendName.setText(model.getFullname());
                        holder.findFriendStatus.setText(model.getStatus());
                        Picasso.get().load(model.getProfileimage()).into(holder.findFriendImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String visit_user_id = getRef(position).getKey();

                                Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                                profileIntent.putExtra("visited_user_id", visit_user_id);   //for identify which one send friend request
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout, viewGroup, false);
                        FindFriendViewHolder holder = new FindFriendViewHolder(view);
                        return holder;
                    }
                };
        SearchResultList.setAdapter(adapter);
        adapter.startListening();
    }
}
