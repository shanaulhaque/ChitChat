package com.sh.chitchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sh.utils.Constants;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mTipStatus;
    private Button mSaveStatusButton;

    private DatabaseReference mStatusDatabaseReference;
    private FirebaseUser mFirebaseUser;

    private  ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new ProgressDialog(this);

        mTipStatus = (TextInputLayout) findViewById(R.id.tip_new_status);
        mSaveStatusButton = (Button) findViewById(R.id.save_status);
        String status = getIntent().getStringExtra(Constants.DB_REF_USER_STATUS_KEY);
        mTipStatus.getEditText().setText(status);


        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mStatusDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.DB_REF_USERS).child(mFirebaseUser.getUid());

        mSaveStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setTitle("Updating Status");
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                String status = mTipStatus.getEditText().getText().toString();
                if(!TextUtils.isEmpty(status)){
                        mStatusDatabaseReference.child(Constants.DB_REF_USER_STATUS_KEY).setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                        mProgressDialog.dismiss();

                            }
                        });
                }
            }
        });

    }
}
