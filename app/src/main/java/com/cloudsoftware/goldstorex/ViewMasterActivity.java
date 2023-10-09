package com.cloudsoftware.goldstorex;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

public class ViewMasterActivity extends AppCompatActivity {
    private ArrayList<HashMap<String, Object>> products = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_view_master);
        ProgressBar loading = findViewById(R.id.progressbar);
        GridView product_list = findViewById(R.id.product);
        ImageView go_back=findViewById(R.id.go_back);
        TextView no_result=findViewById(R.id.no_result);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }






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


                        products.add(product);


                }
                ViewMasterActivity.ProductListAdapter adapter = new ViewMasterActivity.ProductListAdapter(products);
                product_list.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                loading.setVisibility(View.GONE);
            if(products.size()>0){
                no_result.setVisibility(View.GONE);
            }

                //  Toast.makeText(HomeActivity.this,"data loaded", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log.e(TAG, "Error getting products from Firebase", databaseError.toException());
                Toast.makeText(ViewMasterActivity.this, databaseError.toException().toString(), Toast.LENGTH_SHORT).show();

            }

        });
        // -- On Product clicked
        product_list.setOnItemClickListener((parent, view, position, id) -> {
            // send product values for less db usage
            Intent intent = new Intent(ViewMasterActivity.this, AddMasterProductActivity.class);
            if(!products.isEmpty()) {

                intent.putExtra("id", products.get(position).get("id").toString());

                startActivity(intent);
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
                _view = _inflater.inflate(R.layout.product,null);
            }

            final TextView name= _view.findViewById(R.id.name);

            final ImageView image = _view.findViewById(R.id.image);


            try{
                name.setText(_data.get(_position).get("label").toString());



                String imageUrl = _data.get(_position).get("icon").toString();
                Glide.with(ViewMasterActivity.this)
                        .load(imageUrl)
                        .into(image);

            }catch (Exception e){
                Toast.makeText(ViewMasterActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

            }           return _view;
        }
    }

}