package com.cloudsoftware.goldstorex;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class BillingActivity extends AppCompatActivity {
private String card,phone,holder;
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private boolean screenuploated;
private double taux;
private String downloadUrl,key;
private  ImageView imageScreenshot;
private    Button send_screen;
    String uid="",_id,price,master_name,require;
    Uri image_path;
    TextInputEditText account_data;

    private ActivityResultLauncher<String> launcher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
         send_screen=findViewById(R.id.send_screen);

         account_data=findViewById(R.id.target_account);
        TextView PhoneNumber=findViewById(R.id.PhoneNumber);
        LinearLayout screen_picker=findViewById(R.id.image_picker);
        TextView sell=findViewById(R.id.dollar);
        TextView price_tn=findViewById(R.id.price_tn);
        ImageView imageview=findViewById(R.id.image);
        ImageView go_back=findViewById(R.id.go_back);
         imageScreenshot=findViewById(R.id.imageScreenshot);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

         uid = currentUser.getUid();


        Intent intent=getIntent();
        String  selling=intent.getStringExtra("sell");
         _id=intent.getStringExtra("id");
        String  image=intent.getStringExtra("image");
          price=intent.getStringExtra("price");
    require=intent.getStringExtra("require");
         master_name=intent.getStringExtra("master_name");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();

        }

        screenuploated=false;
        send_screen.setAlpha(0.5f);
        send_screen.setEnabled(false);

        // get billing data
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        card = sharedPreferences.getString("card", "");
        taux = Double.parseDouble( sharedPreferences.getString("taux", "3.3"));
        phone = sharedPreferences.getString("phone", "");
        holder = sharedPreferences.getString("holder", "");



        PhoneNumber.setText("D17 Phone Number: "+phone);

        account_data.setHint("Type "+master_name+" "+require);

        sell.setText(selling);
        price_tn.setText(price+" TND");

        Glide.with(BillingActivity.this)
                .load(image)
                .into(imageview);

        // Update the button click listener to use the launcher

        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                // Handle the selected image URI here
                imageScreenshot.setImageURI(result);
                image_path=result;
                screenuploated=true;
                send_screen.setAlpha(1);
                send_screen.setEnabled(true);
               // uploadImageToFirebaseStorage(result);
            }
        });


        go_back.setOnClickListener(view -> finish());
        // submit data
        send_screen.setOnClickListener(view -> {
            if(screenuploated) {

                if(account_data.getText().toString().equals("")){
                    account_data.setError(require+" is required");
                    return;

                }
                else{
                    uploadImageToFirebaseStorage(image_path);
                }




            }else {
                Toast.makeText(BillingActivity.this, "Upload a screenshot first", Toast.LENGTH_SHORT).show();

            }

        });

        screen_picker.setOnClickListener(view -> {
            Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
            intent1.setType("image/*");
            launcher.launch("image/*");
        });

    }
        // upload screenshot
    private void uploadImageToFirebaseStorage(Uri imageUri) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child("screenshot/"+uid +"/"+generate(5)+ fileName);
        UploadTask uploadTask = imageRef.putFile(imageUri);
        // Show the dialog
        ProgressDialog progressDialog = new ProgressDialog(BillingActivity.this);

        progressDialog.setMessage("Uploading Image");
        progressDialog.setCancelable(false);
        progressDialog.show();


        uploadTask.addOnProgressListener(taskSnapshot -> {
                    // Get the progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                   // progressbar.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(BillingActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    // Get the download URL of the uploaded image
                    Task<Uri> downloadUrlTask = imageRef.getDownloadUrl();
                    downloadUrlTask.addOnSuccessListener(uri -> {

                        screenuploated=true;


                        downloadUrl = uri.toString();
                        key = generate(16);
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference rootRef = db.getReference();
                        long now = System.currentTimeMillis();
                        String _now = String.valueOf(now);
                        HashMap<String, Object> data = new HashMap<>();



                        data.put("product", _id);
                        data.put("state", "PENDING");
                        data.put("price_at_purchase",price);
                        data.put("target",account_data.getText().toString());
                        data.put("required",master_name+" "+require);
                        data.put("r","0");

                        data.put("screenshot", downloadUrl);
                        data.put("user", uid);
                        data.put("time", _now);

                        DatabaseReference childRef = rootRef.child("order").push(); // Generate a unique key
                        String keydb = childRef.getKey(); // Get the generated key

                        data.put("id", keydb);

                        childRef.setValue(data, (databaseError, databaseReference) -> {
                            progressDialog.dismiss();
                            if (databaseError != null) {
                                // There was an error while saving the data
                                AlertDialog.Builder builder = new AlertDialog.Builder(BillingActivity.this);
                                builder.setTitle("Failed to send order")
                                        .setMessage(databaseError.toString())
                                        .setPositiveButton("OK", (dialog, which) -> {

                                        })
                                        .show();
                            } else {
                                // Data saved successfully

                                Toast.makeText(BillingActivity.this, "Order sent", Toast.LENGTH_SHORT).show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(BillingActivity.this);
                                builder.setTitle("Order Sent")
                                        .setMessage("Dear valued customer,\nIf it has been more than three hours since you made your payment and you haven't received the product you purchased, please contact us immediately at Our customers support . We are here to assist you promptly.\n\nThank you for choosing our services.")
                                        .setPositiveButton("OK", (dialog, which) -> {
                                            Intent intent3 =new Intent(this,UserOrderActivity.class);
                                            startActivity(intent3);
                                            // go to order page
                                        })
                                        .show();

                            }
                        });









                    });
                })
                .addOnFailureListener(e -> Toast.makeText(BillingActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static String generate(int length) {
        Random random = new Random(System.currentTimeMillis());

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARACTERS.length());
            char randomChar = ALLOWED_CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }

}