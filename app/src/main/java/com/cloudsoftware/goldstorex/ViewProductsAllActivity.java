package com.cloudsoftware.goldstorex;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewProductsAllActivity extends AppCompatActivity {
    private ArrayList<HashMap<String, Object>> products = new ArrayList<>();
    private double equalizer=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_products_all);

        TextView title=findViewById(R.id.title);
        TextView no_result=findViewById(R.id.no_result);
        ImageView go_back=findViewById(R.id.go_back);
        ProgressBar progressBar=findViewById(R.id.progressbar);

        GridView product_list = findViewById(R.id.product);
        no_result.setVisibility(View.GONE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }





        // ----- get products -------
        DatabaseReference _products = FirebaseDatabase.getInstance().getReference("products");
        _products.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
                };

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    HashMap<String, Object> product = productSnapshot.getValue(genericTypeIndicator);


                        if(Double.parseDouble(productSnapshot.child("price").getValue().toString())>equalizer){
                            products.add(product);
                            equalizer=Double.parseDouble(productSnapshot.child("price").getValue().toString());
                        }else{
                            products.add(0,product);
                        }


                }
                ViewProductsAllActivity.ProductListAdapter adapter = new ViewProductsAllActivity.ProductListAdapter(products);
                product_list.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if(products.size()<1){
                    no_result.setVisibility(View.VISIBLE);
                }else{
                    no_result.setVisibility(View.GONE);
                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log.e(TAG, "Error getting products from Firebase", databaseError.toException());
                Toast.makeText(ViewProductsAllActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();

            }

        });
        // -- On Product clicked
        product_list.setOnItemClickListener((parent, view, position, id) -> {
            // send product values for less db usage
            Intent intent1 = new Intent(ViewProductsAllActivity.this, AddProductActivity    .class);
            if(!products.isEmpty()) {

                intent1.putExtra("id", products.get(position).get("id").toString());
                intent1.putExtra("master_id", products.get(position).get("master_product").toString());

                //  intent.putExtra("offer", products.get(position).get("offer").toString());
                startActivity(intent1);
            }
        });
        go_back.setOnClickListener(view -> finish());

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
                _view = _inflater.inflate(R.layout.view_product,null);
            }

            final TextView dollar= _view.findViewById(R.id.dollar);
            final TextView price= _view.findViewById(R.id.price_tn);


            final ImageView image = _view.findViewById(R.id.image);


            try{
                dollar.setText(_data.get(_position).get("sell").toString());
                final double price1= Double.parseDouble(products.get(_position).get("price").toString()) * Double.parseDouble(_data.get(_position).get("taux").toString()) ;
                final long roundedPrice = Math.round(price1);
                price.setText(roundedPrice +" TND");

                String imageUrl = _data.get(_position).get("image").toString();
                Glide.with(ViewProductsAllActivity.this)
                        .load(imageUrl)
                        .into(image);

            }catch (Exception e){
                Toast.makeText(ViewProductsAllActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

            }           return _view;
        }
    }


}