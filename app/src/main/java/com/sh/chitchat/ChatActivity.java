package com.sh.chitchat;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sh.utils.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DatabaseReference mRootRef;

    private TextView mUsername,mStatusOrOnline;
    private ImageView mUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        final String user_id = getIntent().getStringExtra(Constants.USER_ID);
        String username = getIntent().getStringExtra(Constants.DB_REF_USER_NAME_KEY);
        final String thumb_image = getIntent().getStringExtra(Constants.DB_REF_USER_THUMB_IMAGE_KEY);


        mUserImage = (ImageView) findViewById(R.id.custom_bar_image);
        mUsername = (TextView) findViewById(R.id.chat_bar_username);
        mStatusOrOnline = (TextView) findViewById(R.id.chat_bar_last_seen);

        mUsername.setText(username);
        Picasso.with(this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                .placeholder(R.drawable.default_image).into(mUserImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(ChatActivity.this).load(thumb_image).placeholder(R.drawable.default_image).into(mUserImage);
            }
        });


        mRootRef = FirebaseDatabase.getInstance().getReference();

        mRootRef.child(Constants.DB_REF_USERS).child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(Constants.DB_REF_USER_ONLINE)){

                    String online = dataSnapshot.child(Constants.DB_REF_USER_ONLINE).getValue().toString();
                    if(online.equals("true"))
                        mStatusOrOnline.setText("Online");
                    else {
                        GetTimeAgo ago = new GetTimeAgo();
                        long lastTime = Long.parseLong(online);
                        String lastSeen = ago.getTimeAgo(lastTime,getApplicationContext());
                        mStatusOrOnline.setText("Last seen " + lastSeen);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mToolbar = (Toolbar) findViewById(R.id.chat_tool_bar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        //getSupportActionBar().setTitle(username);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(actionBarView);


    }
}
