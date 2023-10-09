package com.cloudsoftware.goldstorex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SudoActivity extends AppCompatActivity {
    private ArrayList<HashMap<String, Object>> userList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> orderList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> productList = new ArrayList<>();
    private ArrayList<String> items = new ArrayList<>();
    private   Intent intent;

    SharedPreferences sharedPreferences ;
    private double income;
    private RequestManager glideManager;
    private ListView orderview;
    private boolean sudoFile=false,support=false,product_manage=false,toplevel=false;

    SharedPreferences.Editor editor;
    private   DatabaseReference query;
 private    TextView noresult,incometext;
   private ProgressBar loading;
    private  double taux=3.3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudo);

        sharedPreferences= getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        noresult=findViewById(R.id.no_result);
        loading=findViewById(R.id.loading);
        incometext=findViewById(R.id.income);

        LinearLayout drawerLayout = findViewById(R.id._nav_view);
        ImageView menu=findViewById(R.id.menu);

        DrawerLayout drawerLayout1 = findViewById(R.id._drawer);

        LinearLayout chat_linear=drawerLayout.findViewById(R.id.chat_linear);
        LinearLayout lineartop_admin=drawerLayout.findViewById(R.id.lineartop_admin);
        ImageView addMaster=drawerLayout.findViewById(R.id.add_master_product);
        ImageView addSubProduct=drawerLayout.findViewById(R.id.add_sub_product);
        LinearLayout logout=drawerLayout.findViewById(R.id.linearlogout);
        LinearLayout View_all_master=drawerLayout.findViewById(R.id.view_masterproduct);
        LinearLayout View_all_sub=drawerLayout.findViewById(R.id.view_subproduct);
        ImageView pfp = drawerLayout.findViewById(R.id.avatar);
        TextView user_name = drawerLayout.findViewById(R.id.name);
        LinearLayout user_manage=drawerLayout.findViewById(R.id.linear_user);

        user_manage.setVisibility(View.GONE);

        ImageView view_as_user=findViewById(R.id.stealth);

        Spinner view_type=findViewById(R.id.view_type);



        // Create a list of items
        List<String> itemList = new ArrayList<>();
        itemList.add("PENDING");
        itemList.add("DONE");
        itemList.add("DENIED");


        PushNotificationSender notify_order= new PushNotificationSender();

// Create an ArrayAdapter using the list of items and a default layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemList);

// Set the layout style for the dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Set the adapter for the Spinner
        view_type.setAdapter(adapter);


     orderview = findViewById(R.id.orders);

         query = FirebaseDatabase.getInstance().getReference("order");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            // Logging for sudoers
            DatabaseReference sudoersRef = FirebaseDatabase.getInstance().getReference("default/sudo");

            sudoersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(currentUser.getUid())) {
                        // Sudo users found
                        if(dataSnapshot.child(currentUser.getUid()).hasChild("sudo_file")){
                            if(dataSnapshot.child(currentUser.getUid()).child("sudo_file").getValue(String.class).equals("yes")){
                                sudoFile=true;
                            }

                        }
                        if(dataSnapshot.child(currentUser.getUid()).hasChild("toplevel")){
                            if(dataSnapshot.child(currentUser.getUid()).child("toplevel").getValue(String.class).equals("yes")){
                                toplevel=true;
                            }

                        }
                        if(dataSnapshot.child(currentUser.getUid()).hasChild("chat")){
                            if(dataSnapshot.child(currentUser.getUid()).child("chat").getValue(String.class).equals("yes")){
                                support=true;
                            }

                        }
                        if(dataSnapshot.child(currentUser.getUid()).hasChild("product")){
                            if(dataSnapshot.child(currentUser.getUid()).child("product").getValue(String.class).equals("yes")){
                                product_manage=true;
                            }

                        }
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                            Object data = childSnapshot.getValue();

                        }
                    } else{
                        Toast.makeText(SudoActivity.this, "not allowed", Toast.LENGTH_SHORT).show();

                        finish();

                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors here
                }
            });


        } else {
            // User is not logged in
            finish();

        }

        // Get static data
        DatabaseReference billing = FirebaseDatabase.getInstance().getReference("default/billing/D17");
        billing.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("Card")){
                    String card = snapshot.child("Card").getValue(String.class);
                    editor.putString("card", card);
                }

                String tauxString = snapshot.child("taux").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String holder = snapshot.child("holder").getValue(String.class);


                editor.putString("taux", tauxString);
                if(!tauxString.equals("")) {
                    taux = Double.parseDouble(tauxString);
                }
                editor.putString("phone", phone);
                editor.putString("holder", holder);
                editor.apply();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               // Toast.makeText(SudoActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference productsdb = FirebaseDatabase.getInstance().getReference("products");

        // Get user count
        userRef.addValueEventListener(new ValueEventListener() {
            final GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
            };
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot _snapshot : snapshot.getChildren()) {

                    if(_snapshot.child("id").getValue(String.class).equals(currentUser.getUid())){
                        String photo = _snapshot.child("photo").getValue(String.class);
                        user_name.setText(_snapshot.child("name").getValue(String.class));
                        glideManager = Glide.with(SudoActivity.this);
                        // Load image using Glide
                        glideManager.load(photo)
                                .centerCrop()
                                .apply(RequestOptions.circleCropTransform())
                                .into(pfp);
                    }

                    HashMap<String, Object> item = _snapshot.getValue(genericTypeIndicator);
                    userList.add(item);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
              //  Toast.makeText(SudoActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //get unread orders

        query.addValueEventListener(new ValueEventListener() {
            final GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
            };
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot _snapshot : snapshot.getChildren()) {

                    HashMap<String, Object> xitem = _snapshot.getValue(genericTypeIndicator);

                        if (xitem.containsKey("r") && xitem.get("r").equals("0")){

                            HashMap<String, Object> data = new HashMap<>();
                            data.put("r","1");
                            query.child(_snapshot.child("id").getValue(String.class)).updateChildren(data);
                            data.clear();



                            PushNotificationSender.sendLocalNotification(SudoActivity.this,"New Order",xitem.get("required").toString());
                        }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                Toast.makeText(SudoActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        });


        // get product count
        productsdb.addListenerForSingleValueEvent(new ValueEventListener() {
            final GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
            };
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot _snapshot : snapshot.getChildren()) {

                    HashMap<String, Object> item = _snapshot.getValue(genericTypeIndicator);
                    productList.add(item);
                  //product_count.setText("Products: " + String.valueOf(productList.size()));

                }}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                //Toast.makeText(SudoActivity.this, error.toString(), Toast.LENGTH_SHORT).show();


            }
        });

        getOrders("PENDING");

        addSubProduct.setOnClickListener(view -> {
            if(product_manage || toplevel) {
                Intent intent12 = new Intent(this, AddProductActivity.class);
                startActivity(intent12);
            }else{
                Toast.makeText(SudoActivity.this, "Missing Permission", Toast.LENGTH_SHORT).show();

            }
        });

        addMaster.setOnClickListener(view -> {
            if(product_manage || toplevel) {
                Intent intent12 = new Intent(this, AddMasterProductActivity.class);
                startActivity(intent12);
            }else{
                Toast.makeText(SudoActivity.this, "Missing Permission", Toast.LENGTH_SHORT).show();

            }
        });

        // view as user
        view_as_user.setOnClickListener(view -> {

            // Launch AuthActivity
             intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });

        // ---- logout ----
        logout.setOnClickListener(v -> {
            // Create a new AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out?").setTitle("Log out");

            builder.setPositiveButton("OK", (dialog, id) -> {
                // Sign out user from Firebase Authentication
                FirebaseAuth.getInstance().signOut();
                finish();

                // Launch AuthActivity
                intent = new Intent(this, AuthActivity.class);
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

        // view all sub products
        View_all_sub.setOnClickListener(view -> {
            Intent intent =new Intent(this,ViewProductsAllActivity.class);
            startActivity(intent);
        });

        // view all master products
        View_all_master.setOnClickListener(view -> {
            if(product_manage || toplevel) {
                Intent intent = new Intent(this, ViewMasterActivity.class);
                startActivity(intent);
            }else{
                Toast.makeText(SudoActivity.this, "Missing Permission", Toast.LENGTH_SHORT).show();

            }
        });

        chat_linear.setOnClickListener(view -> {
            if(support || toplevel) {
                Intent intent1 = new Intent(this, LastMessageActivity.class);
                startActivity(intent1);
            }else{
                Toast.makeText(SudoActivity.this, "Missing Permission", Toast.LENGTH_SHORT).show();


            }
        });

        lineartop_admin.setOnClickListener(view -> {
            if(sudoFile || toplevel){
                Intent intent1 = new Intent(this, EditSysActivity.class);
                startActivity(intent1);
            }else{
                Toast.makeText(SudoActivity.this, "Missing Permission", Toast.LENGTH_SHORT).show();

            }
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

        // change view type
        view_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getOrders(itemList.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



    }
    @Override
    protected void onResume() {
        super.onResume();

        // Refresh or update your view here
       getOrders("PENDING");
    }
    public void getOrders(String type){
        orderList.clear();
        noresult.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
         income=  0;


        // ---- get orders -----
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            final GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
            };

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot _snapshot : snapshot.getChildren()) {

                    HashMap<String, Object> item = _snapshot.getValue(genericTypeIndicator);
                    String state = _snapshot.child("state").getValue(String.class);
                    try{

                        if (item.containsKey("id")){
                            if (state.equals(type)) {
                                orderList.add(item);
                                income+=Double.parseDouble(Objects.requireNonNull(_snapshot.child("price_at_purchase").getValue(String.class)));
                                OrderAdapter adapter = new OrderAdapter(orderList);
                                orderview.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                // Toast.makeText(SudoActivity.this, " Size: " + String.valueOf(orderList.size()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }catch (Exception e){

                    }
                }
                loading.setVisibility(View.GONE);
                if (orderList.size()==0){
                    noresult.setVisibility(View.VISIBLE);
                }
                if(type.equals("DONE")){
                    incometext.setText("Revenue: "+ income +" TND");
                    incometext.setVisibility(View.VISIBLE);
                }else{
                    incometext.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                Toast.makeText(SudoActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    public class OrderAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> _orderList;
        OrderAdapter(ArrayList<HashMap<String, Object>> orderList){
            _orderList =orderList;

        }
        @Override
        public int getCount() {
            return  _orderList.size();
        }

        @Override
        public Object getItem(int i) {
            return _orderList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater _inflater= getLayoutInflater();
            View _view=view;
            if (_view == null){
                _view = _inflater.inflate(R.layout.order,null);
            }
           final TextView total_price = _view.findViewById(R.id.price);
           final TextView username = _view.findViewById(R.id.username);
            final TextView state_text = _view.findViewById(R.id.state_text);
            final ImageView state_image = _view.findViewById(R.id.state_image);

          final   LinearLayout main_linear=_view.findViewById(R.id.main_linear);



            TextView time = _view.findViewById(R.id.time);
            ImageView user_pfp = _view.findViewById(R.id.userpfp);

            ImageView call = _view.findViewById(R.id.call);
            ImageView product_image = _view.findViewById(R.id.product_image);
            TextView product_txt = _view.findViewById(R.id.product_txt);


            String id =orderList.get(i).get("user").toString();

            String product_id=_orderList.get(i).get("product").toString();

            String phoneNum="",product_name,pimage_url="";

            final Date date = new Date(Long.parseLong(_orderList.get(i).get("time").toString()));
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            final String dateString = sdf.format(date);
            String email="";
            time.setText(dateString);

            // user info
            for (HashMap<String, Object> map : userList) {
                if (map.containsKey("id") && map.get("id").equals(id)) {
                    // Do something with the values at this key, for example:
                    String name = (String) map.get("name");
                    phoneNum=(String) map.get("phone");
                    username.setText(name);
                    email=(String) map.get("mail");
                    String image_url = (String) map.get("photo");

                    // Load image with Glide
                    glideManager = Glide.with(SudoActivity.this);
                    // Load image using Glide
                    glideManager.load(image_url)
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .into(user_pfp);
                }}

            total_price.setText(orderList.get(i).get("price_at_purchase").toString()+" TND");

            // get product data
            for (HashMap<String, Object> map : productList) {
                if (map.containsKey("id") && map.get("id").equals(product_id)) {
                    // Do something with the values at this key, for example:
                 product_name= (String) map.get("sell");
                    product_txt.setText(product_name);
                    double dinar=Double.parseDouble(map.get("price").toString())*taux;
                    //total_price.setText(taux+" TND");
                   pimage_url = (String) map.get("image");
                    Glide.with(SudoActivity.this)
                            .load(pimage_url)
                            .into(product_image);
                }}


            final String fphoneNum=phoneNum;
            if(orderList.get(i).get("state").toString().equals("DONE")){
                state_text.setText("Done");
                state_image.setImageResource(R.drawable.verified);

            }else
            if( orderList.get(i).get("state").toString().equals("DENIED")){
                state_text.setText("Rejected:\n");
                HashMap<String, Object> hashMap = orderList.get(i);
                if (hashMap.containsKey("reason")) {
                    state_text.setText("Rejected:\n"+orderList.get(i).get("reason").toString());
                }
                state_image.setImageResource(R.drawable.declined);

            }

            // call user
            call.setOnClickListener(view12 -> {

                Intent intent3 = new Intent(Intent.ACTION_DIAL);
                intent3.setData(Uri.parse("tel:" +fphoneNum ));

                if (intent3.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent3);
                }
            });

            // view details
            String finalPimage_url = pimage_url;
            String finalEmail = email;
            main_linear.setOnClickListener(view1 -> {

                Intent intent =new Intent(SudoActivity.this,ViewOrderActivity.class);
                intent.putExtra("order_id",orderList.get(i).get("id").toString());
                intent.putExtra("product_name",product_txt.getText().toString());
                intent.putExtra("product_image", finalPimage_url);
                intent.putExtra("price",orderList.get(i).get("price_at_purchase").toString());
                intent.putExtra("user",orderList.get(i).get("user").toString());
                intent.putExtra("mail", finalEmail);
                intent.putExtra("screenshot",orderList.get(i).get("screenshot").toString());
                intent.putExtra("target",orderList.get(i).get("target").toString());
                intent.putExtra("required",orderList.get(i).get("required").toString());

                startActivity(intent);
            });
       return _view;
        }

    }

}