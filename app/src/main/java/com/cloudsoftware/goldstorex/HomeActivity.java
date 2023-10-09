package com.cloudsoftware.goldstorex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {
private ArrayList<HashMap<String, Object>> products = new ArrayList<>();
    private  int randomNumber;
    private RequestManager glideManager;
    private SharedPreferences sharedPreferences;
    private   SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseApp.initializeApp(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        LinearLayout drawerLayout = findViewById(R.id._nav_view);
        DrawerLayout drawerLayout1 = findViewById(R.id._drawer);
        TextView user_name = drawerLayout.findViewById(R.id.name);
        ImageView pfp = drawerLayout.findViewById(R.id.avatar);
        TextView emailText = drawerLayout.findViewById(R.id.emailtext);
        LinearLayout linear_profile= drawerLayout.findViewById(R.id.linear_profile);
        LinearLayout linear_chat= drawerLayout.findViewById(R.id.linear_chat);
        LinearLayout linear_support= drawerLayout.findViewById(R.id.linearsupport);
        LinearLayout user_order= drawerLayout.findViewById(R.id.linear_order);
        LinearLayout settings= drawerLayout.findViewById(R.id.settings);

        GridView product_list = findViewById(R.id.product);
        ImageView menu = findViewById(R.id.menu);
        ImageView user_profile = findViewById(R.id.userprofile);
        TextView debug = findViewById(R.id.debug);
        TextView search = findViewById(R.id.search);
        LinearLayout linearLogout = findViewById(R.id.linearlogout);
        ProgressBar  loading = findViewById(R.id.loading);

        Random random = new Random();





        // Set a color filter on the image
        int color = Color.parseColor("#009688");
       // menu.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
      // linear_profile.setBackground(new GradientDrawable(GradientDrawable.Orientation.BR_TL, new int[] {0xFF009688,0x3878C6}));

        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0xFF00BCD4, 0xFF2196F3}
        );
        linear_profile.setBackground(gradientDrawable);

        // set up user data
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is authenticated
        // Get the UID of the authenticated user
        String uid = currentUser.getUid();
        String email = currentUser.getEmail();

        // Get a reference to the "users" node in the Firebase Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/"+uid);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the user data from the snapshot
                String name = snapshot.child("name").getValue(String.class);

                String photo = snapshot.child("photo").getValue(String.class);

                user_name.setText(name);
                emailText.setText(email);
                try {


                    // Load image with Glide
                    glideManager = Glide.with(HomeActivity.this);
                    // Load image using Glide
                    glideManager.load(photo)
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .into(pfp);
                    glideManager.load(photo)
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .into(user_profile);
                }catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
               // Toast.makeText(HomeActivity.this, error.toException().toString(), Toast.LENGTH_SHORT).show();


            }
        });

        // Get static data
        DatabaseReference billing = FirebaseDatabase.getInstance().getReference("default/billing/D17");
        billing.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String card = snapshot.child("Card").getValue(String.class);
                String taux = snapshot.child("taux").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String holder = snapshot.child("holder").getValue(String.class);
                 sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("card", card);
                editor.putString("taux", taux);
                editor.putString("phone", phone);
                editor.putString("holder", holder);
                editor.apply();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // ----- get products -------
        DatabaseReference _products = FirebaseDatabase.getInstance().getReference("product");
        _products.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
                };
            //    debug.setText(dataSnapshot.toString());

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    HashMap<String, Object> product = productSnapshot.getValue(genericTypeIndicator);
                    if(productSnapshot.child("active").getValue(String.class).equals("yes")) {

                        randomNumber = random.nextInt(2);
                        if (randomNumber == 0)
                            products.add(product);
                        else
                            products.add(0, product);
                    }

                }
                  ProductListAdapter adapter = new ProductListAdapter(products);
                product_list.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                loading.setVisibility(View.GONE);

               //  Toast.makeText(HomeActivity.this,"data loaded", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log.e(TAG, "Error getting products from Firebase", databaseError.toException());
               // Toast.makeText(HomeActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();

            }

        });


        // view orders
        user_order.setOnClickListener(view ->
        {
            Intent intent=new Intent(this,UserOrderActivity.class);
            startActivity(intent);
        });
        user_profile.setOnClickListener(view ->
        {
            Intent intent=new Intent(this,EditProfileActivity.class);
            startActivity(intent);
        });
        //Settings
        settings.setOnClickListener(view -> {
            Intent intent=new Intent(this,SettingsActivity.class);
            startActivity(intent);
        });
        // open and close drawer
        menu.setOnClickListener(v -> {
            // Check if the drawer is open
            if (drawerLayout1.isDrawerOpen(GravityCompat.START)) {
                // If the drawer is open, close it
                drawerLayout1.closeDrawer(GravityCompat.START);
            } else {
                // If the drawer is closed, open it
                drawerLayout1.openDrawer(GravityCompat.START);
            }
        });

        // --- get support ---
        linear_support.setOnClickListener(view -> {
            Intent intent=new Intent(this,ChatActivity.class);
            intent.putExtra("target","sys");
            intent.putExtra("user",uid);
            startActivity(intent);
        });

        // ---- logout ----
        linearLogout.setOnClickListener(v -> {
            // Create a new AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            builder.setMessage("Are you sure you want to log out?").setTitle("Log out");

            builder.setPositiveButton("OK", (dialog, id) -> {
                // Sign out user from Firebase Authentication
                FirebaseAuth.getInstance().signOut();
                finish();

                // Launch AuthActivity
                Intent intent = new Intent(HomeActivity.this, AuthActivity.class);
                startActivity(intent);
            });

            // Add Cancel button to the dialog
            builder.setNegativeButton("Cancel", (dialog, id) -> {

                dialog.dismiss();
            });

            // Show the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        });

        linear_chat.setOnClickListener(view -> {
            Intent intent=new Intent(this,AllChatActivity.class);
            startActivity(intent);

        });

        // -- On Product clicked
        product_list.setOnItemClickListener((parent, view, position, id) -> {
            // send product values for less db usage
            Intent intent = new Intent(HomeActivity.this, ViewProductActivity.class);
            if(!products.isEmpty()) {

                intent.putExtra("id", products.get(position).get("id").toString());
                intent.putExtra("name", products.get(position).get("label").toString());
                intent.putExtra("require", products.get(position).get("require").toString());

                //  intent.putExtra("offer", products.get(position).get("offer").toString());
                startActivity(intent);
            }
        });



    }

    // --------product BaseAdatper -------
    public class ProductListAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> _data;
        public ProductListAdapter(ArrayList<HashMap<String, Object>> _arr){
            _data = _arr;
        }
        @Override
        public int getCount(){
            return _data.size();
        }
        @Override
        public HashMap<String, Object> getItem(int _index){
            return _data.get(_index);
        }
        @Override
        public long getItemId(int _index){
            return _index;
        }

        @Override
        public View getView(final int _position, View _v, ViewGroup _container){
            LayoutInflater _inflater= getLayoutInflater();
            View _view=_v;
            if (_view == null){
                _view = _inflater.inflate(R.layout.product,null);
            }

            final TextView name= _view.findViewById(R.id.name);

            final ImageView image = _view.findViewById(R.id.image);


            try{
                name.setText(_data.get(_position).get("label").toString());

                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                gradientDrawable.setColors(new int[] { Color.parseColor("#EFEFEF"), Color.parseColor("#FFFFFF") });
                gradientDrawable.setCornerRadii(new float[] { 20, 20, 20, 20, 0, 0, 0, 0 });

                // bg.setBackground(gradientDrawable);

                String imageUrl = _data.get(_position).get("icon").toString();
                Glide.with(HomeActivity.this)
                        .load(imageUrl)
                        .into(image);

            }catch (Exception e){
                Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

            }           return _view;
        }
    }


}