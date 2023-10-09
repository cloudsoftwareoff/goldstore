package com.cloudsoftware.goldstorex;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AuthActivity extends AppCompatActivity {
    private TextInputEditText email_edit,name_edit,pass_edit;
    private boolean login;
    private  TextInputLayout name_field;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);


        // Initialze
        TextView forget_password= findViewById(R.id.forget_password);

        TextView change_action= findViewById(R.id.change_action);
        name_field=findViewById(R.id.name_field);
        Button action_btn=findViewById(R.id.auth_action);
        email_edit=findViewById(R.id.email_edit);
        name_edit=findViewById(R.id.name_edit);
        pass_edit=findViewById(R.id.password_edit);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // change for auth state
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if(currentUser!=null){
            Intent intent= new Intent(AuthActivity.this,HomeActivity.class);
            startActivity(intent);
        }
        //  trigger change
        login=false;
        action_btn.setText("Sign Up");
        name_field.setVisibility(View.VISIBLE);
        forget_password.setVisibility(View.GONE);


        // authentication
        action_btn.setOnClickListener(view -> {
            action_btn.setEnabled(false);
            action_btn.setAlpha(0.5f);
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            //Get data
            String email = email_edit.getText().toString().trim();
            String name = name_edit.getText().toString().trim();
            String password = pass_edit.getText().toString().trim();

            if(email.equals("")){
                email_edit.setError(" ");
                action_btn.setEnabled(true);
                action_btn.setAlpha(1);

                return ;
            }
            // if action is sign up
            if(!login){
                if(name.equals("")){
                    name_edit.setError(" ");
                    action_btn.setEnabled(true);
                    action_btn.setAlpha(1);
                    return ;
                }
                if(name.length() < 6 || name.length() >20){
                    name_edit.setError("Name must be between 6 and 20 characters");
                    action_btn.setEnabled(true);
                    action_btn.setAlpha(1);
                    return ;

                }
            }


            if(password.equals("")) {
                pass_edit.setError(" ");
                action_btn.setEnabled(true);
                action_btn.setAlpha(1);
                return ;
            }


            // ----- Login Event -----
            if(login){
                ProgressDialog progressDialog = new ProgressDialog(AuthActivity.this);

                progressDialog.setMessage("Logging in...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // User is authenticated,

                                finish();
                                Intent intent = new Intent(AuthActivity.this, MainActivity.class);
                                startActivity(intent);

                            } else {
                                // Something went wrong
                                progressDialog.dismiss();
                                action_btn.setEnabled(true);
                                action_btn.setAlpha(1);

                                String errorMessage = task.getException().getMessage();
                                // Authentication failed, show an error message

                                AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
                                builder.setMessage(errorMessage)
                                        .setPositiveButton("OK", (dialog, id) -> {
                                            // Do something
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });

            }
            // ------- sign up ---------
            else{
                // Show the dialog
                ProgressDialog progressDialog = new ProgressDialog(AuthActivity.this);

                progressDialog.setMessage("Creating account...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(AuthActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Sign up success
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    String userId = user.getUid();

                                    // Get a reference to the Firebase Realtime Database
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference rootRef = database.getReference();

// Get a reference to new user

                                    DatabaseReference childRef = rootRef.child("users/"+userId);

// Create a HashMap to represent the user
                                    HashMap<String, Object> data = new HashMap<>();
                                    data.put("id",userId);
                                    data.put("name", name);
                                    long currentTimeMillis = System.currentTimeMillis();
                                    String currentTimeString = Long.toString(currentTimeMillis);
                                    data.put("chat_color","#2E3338");

                                    data.put("joined", currentTimeString);
                                    data.put("mail",user.getEmail());
                                    data.put("online", currentTimeMillis);
                                    data.put("verify","no");
                                    data.put("phone","");
                                    data.put("photo","https://i.ibb.co/9hVTZDN/user-2.png");

                                    childRef.setValue(data);
                                    finish();
//  ----- Sign up complete
                                    Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
                                    startActivity(intent);


                                } else {


                                    // user is not signed in
                                    Toast.makeText(AuthActivity.this, "Sign up failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }

                            } else {

                                //sign up fails
                                String errorMessage = task.getException().getMessage();
                                // Authentication failed, show an error message
                                progressDialog.dismiss();
                                action_btn.setEnabled(true);
                                action_btn.setAlpha(1);

                                AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
                                builder.setMessage(errorMessage)
                                        .setPositiveButton("OK", (dialog, id) -> {
                                            // Do something
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();

                            }
                        });
            }
    });

        // forget password
        forget_password.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AuthActivity.this);
            builder.setTitle("Enter your Email");

// Set up the input field
            final EditText input = new EditText(getApplicationContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

// Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                String new_email = input.getText().toString().trim();
                if(!new_email.equals("")){
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    firebaseAuth.sendPasswordResetEmail(new_email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Password reset email sent successfully
                                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show();


                                } else {
                                    Toast.makeText(this,  task.getException().getMessage()
                                            , Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

// Create and show the dialog
            AlertDialog dialog = builder.create();
           dialog.show();

        });

        // change UI to login/signup
        change_action.setOnClickListener(view -> {
            login=!login;
            if(login){
                action_btn.setText("Login");
                name_field.setVisibility(View.GONE);
                change_action.setText("Don't have an account? Sign up");
                forget_password.setVisibility(View.VISIBLE);

                pass_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }else{
                action_btn.setText("Sign Up");
                name_field.setVisibility(View.VISIBLE);
                forget_password.setVisibility(View.GONE);
                change_action.setText("Have an account? Login");

                pass_edit.setTransformationMethod(null);
            }
        });


    }
}