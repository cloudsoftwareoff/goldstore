package com.cloudsoftware.goldstorex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditSysActivity extends AppCompatActivity {
    TextInputEditText d17_number,email_edit,password_edit;
    private SharedPreferences sharedPreferences;
    private   SharedPreferences.Editor editor;
    boolean sudoFile=false,support=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sys);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        sharedPreferences = getSharedPreferences("audit", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ImageView go_back=findViewById(R.id.go_back);

        d17_number=findViewById(R.id.d17_edit);
        email_edit=findViewById(R.id.email_edit);
        password_edit=findViewById(R.id.password_edit);
        Button submit =findViewById(R.id.submit);


        if(sharedPreferences.contains("sys_email")){
            email_edit.setText(sharedPreferences.getString("sys_email",""));
        }
        if(sharedPreferences.contains("sys_password")){
            password_edit.setText(sharedPreferences.getString("sys_password",""));
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            // Logging for sudoers
            DatabaseReference sudoersRef = FirebaseDatabase.getInstance().getReference("default/sudo");

            sudoersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(currentUser.getUid())) {


                        // Sudo users found
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                            Object data = childSnapshot.getValue();

                        }
                    } else{
                        Toast.makeText(EditSysActivity.this, "not allowed", Toast.LENGTH_SHORT).show();

                        finish();

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors here
                }
            });

            // billing data
            DatabaseReference billing = FirebaseDatabase.getInstance().getReference("default/billing/D17");
            billing.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {



                    String phone = snapshot.child("phone").getValue(String.class);

                    d17_number.setText(phone);
                    editor.putString("phone", phone);

                    editor.apply();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            go_back.setOnClickListener(view -> finish());



            submit.setOnClickListener(view -> {
                if(d17_number.getText().toString().equals("")){
                    d17_number.setError("type number");
                    return;
                }
                if(email_edit.getText().toString().equals("")){
                    email_edit.setError("type valid email");
                    return;
                }
                if(password_edit.getText().toString().equals("")){
                    password_edit.setError("type the password");
                    return;
                }

                editor.putString("sys_email",email_edit.getText().toString());
                editor.putString("sys_password",password_edit.getText().toString());
                editor.apply();

                HashMap<String, Object> data = new HashMap<>();
                data.put("phone",d17_number.getText().toString());
                billing.updateChildren(data);
                Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show();
                finish();



            });



        } else {
            // User is not logged in
            finish();

        }


    }
}