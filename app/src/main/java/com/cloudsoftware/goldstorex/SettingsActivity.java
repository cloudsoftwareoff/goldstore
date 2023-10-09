package com.cloudsoftware.goldstorex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        TextView idtext= findViewById(R.id.text_id);
        TextView email= findViewById(R.id.emailtext);
        ImageView copy = findViewById(R.id.copyid);
        ImageView editmail = findViewById(R.id.editmail);
        ImageView editpass = findViewById(R.id.editpass);
        ImageView back = findViewById(R.id.go_back);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rootRef = database.getReference();

        if (currentUser != null) {

            idtext.setText(currentUser.getUid());
            email.setText(currentUser.getEmail());

            copy.setOnClickListener(view -> {

                // Get the clipboard system service
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(SettingsActivity.CLIPBOARD_SERVICE);

                // Create a new ClipData with the text to copy
                ClipData clipData = ClipData.newPlainText("Copied Text", currentUser.getUid());

                // Set the ClipData to the clipboard
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), "ID copied to clipboard", Toast.LENGTH_SHORT).show();
            });
            // change email
            editmail.setOnClickListener(view -> {
                // Create an AlertDialog Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Enter the New Email");

// Create an EditText field for user input
                final EditText inputField = new EditText(SettingsActivity.this);
                builder.setView(inputField);

// Set the positive button for OK
                builder.setPositiveButton("OK", (dialog, which) -> {
                    String userInput = inputField.getText().toString();
                    // Process the user input here
                    currentUser.updateEmail(userInput)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {

                                    DatabaseReference childRef = rootRef.child("users/"+currentUser.getEmail());

                                    HashMap<String, Object> data = new HashMap<>();
                                    data.put("mail",userInput);
                                    childRef.updateChildren(data);

                                    // Email updated successfully
                                    Toast.makeText(getApplicationContext(), "Email updated successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Failed to update email
                                    Toast.makeText(getApplicationContext(), "Failed to update email", Toast.LENGTH_SHORT).show();
                                }
                            });

                });

                builder.setNegativeButton("Cancel", (dialog, which) ->

                        dialog.cancel());

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            });
            back.setOnClickListener(view -> {
                finish();
            });

            editpass.setOnClickListener(view -> {
                FirebaseAuth auth = FirebaseAuth.getInstance();

                auth.sendPasswordResetEmail(currentUser.getEmail())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Password reset email sent successfully
                                    Toast.makeText(SettingsActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Failed to send password reset email
                                    Toast.makeText(SettingsActivity.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            });



















        }
    }
}