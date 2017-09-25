package com.sh.chitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sh.utils.Constants;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static String TAG = RegisterActivity.class.getSimpleName();
    private TextInputLayout mEmail;
    private TextInputLayout mName;
    private TextInputLayout mPassword;
    private Button mCreateBtn;
    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mProgressDialog = new ProgressDialog(this);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mName = (TextInputLayout) findViewById(R.id.login_user_name);
        mPassword = (TextInputLayout) findViewById(R.id.login_password);
        mCreateBtn = (Button) findViewById(R.id.reg_btn);


        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String displayName = mName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password) || !TextUtils.isEmpty(displayName) ){
                    mProgressDialog.setTitle("Registering User");
                    mProgressDialog.setMessage("Please wait...");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    registerUser(displayName,email,password);
                }



            }
        });


    }

    private void registerUser(final String displayName, String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            mProgressDialog.hide();
                            Toast.makeText(RegisterActivity.this, "Cannot Sign please check form",
                                    Toast.LENGTH_SHORT).show();
                        }else{

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = user.getUid();
                            database = FirebaseDatabase.getInstance();
                            DatabaseReference userRef = database.getReference().child("Users").child(uid);


                            HashMap<String, String> userMap = new HashMap<String, String>();
                            userMap.put(Constants.DB_REF_USER_NAME_KEY,displayName);
                            userMap.put(Constants.DB_REF_USER_STATUS_KEY,"Hi there, I am using ChitChat App.");
                            userMap.put(Constants.DB_REF_USER_IMAGE_KEY,"default");
                            userMap.put(Constants.DB_REF_USER_THUMB_IMAGE_KEY,"default");
                            String tokenId = FirebaseInstanceId.getInstance().getToken();
                            userMap.put(Constants.DB_REF_USER_DEVICE_TOKEN_KEY,tokenId);
                            userRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mProgressDialog.dismiss();
                                        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                });
    }

}
