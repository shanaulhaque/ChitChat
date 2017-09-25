package com.sh.chitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sh.utils.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;
    private StorageReference mStorageRef;




    private CircleImageView imageView;
    private TextView mUsername;
    private TextView mStatus;
    private Button mChangeStatus;
    private Button mChangeImage;

    private ProgressDialog mProgressDialog;

    private static final int GALLERY_PIC = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imageView = (CircleImageView) findViewById(R.id.user_image);
        mUsername = (TextView) findViewById(R.id.username);
        mStatus = (TextView) findViewById(R.id.status);

        mChangeImage = (Button) findViewById(R.id.change_image_btn);
        mChangeStatus = (Button) findViewById(R.id.change_status_btn);
        mProgressDialog = new ProgressDialog(this);

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                String status = mStatus.getText().toString();
                statusIntent.putExtra(Constants.DB_REF_USER_STATUS_KEY,status);
                startActivity(statusIntent);
            }
        });

        mChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
//                Intent galleryIntent = new Intent();
//                galleryIntent.setType("image/*");
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PIC);
            }
        });

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        String uid = mUser.getUid();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.DB_REF_USERS).child(uid);
        mDatabaseReference.keepSynced(true);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child(Constants.DB_REF_USER_NAME_KEY).getValue().toString();
                    String status = dataSnapshot.child(Constants.DB_REF_USER_STATUS_KEY).getValue().toString();
                    final String image =  dataSnapshot.child(Constants.DB_REF_USER_IMAGE_KEY).getValue().toString();
                    if(!image.equals("default")) {

                        Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.default_image).into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_image).into(imageView);
                            }
                        });
                    }
                    mUsername.setText(name);
                    mStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Uri resultUri = result.getUri();
                String profilePicName = mUser.getUid();

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                                .setMaxHeight(200)
                                .setMaxWidth(200)
                                .setQuality(75)
                                .compressToBitmap(new File(resultUri.getPath()));
                } catch (IOException e) {

                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();
                final StorageReference thumb_filePath = mStorageRef.child(Constants.IMAGE_FOLDER_PROFILE_IMAGE).child(Constants.IMAGE_FOLDER_PROFILE_IMAGE_THUMB).child(profilePicName+".jpg");

                StorageReference filePath = mStorageRef.child(Constants.IMAGE_FOLDER_PROFILE_IMAGE).child(profilePicName+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()) {
                            final String imageUri = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()){
                                        String image_thumb = task.getResult().getDownloadUrl().toString();
                                        Map updateImage = new HashMap();
                                        updateImage.put(Constants.DB_REF_USER_IMAGE_KEY,imageUri);
                                        updateImage.put(Constants.DB_REF_USER_THUMB_IMAGE_KEY,image_thumb);
                                        mDatabaseReference.updateChildren(updateImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(SettingsActivity.this,"Uploaded Successfully",Toast.LENGTH_SHORT);
                                                    mProgressDialog.dismiss();
                                                }
                                            }
                                        });
                                    }else{
                                        Toast.makeText(SettingsActivity.this,"Error in uploading",Toast.LENGTH_SHORT);
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(SettingsActivity.this,"Error in uploading",Toast.LENGTH_SHORT);
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
