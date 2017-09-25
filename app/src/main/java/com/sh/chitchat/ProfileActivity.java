package com.sh.chitchat;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sh.utils.Constants;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private ImageView mImageView;
    private TextView mUsername;
    private TextView mStatus,mTotalFriends;
    private Button mFriendRequestButton,mDeclineRequestButton;

    private ProgressDialog mProgressDialog;

    private int mCurrentState;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFriendRequestDatabaseReference;
    private DatabaseReference mFriendDatabaseReference;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String user_id = getIntent().getStringExtra(Constants.USER_ID);
        mImageView = (ImageView) findViewById(R.id.profile_image);
        mUsername = (TextView) findViewById(R.id.profile_username);
        mStatus = (TextView) findViewById(R.id.profile_status);
        mTotalFriends = (TextView) findViewById(R.id.profile_total_friends);
        mFriendRequestButton = (Button) findViewById(R.id.send_friend_request);
        mDeclineRequestButton = (Button) findViewById(R.id.decline_friend_request);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Fetching User Detail");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.DB_REF_USERS).child(user_id);
        mFriendRequestDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.DB_REF_FRIEND_REQ);
        mFriendDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.DB_REF_FRIENDS);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG,dataSnapshot + " " + user_id);
                String username = dataSnapshot.child(Constants.DB_REF_USER_NAME_KEY).getValue().toString();
                String status = dataSnapshot.child(Constants.DB_REF_USER_STATUS_KEY).getValue().toString();
                String image = dataSnapshot.child(Constants.DB_REF_USER_IMAGE_KEY).getValue().toString();
                mUsername.setText(username);
                mStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_image).into(mImageView);
                mFriendRequestDatabaseReference.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)) {
                            String requestType = dataSnapshot.child(user_id).child(Constants.DB_REF_FRIEND_REQ_TYPE_KEY).getValue().toString();
                            if (requestType.equals(Constants.REQUEST_TYPE_SENT)) {
                                mCurrentState = Constants.REQUEST_SENT;
                            }else if(requestType.equals(Constants.REQUEST_TYPE_RECEIVED)){
                                mCurrentState = Constants.REQUEST_RECEIVED;
                            }
                        }else{
                            mFriendDatabaseReference.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)) {
                                        mCurrentState = Constants.FRIENDS;
                                    }else{
                                        mCurrentState = Constants.NOT_FRIENDS;
                                    }
                                    updateUI();
                                    mProgressDialog.dismiss();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                        mProgressDialog.dismiss();
                        updateUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mProgressDialog.dismiss();
                    }
                });
                updateUI();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFriendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendRequestButton.setEnabled(false);
                // -------------- SENDING FRIEND REQUEST
                if(mCurrentState == Constants.NOT_FRIENDS){

                    DatabaseReference newNotificationRef = mRootRef.child(Constants.DB_REF_NOTIFICATION).child(user_id).push();
                    String notificationId = newNotificationRef.getKey();
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put(Constants.NOTIFICATION_FROM,mCurrentUser.getUid());
                    notificationData.put(Constants.NOTIFICATION_TYPE,Constants.NOTIFICATION_REQUEST);
                    Map request = new HashMap();

                    request.put(Constants.DB_REF_FRIEND_REQ +"/"+ mCurrentUser.getUid()+ "/"+user_id+ "/" + Constants.DB_REF_FRIEND_REQ_TYPE_KEY ,Constants.REQUEST_TYPE_SENT);
                    request.put(Constants.DB_REF_FRIEND_REQ +"/"+ user_id+"/"+mCurrentUser.getUid()+ "/" + Constants.DB_REF_FRIEND_REQ_TYPE_KEY ,Constants.REQUEST_TYPE_RECEIVED);
                    request.put(Constants.DB_REF_NOTIFICATION +"/"+ user_id + "/" +notificationId,notificationData);

                    mRootRef.updateChildren(request, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null) {
                                Toast.makeText(ProfileActivity.this, "Friend Request Sent", Toast.LENGTH_SHORT);
                                mCurrentState = Constants.REQUEST_SENT;
                            }
                        }
                    });

                }

                // ------------ CANCEL FRIEND REQUEST

                if(mCurrentState == Constants.REQUEST_SENT){
                    Map request = new HashMap();
                    request.put(Constants.DB_REF_FRIEND_REQ +"/"+ mCurrentUser.getUid()+ "/"+user_id+ "/" + Constants.DB_REF_FRIEND_REQ_TYPE_KEY ,null);
                    request.put(Constants.DB_REF_FRIEND_REQ +"/"+ user_id+"/"+mCurrentUser.getUid()+ "/" + Constants.DB_REF_FRIEND_REQ_TYPE_KEY ,null);

                    mRootRef.updateChildren(request, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null) {
                                Toast.makeText(ProfileActivity.this, "Friend Request canceled", Toast.LENGTH_SHORT);
                                mCurrentState = Constants.NOT_FRIENDS;
                            }
                        }
                    });
                }


                // -------------- ACCEPT FRIEND REQUEST

                if(mCurrentState == Constants.REQUEST_RECEIVED){
                    final String currentDate = SimpleDateFormat.getDateTimeInstance().format(new Date());
                    Map request = new HashMap();
                    request.put(Constants.DB_REF_FRIENDS +"/"+ mCurrentUser.getUid()+ "/"+user_id+"/" + Constants.DB_REF_FRIEND_DATE_KEY ,currentDate);
                    request.put(Constants.DB_REF_FRIENDS +"/"+ user_id+"/"+mCurrentUser.getUid()+"/" + Constants.DB_REF_FRIEND_DATE_KEY  ,currentDate);

                    request.put(Constants.DB_REF_FRIEND_REQ +"/"+ mCurrentUser.getUid()+ "/"+user_id+ "/" + Constants.DB_REF_FRIEND_REQ_TYPE_KEY ,null);
                    request.put(Constants.DB_REF_FRIEND_REQ +"/"+ user_id+"/"+mCurrentUser.getUid()+ "/" + Constants.DB_REF_FRIEND_REQ_TYPE_KEY ,null);

                    mRootRef.updateChildren(request, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null) {
                                Toast.makeText(ProfileActivity.this, "Friend Request accepted", Toast.LENGTH_SHORT);
                                mCurrentState = Constants.FRIENDS;
                            }
                        }
                    });

                }

                // ---------- UN FRIEND

                if(mCurrentState == Constants.FRIENDS) {

                    Map request = new HashMap();
                    request.put(Constants.DB_REF_FRIENDS +"/"+ mCurrentUser.getUid()+ "/"+user_id,null);
                    request.put(Constants.DB_REF_FRIENDS +"/"+ user_id+"/"+mCurrentUser.getUid(),null);
                    mRootRef.updateChildren(request, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                Toast.makeText(ProfileActivity.this,"un friend",Toast.LENGTH_SHORT);
                                mCurrentState = Constants.NOT_FRIENDS;
                            }
                        }
                    });
                }
                updateUI();
                mFriendRequestButton.setEnabled(true);
            }
        });
        mDeclineRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map request = new HashMap();
                request.put(Constants.DB_REF_FRIEND_REQ +"/"+ mCurrentUser.getUid()+ "/"+user_id+ "/" + Constants.DB_REF_FRIEND_REQ_TYPE_KEY ,null);
                request.put(Constants.DB_REF_FRIEND_REQ +"/"+ user_id+"/"+mCurrentUser.getUid()+ "/" + Constants.DB_REF_FRIEND_REQ_TYPE_KEY ,null);

                mRootRef.updateChildren(request, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            Toast.makeText(ProfileActivity.this, "Friend Request Declined", Toast.LENGTH_SHORT);
                            mCurrentState = Constants.NOT_FRIENDS;
                        }
                    }
                });
                updateUI();
            }

        });

    }

    private void updateUI() {
        Log.i(TAG,"updateUI " + mCurrentState);
        mDeclineRequestButton.setVisibility(View.INVISIBLE);
        mDeclineRequestButton.setEnabled(false);
        switch(mCurrentState){
            case Constants.NOT_FRIENDS:
                mFriendRequestButton.setText("Send Friend Request");
                break;
            case Constants.FRIENDS:
                mFriendRequestButton.setText("UnFriend this person");
                break;
            case Constants.REQUEST_RECEIVED:
                mDeclineRequestButton.setVisibility(View.VISIBLE);
                mDeclineRequestButton.setEnabled(true);
                mFriendRequestButton.setText("Accept Friend Request");
                break;
            case Constants.REQUEST_SENT:
                mFriendRequestButton.setText("Cancel Friend Request");
                break;
        }

    }
}
