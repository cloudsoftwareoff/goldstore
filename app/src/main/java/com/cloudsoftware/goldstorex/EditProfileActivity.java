package com.cloudsoftware.goldstorex;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;
import yuku.ambilwarna.AmbilWarnaDialog;
public class EditProfileActivity extends AppCompatActivity implements OnUserEarnedRewardListener {
  private   ProgressBar progressbar;
  private   String uid,downloadUrl;
    private RequestManager glideManager;
  Uri temp;
   private ImageView profile;
    private ActivityResultLauncher<String> launcher;
    String photo,hex_color;
    boolean image_picked=false,picked_color=false;
    LinearLayout Progress_linear;
    private TextView progress_text;

    private InterstitialAd mInterstitialAd;
    TextInputEditText phone,name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

         Progress_linear = findViewById(R.id.ProgressLinear);
        LinearLayout select_image = findViewById(R.id.editpic);

         name = findViewById(R.id.name_edit);
         phone = findViewById(R.id.phone_edit);

         LinearLayout pick_color=findViewById(R.id.pick_color);

        ImageView go_back = findViewById(R.id.go_back);
         progress_text = findViewById(R.id.progresstext);
         profile = findViewById(R.id.user_image);
        Button submit = findViewById(R.id.submit);
        progressbar = findViewById(R.id.progressbar);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Progress_linear.setVisibility(View.GONE);
        // Check if the user is authenticated
        if (currentUser != null) {
            // Get the UID of the authenticated user
            uid = currentUser.getUid();

            // Get a reference to the "users" node in the Firebase Realtime Database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
            // Get a reference to the child node with the authenticated user's UID
            DatabaseReference currentUserRef = userRef.child(uid);

            currentUserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Retrieve the user data from the snapshot
                    String _name = snapshot.child("name").getValue(String.class);

                    if(snapshot.hasChild("chat_color")){
                        int color = Color.parseColor(snapshot.child("chat_color").getValue(String.class));
                        pick_color.setBackgroundColor(color);
                    }

                     photo = snapshot.child("photo").getValue(String.class);
                    String _phone = snapshot.child("phone").getValue(String.class);

                    // Do something with the user data
                    name.setText(_name);
                    phone.setText(_phone);

                    try {


                        glideManager = Glide.with(EditProfileActivity.this);
                        //image_picked=true;
                        // Load image using Glide
                        glideManager.load(photo)
                                .centerCrop()
                                .apply(RequestOptions.circleCropTransform())
                                .into(profile);

                    } catch (Exception e) {

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error

                }
            });
            launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
                if (result != null) {
                    // Check the file size of the selected image
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(result);
                        int fileSize = inputStream.available();
                        inputStream.close();

                        // Check if the file size is less than 5MB (5,000,000 bytes)
                        if (fileSize <= 5000000) {
                            // Handle the selected image URI here
                            profile.setImageURI(result);
                            image_picked = true;
                            temp = result;
                        } else {
                            // Display a message that the file is too large
                            Toast.makeText(this, "Selected image is too large (max 5MB)", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {}
            });
            AdRequest adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(this,"ca-app-pub-5494385415064868/2472012522", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                            // an ad is loaded.
                            mInterstitialAd = interstitialAd;

                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error

                            mInterstitialAd = null;
                        }
                    });
        }


        // pick chat color
        pick_color.setOnClickListener(view -> {
            AmbilWarnaDialog.OnAmbilWarnaListener colorPickerListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // Handle cancel action if needed
                }

                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {

                    hex_color=String.format("#%06X", (0xFFFFFF & color));
                 picked_color=true;
                    int xcolor = Color.parseColor(hex_color);
                    pick_color.setBackgroundColor(xcolor);
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(EditProfileActivity.this);
                    } else {

                    }
                }
            };

            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, Color.BLACK, true, colorPickerListener);
            colorPicker.show();
        });

        submit.setOnClickListener(view -> {
                    if(name.getText().toString().length()<6){
                        name.setError("name too short min 5 charaters");
                        return;
                    }else
                    if(phone.getText().toString().length() !=8 ){
                        phone.setError("Invalid Number");
                        return;
                    }else {

                        if(image_picked){
                            Progress_linear.setVisibility(View.VISIBLE);
                        uploadImageToFirebaseStorage(temp);}
                        else {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference rootRef = database.getReference();

                            String customKey = "users/"+uid;
                            DatabaseReference childRef = rootRef.child(customKey);

                            HashMap<String, Object> data = new HashMap<>();

                            data.put("name", name.getText().toString());

                            data.put("id",uid);
                            if(picked_color){
                                data.put("chat_color",hex_color);
                            }
                            data.put("phone",phone.getText().toString());

                            childRef.updateChildren(data);
                            Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }


                }
        );

        // pick image
        profile.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            launcher.launch("image/*");
            // Create an instance of BottomSheetDialogFragment
          /*  BottomSheetpfp bottomSheetDialogFragment = new BottomSheetpfp();


            bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());*/

        });

        go_back.setOnClickListener(view -> finish());
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child("userpfp/" +uid);
        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnProgressListener(taskSnapshot -> {
                    // Get the progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progress_text.setText(Math.round(progress) +"%");

                    progressbar.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {

                    Task<Uri> downloadUrlTask = imageRef.getDownloadUrl();
                    downloadUrlTask.addOnSuccessListener(uri -> {

                        Progress_linear.setVisibility(View.GONE);

                        image_picked=true;
                        downloadUrl = uri.toString();
                        // Get a reference to the Firebase Realtime Database
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference rootRef = database.getReference();

                        String customKey = "users/"+uid;
                        DatabaseReference childRef = rootRef.child(customKey);

                        HashMap<String, Object> data = new HashMap<>();

                        data.put("name", name.getText().toString());

                        data.put("id",uid);
                        data.put("phone",phone.getText().toString());
                        if(!downloadUrl.equals("")){
                            data.put("photo",downloadUrl);}
                        childRef.updateChildren(data);
                        Toast.makeText(EditProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                        finish();

                    });
                })
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

        // TODO: Reward the user!
    }
}