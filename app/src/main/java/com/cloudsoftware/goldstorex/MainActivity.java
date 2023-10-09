package com.cloudsoftware.goldstorex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
private   Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout copyright = findViewById(R.id.copyright);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference sys_state = FirebaseDatabase.getInstance().getReference("default/sys");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // refer to the developer
        copyright.setOnClickListener(view -> {

            String websiteUrl = "https://cloudsoftware.tn/";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));

            // Start the intent
            startActivity(intent);
        });

        // Checking Application state
        sys_state.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("state").getValue(String.class).equals("active")){

                    // check if current user is admin
                    if(currentUser!=null) {
                        DatabaseReference sysdb = FirebaseDatabase.getInstance().getReference("default/sudo");

                        sysdb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(currentUser.getUid())) {
                                    // Sudo users found


                                    finish();
                                    Intent intent = new Intent(MainActivity.this, SudoActivity.class);
                                    startActivity(intent);
                                } else{


                                    finish();
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle errors here
                               // Toast.makeText(MainActivity.this, databaseError.toString(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }else {
                         intent = new Intent(MainActivity.this, AuthActivity.class);
                        startActivity(intent);
                    }

                }
                else {
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage(snapshot.child("off_message").getValue(String.class))
                                .setPositiveButton("Exit", (dialog, which) -> {
                                    finishAffinity();
                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }catch (Exception e){

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(error.getMessage())
                        .setPositiveButton("Exit", (dialog, which) -> {
                            finishAffinity();
                        });

            }
        });

    }
}