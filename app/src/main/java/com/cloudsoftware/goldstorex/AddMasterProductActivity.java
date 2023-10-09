package com.cloudsoftware.goldstorex;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class AddMasterProductActivity extends AppCompatActivity {
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
EditText name,require;
Button submit;
ProgressBar progressbar;
ImageView product_image;
private String downloadUrl,key,MasterID,iso;
private  boolean image_picked,editmode;
    private ActivityResultLauncher<String> launcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_master_product);

        name=findViewById(R.id.edit_name);
        require=findViewById(R.id.require);
        CheckBox active=findViewById(R.id.active);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        submit =findViewById(R.id.submit);
        TextView title= findViewById(R.id.title);
        progressbar = findViewById(R.id.progressbar);
        product_image=findViewById(R.id.product_image);
        ImageView go_back=findViewById(R.id.go_back);

        image_picked=false;
        editmode=false;

        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                // Handle the selected image URI here
                product_image.setImageURI(result);
                uploadImageToFirebaseStorage(result);
            }
        });


        if (getIntent().hasExtra("id")) {
            // Start loading product data
            editmode=true;
            submit.setText("Update Product");
            MasterID = getIntent().getStringExtra("id");

            title.setText("Edit Product");

            DatabaseReference productdb = FirebaseDatabase.getInstance().getReference("product");
            if(!MasterID.equals("")) {
                productdb.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {
                            for (DataSnapshot Snapshot : snapshot.getChildren()) {
                                // Retrieve the user data from the snapshot
                                if (MasterID.equals(Snapshot.child("id").getValue(String.class))){
                                    String _name = Snapshot.child("label").getValue(String.class);
                                    String _require = Snapshot.child("require").getValue(String.class);
                                    String _image = Snapshot.child("icon").getValue(String.class);
                                    if(Snapshot.child("active").getValue(String.class).equals("yes")){
                                        active.setChecked(true);
                                    }else{
                                        active.setChecked(false);
                                    }
                                     iso=_image;
                                    downloadUrl=_image;
                                    image_picked=true;
                                    name.setText(_name);
                                    require.setText(_require);

                                    try {
                                        Glide.with(AddMasterProductActivity.this)
                                                .load(_image)
                                                .into(product_image);
                                    }catch (Exception e){

                                    }

                                }




                            }
                        } catch (Exception e) {
                            Toast.makeText(AddMasterProductActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error

                    }
                });

            }else{
                Toast.makeText(AddMasterProductActivity.this, "No ID was passed", Toast.LENGTH_SHORT).show();


                finish();
            }

        } else {
            editmode=false;
           // Toast.makeText(AddMasterProductActivity.this, "not edit mode", Toast.LENGTH_SHORT).show();


        }

        // pick image
        product_image.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            launcher.launch("image/*");
        });
        // Submit data
        submit.setOnClickListener(view -> {
                    if(name.getText().toString().length()<3){
                        name.setError("name too short");
                        return;
                    }
                    if(require.getText().toString().equals("")){
                        require.setError("put a requirement");
                        return;
                    }
                    if(!image_picked){
                        if(editmode){

                            downloadUrl=iso;
                        }else {
                            Toast.makeText(AddMasterProductActivity.this, "Pick an icon", Toast.LENGTH_SHORT).show();

                            return;
                        }
                    }




                    // Get a reference to the Firebase Realtime Database
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference rootRef = db.getReference();
                    long now = System.currentTimeMillis();
                    String _now=String.valueOf(now);

                    HashMap<String, Object> data = new HashMap<>();

                    data.put("label", name.getText().toString());
                    data.put("active","no");
                    if(active.isChecked()){
                        data.put("active","yes");
                    }

                    data.put("addedOn",_now);

                    data.put("require",require.getText().toString());
                    if(!downloadUrl.equals("")){
                        data.put("icon",downloadUrl);}
                    if(!editmode){
                        DatabaseReference childRef = rootRef.child("product").push();
                        key=childRef.getKey();
                        data.put("id",key);
                        childRef.setValue(data);
                        Toast.makeText(AddMasterProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();
                        finish();}
                    else{
                        // Update the node with the new data
                        data.remove("addedOn");

                        rootRef.child("product").child(MasterID).updateChildren(data)
                                .addOnSuccessListener(aVoid -> {
                                    // Update successful
                                    Toast.makeText(AddMasterProductActivity.this, "Product Updated", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    // Update failed
                                    Toast.makeText(AddMasterProductActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                });
                    }

                }
        );


        go_back.setOnClickListener(view -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear Glide resources and cancel any pending requests
       // Glide.with(this).pauseRequests();
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child("product/" +generate(5)+ fileName);
        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnProgressListener(taskSnapshot -> {
                    // Get the progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    progressbar.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {

                    Task<Uri> downloadUrlTask = imageRef.getDownloadUrl();
                    downloadUrlTask.addOnSuccessListener(uri -> {

                        image_picked=true;
                        downloadUrl = uri.toString();
                        Glide.with(AddMasterProductActivity.this)
                                .load(downloadUrl)
                                .into(product_image);


                    });
                })
                .addOnFailureListener(e -> Toast.makeText(AddMasterProductActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    public static String generate(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }
}