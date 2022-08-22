package com.example.friendster;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class UserProfileActivity extends AppCompatActivity {


    private static final int PICK_IMAGE = 2;
    Uri resultUri;
    private ImageButton ibUserImage;
    private TextView tvUserName, tvUserEmail;
    private Button bnPostUserProfile;
    private ProgressDialog mProgressDialog;


    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser firebaseUser;
    private StorageReference mStorageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Intent intent = getIntent();
        String feed_uid = intent.getStringExtra("feed_uid");

        ibUserImage = (ImageButton) findViewById(R.id.xibUserImage);
        tvUserName = (TextView) findViewById(R.id.xtvUserProfileName);
        tvUserEmail = (TextView) findViewById(R.id.xtvUserEmail);
        bnPostUserProfile = (Button) findViewById(R.id.xbnPostUserProfile);
        mProgressDialog = new ProgressDialog(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef.keepSynced(true);

        ibUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickImageIntent.setType("image/*");
                startActivityForResult(pickImageIntent, PICK_IMAGE);
            }
        });

        bnPostUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultUri != null) {

                    mProgressDialog.setMessage("Updating your profile...");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    StorageReference mProfileImageRef = mStorageRef.child("profile_images").child(firebaseUser.getUid());
                    mProfileImageRef.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(UserProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                            final Uri[] downloadUrl = new Uri[1];
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            downloadUrl[0] =uri;
                                            //String imageUrl = uri.toString();
                                            //createNewPost(imageUrl);
                                            mDatabaseRef.child("profile_image").setValue(downloadUrl[0].toString());

                                            mProgressDialog.dismiss();
                                        }
                                    });
                                }
                            }
                            //Uri downloadUrl = Uri.parse(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());

                        }
                    });
                }
            }
        });

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String imageUrl = dataSnapshot.child("profile_image").getValue(String.class);

                tvUserName.setText(name);
                tvUserEmail.setText(email);
                if (imageUrl != null) {
                    Glide.with(UserProfileActivity.this)
                            .load(imageUrl)
                            .asBitmap()
                            .centerCrop()
                            .into(ibUserImage);

                } else {
                    Glide.with(UserProfileActivity.this).load(R.mipmap.empty_user).into(ibUserImage);
                }
            }

            @Override

            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                Glide.with(UserProfileActivity.this).load(resultUri).into(ibUserImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}