package com.sh.chitchat.main_page_fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sh.chitchat.ChatActivity;
import com.sh.chitchat.ProfileActivity;
import com.sh.chitchat.R;
import com.sh.model.Friend;
import com.sh.utils.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private FirebaseAuth mAuth;
    private DatabaseReference mFriendDatabaseReference;
    private DatabaseReference mUserDatabaseReference;

    private RecyclerView mFriendList;

    private String mCurrentUserId;
    private View mMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_friends, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mFriendDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.DB_REF_FRIENDS).child(mCurrentUserId);
        mFriendDatabaseReference.keepSynced(true);
        mUserDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.DB_REF_USERS);
        mUserDatabaseReference.keepSynced(true);
        mFriendList = (RecyclerView) mMainView.findViewById(R.id.friends_friend_list);
        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friend,FriendsFragment.FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friend,FriendsFragment.FriendsViewHolder>(
                Friend.class,
                R.layout.users_single_layout,
                FriendsFragment.FriendsViewHolder.class,
                mFriendDatabaseReference
        ) {

            @Override
            protected void populateViewHolder(final FriendsFragment.FriendsViewHolder viewHolder, final Friend model, int position) {
                final String listUserId = getRef(position).getKey();
                mUserDatabaseReference.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child(Constants.DB_REF_USER_NAME_KEY).getValue().toString();
                        String status = dataSnapshot.child(Constants.DB_REF_USER_STATUS_KEY).getValue().toString();
                        final String image = dataSnapshot.child(Constants.DB_REF_USER_THUMB_IMAGE_KEY).getValue().toString();

                        viewHolder.setName(name);
                        viewHolder.setStatus(status);
                        viewHolder.setImage(image);
                        if(dataSnapshot.hasChild(Constants.DB_REF_USER_ONLINE)){
                            Boolean online = dataSnapshot.child(Constants.DB_REF_USER_ONLINE).getValue().toString().equals("true");
                            viewHolder.setOnlineStatus(online);
                        }
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence option[] = new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                        .setTitle("Select Option")
                                        .setItems(option, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which == 0) {
                                                    Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                                    profileIntent.putExtra(Constants.USER_ID,listUserId);
                                                    startActivity(profileIntent);
                                                }else{
                                                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                                    chatIntent.putExtra(Constants.USER_ID,listUserId);
                                                    chatIntent.putExtra(Constants.DB_REF_USER_NAME_KEY,name);
                                                    chatIntent.putExtra(Constants.DB_REF_USER_THUMB_IMAGE_KEY,image);
                                                    startActivity(chatIntent);
                                                }
                                            }
                                        });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendList.setAdapter(firebaseRecyclerAdapter);

    }

    private static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView mUsername = (TextView) mView.findViewById(R.id.list_user_name);
            mUsername.setText(name);
        }

        public void setStatus(String date) {
            TextView mStatus = (TextView) mView.findViewById(R.id.list_user_status);
            mStatus.setText(date);
        }

        public void setOnlineStatus(Boolean online) {
            ImageView mStatus = (ImageView) mView.findViewById(R.id.img_online_status);
            if (online) {
                mStatus.setVisibility(View.VISIBLE);
            } else {
                mStatus.setVisibility(View.INVISIBLE);
            }
        }

        public void setImage(final String image) {
            final CircleImageView imageView = (CircleImageView) mView.findViewById(R.id.list_user_image);

            Picasso.with(mView.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_image).into(imageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(mView.getContext()).load(image).placeholder(R.drawable.default_image).into(imageView);
                }
            });

        }
    }

}
