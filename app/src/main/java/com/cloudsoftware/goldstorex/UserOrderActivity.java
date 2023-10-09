package com.cloudsoftware.goldstorex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

public class UserOrderActivity extends AppCompatActivity {
    private ArrayList<HashMap<String, Object>> orderList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> productList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    String uid;
    SharedPreferences.Editor editor ;
    private  double taux=3.3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_order);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        ListView orderview = findViewById(R.id.orders);

        ImageView go_back=findViewById(R.id.go_back);
        ProgressBar loading=findViewById(R.id.loading);
        TextView no_result=findViewById(R.id.no_result);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        no_result.setVisibility(View.GONE);
        uid=currentUser.getUid();
        // get product
        DatabaseReference productsdb = FirebaseDatabase.getInstance().getReference("products");
        productsdb.addListenerForSingleValueEvent(new ValueEventListener() {
            final GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
            };
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot _snapshot : snapshot.getChildren()) {



                    HashMap<String, Object> item = _snapshot.getValue(genericTypeIndicator);
                    productList.add(item);


                }}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error

            }
        });

        DatabaseReference query = FirebaseDatabase.getInstance().getReference("order");

        // ---- get orders -----
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            final GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
            };
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot _snapshot : snapshot.getChildren()) {

                    HashMap<String, Object> item = _snapshot.getValue(genericTypeIndicator);
                    String state = _snapshot.child("state").getValue(String.class);
                    String user = _snapshot.child("user").getValue(String.class);
                    try{

                        if (user.equals(uid)){
                                if (!state.equals("PENDIND")) {

                               orderList.add(0,item);
                                // Toast.makeText(SudoActivity.this, " Size: " + String.valueOf(orderList.size()), Toast.LENGTH_SHORT).show();
                            } else{
                                    orderList.add(item);
                                }
                            OrderAdapter adapter = new OrderAdapter(orderList);
                            orderview.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                        loading.setVisibility(View.GONE);
                        if(orderList.size()>0){

                            no_result.setVisibility(View.GONE);
                        }else{
                            no_result.setVisibility(View.VISIBLE);
                        }

                    }
                    catch (Exception e){
                       Toast.makeText(UserOrderActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error

            }
        });


        go_back.setOnClickListener(view -> finish());

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
            final LinearLayout main_linear=_view.findViewById(R.id.main_linear);
            final LinearLayout user_linear=_view.findViewById(R.id.user_linear);
            final TextView state_text = _view.findViewById(R.id.state_text);
            final ImageView state_image = _view.findViewById(R.id.state_image);


            user_linear.setVisibility(View.GONE);



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
            time.setText(dateString);
            if(orderList.get(i).get("state").toString().equals("DONE")){
                state_text.setText("Done");
                state_image.setImageResource(R.drawable.verified);

            }else
            if( orderList.get(i).get("state").toString().equals("DENIED")){
                state_text.setText("Rejected");
                HashMap<String, Object> hashMap = orderList.get(i);
                if (hashMap.containsKey("reason")) {
                    state_text.setText("Rejected:\n"+orderList.get(i).get("reason").toString());
                }
                state_image.setImageResource(R.drawable.declined);

            }

            //notifyDataSetChanged();
            total_price.setText(_orderList.get(i).get("price_at_purchase").toString()+" TND");

// get product data
            for (HashMap<String, Object> map : productList) {
                if (map.containsKey("id") && map.get("id").equals(product_id)) {
                    // Do something with the values at this key, for example:
                    product_name= (String) map.get("sell");
                    product_txt.setText(product_name);
                    double dinar=Double.parseDouble(map.get("price").toString())*taux;
                   // total_price.setText(taux+" TND");
                    pimage_url = (String) map.get("image");
                    Glide.with(UserOrderActivity.this)
                            .load(pimage_url)
                            .into(product_image);
                }}


            final String fphoneNum=phoneNum;

            // call user
            call.setOnClickListener(view12 -> {

                Intent intent3 = new Intent(Intent.ACTION_DIAL);
                intent3.setData(Uri.parse("tel:" +fphoneNum ));

                if (intent3.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent3);
                }
            });

            // view details


            return _view;
        }

    }

}