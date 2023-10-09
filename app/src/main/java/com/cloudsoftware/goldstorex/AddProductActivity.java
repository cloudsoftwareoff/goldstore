package com.cloudsoftware.goldstorex;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {
    private static final String ALLOWED_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private ArrayList<HashMap<String,Object>> mydata = new ArrayList<>();
    private EditText name,price,desc,tax;
    double cast;
    private Button submit;
    // Define an instance variable for the launcher
    private ActivityResultLauncher<String> launcher;


    private boolean image_picked;
    private ArrayList<String> idList = new ArrayList<>();
    private ArrayList<String> nameList = new ArrayList<>();
    private String downloadUrl="",master_id,key,selected,MasterID,iso;
    private ImageView product_image;
    private Spinner category;
    private  boolean editmode;
    private int index,temp_index=0;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ProgressBar progressbar;
    private CheckBox active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        category=findViewById(R.id.category);
        name=findViewById(R.id.edit_name);
        price=findViewById(R.id.price);
        tax=findViewById(R.id.tax);

        desc=findViewById(R.id.desc);
        submit =findViewById(R.id.submit);
        TextView title= findViewById(R.id.title);
        progressbar = findViewById(R.id.progressbar);
        product_image=findViewById(R.id.product_image);
        ImageView go_back=findViewById(R.id.go_back);
        image_picked = false;
        selected="";
        master_id="0";
        index=0;
        active=findViewById(R.id.active);



        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }


        if (getIntent().hasExtra("id")) {
            if (getIntent().hasExtra("master_id")) {
                master_id=getIntent().getStringExtra("master_id");
            }
            // Start loading product data
            editmode=true;
            submit.setText("Update Product");
            MasterID = getIntent().getStringExtra("id");

            title.setText("Edit Product");

            DatabaseReference productdb = FirebaseDatabase.getInstance().getReference("products");
            if(!MasterID.equals("")) {
                productdb.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        try {
                            for (DataSnapshot Snapshot : snapshot.getChildren()) {
                                // Retrieve the user data from the snapshot
                                if (MasterID.equals(Snapshot.child("id").getValue(String.class))){
                                    String _name = Snapshot.child("sell").getValue(String.class);
                                    String _price = Snapshot.child("price").getValue(String.class);
                                    String _image = Snapshot.child("image").getValue(String.class);
                                    String _desc = Snapshot.child("desc").getValue(String.class);
                                    String _taux = Snapshot.child("taux").getValue(String.class);
                                   if(Snapshot.child("active").getValue(String.class).equals("yes")){
                                       active.setChecked(true);
                                   }else {
                                       active.setChecked(false);
                                   }

                                    iso=_image;
                                   image_picked=true;
                                    name.setText(_name);
                                    price.setText(_price);
                                    desc.setText(_desc);
                                    tax.setText(_taux);
                                    Glide.with(AddProductActivity.this)
                                            .load(_image)
                                            .into(product_image);


                                }




                            }
                        } catch (Exception e) {
                            Toast.makeText(AddProductActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle the error

                    }
                });

            }else{
                Toast.makeText(AddProductActivity.this, "No ID was passed", Toast.LENGTH_SHORT).show();


                finish();
            }

        } else {
            editmode=false;

        }

        //load Master Product
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference master_product = database.getReference("product");
        master_product.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    for (DataSnapshot catSnapshot : snapshot.getChildren()) {
                        String _id = catSnapshot.child("id").getValue(String.class);
                        idList.add(_id);
                        nameList.add(catSnapshot.child("label").getValue(String.class));

                        if (_id.equals(master_id)) {
                            temp_index = index;
                        }
                        index++;
                    }

                    category.setAdapter(new ArrayAdapter<String>(AddProductActivity.this, android.R.layout.simple_spinner_dropdown_item, nameList));
                    category.setSelection(temp_index);
                } catch (Exception e) {
                    Toast.makeText(AddProductActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error

            }
        });

        // on master product selected
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = idList.get(position);
                // Handle the selected item
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Update the button click listener to use the launcher

        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                // Handle the selected image URI here
                product_image.setImageURI(result);
                uploadImageToFirebaseStorage(result);
            }
        });

        product_image.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            launcher.launch("image/*");
        });

        //submit data

        submit.setOnClickListener(view -> {
                    if(name.getText().toString().length()<3){
                        name.setError("name too short");
                        return;
                    }
                    if(price.getText().toString().length()<1){
                        price.setError("Invalid price");
                        return;
                    }

                    if(tax.getText().toString().equals("")){
                        tax.setError("put taux value");
                        return;
                    }else{

                        try {
                            cast=Double.parseDouble(tax.getText().toString());

                        }catch (Exception e){
                            tax.setError(e.toString());
                        }
                    }

                   if(!image_picked){
                        if(editmode){
                            downloadUrl=iso;
                        }else{
                            Toast.makeText(AddProductActivity.this, "Upaload a product image", Toast.LENGTH_SHORT).show();

                            return;}
                    }
                    if(selected.equals("")){
                        Toast.makeText(AddProductActivity.this, "Select a category", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    // Get a reference to the Firebase Realtime Database
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference rootRef = db.getReference();
                    long now = System.currentTimeMillis();
                    String _now=String.valueOf(now);


                    key = _now+generate(9);
                    String finalPkey=key;
                    //String key= childRef.push().getKey();

                    HashMap<String, Object> data = new HashMap<>();

                    data.put("sell", name.getText().toString());
                    data.put("price", price.getText().toString());
                    data.put("master_product",selected);

                    data.put("taux",tax.getText().toString());


                    data.put("active","no");
                    if(active.isChecked()){
                        data.put("active","yes");
                    }
                    data.put("addedOn",_now);
                    data.put("desc",desc.getText().toString());

                    if(!downloadUrl.equals("")){
                        data.put("image",downloadUrl);
                    }
                    if(!editmode){
                        DatabaseReference childRef = rootRef.child("products").push();
                        key=childRef.getKey();
                        data.put("id",key);

                        childRef.setValue(data);
                        Toast.makeText(AddProductActivity.this, "Product added", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else{
                        // Update the node with the new data
                        data.remove("addedOn");
                        rootRef.child("products").child(MasterID).updateChildren(data)
                                .addOnSuccessListener(aVoid -> {
                                    // Update successful
                                    Toast.makeText(AddProductActivity.this, "Product Updated", Toast.LENGTH_SHORT).show();
                                    finish();

                                })
                                .addOnFailureListener(e -> {
                                    // Update failed
                                    Toast.makeText(AddProductActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
        //Glide.with(this).clear(product_image);
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child("product/" +generate(5)+ fileName);
        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask.addOnProgressListener(taskSnapshot -> {
                    // Get the progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    // Update the progress bar with the progress percentage
                    progressbar.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Toast.makeText(MainActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    // Get the download URL of the uploaded image
                    Task<Uri> downloadUrlTask = imageRef.getDownloadUrl();
                    downloadUrlTask.addOnSuccessListener(uri -> {
                        // Get the download URL of the uploaded image
                        image_picked=true;
                        downloadUrl = uri.toString();
                        Glide.with(AddProductActivity.this)
                                .load(downloadUrl)
                                .into(product_image);


                    });
                })
                .addOnFailureListener(e -> Toast.makeText(AddProductActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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