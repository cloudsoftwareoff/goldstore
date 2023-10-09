package com.cloudsoftware.goldstorex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class LastMessageActivity extends AppCompatActivity {
    private ArrayList<HashMap<String, Object>> userList = new ArrayList<>();
long timing=0;
long last=0;
    private RequestManager glideManager;
    private ArrayList<HashMap<String, Object>> chatList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_message);

        ListView chatView =findViewById(R.id.chat);
        TextView noresult=findViewById(R.id.no_result);
        ProgressBar loading = findViewById(R.id.loading);
        ImageView go_back=findViewById(R.id.go_back);

        noresult.setVisibility(View.GONE);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
      // get user list
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            final GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
            };
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot _snapshot : snapshot.getChildren()) {

                    HashMap<String, Object> item = _snapshot.getValue(genericTypeIndicator);

                  userList.add(item);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LastMessageActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        DatabaseReference LastRef = FirebaseDatabase.getInstance().getReference("last/");

        LastRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the chatList before populating it again
                chatList.clear();
                snapshot.getKey();


                for (DataSnapshot childSnapshot : snapshot.getChildren()) {

                    // Get the data from each child
                    HashMap<String, Object> chatData = (HashMap<String, Object>) childSnapshot.getValue();

                    timing=Long.parseLong(chatData.get("time").toString());
                    /*
                    tmin    20
                    last 100


                     */
                    if(timing>last){
                        last=timing;
                        chatList.add(0,chatData);

                    }else{
                        last=timing;
                        chatList.add(chatData);
                    }
                    chatList.sort(new Comparator<HashMap<String, Object>>() {
                        @Override
                        public int compare(HashMap<String, Object> stringObjectHashMap, HashMap<String, Object> t1) {
                            return 0;
                        }
                    });

                }

                loading.setVisibility(View.GONE);
                if(chatList.size() == 0){
                    noresult.setVisibility(View.VISIBLE);
                }else{
                    noresult.setVisibility(View.GONE);
                }

                // Do something with the populated chatList
                LastMessageActivity.ChatAdapter adapter = new LastMessageActivity.ChatAdapter(chatList);
                chatView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatView.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent=new Intent(this,ChatActivity.class);
            intent.putExtra("user",chatList.get(i).get("id").toString());
            intent.putExtra("target",chatList.get(i).get("id").toString());
            startActivity(intent);
        });

        go_back.setOnClickListener(view -> finish());








    }

    public class ChatAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> _chatList;
        ChatAdapter(ArrayList<HashMap<String, Object>> orderList){
            _chatList =orderList;

        }
        @Override
        public int getCount() {
            return  _chatList.size();
        }

        @Override
        public Object getItem(int i) {
            return _chatList.get(i);
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
                _view = _inflater.inflate(R.layout.lastest_message,null);
            }
            final ImageView sender_image = _view.findViewById(R.id.sender);
            final TextView message_value = _view.findViewById(R.id.message_value);
            final TextView time = _view.findViewById(R.id.time);
            final TextView sender_name = _view.findViewById(R.id.sender_name);





            // time
            final Date date = new Date(Long.parseLong(_chatList.get(i).get("time").toString()));
            final Date currentDate = new Date(); // Current date and time
            final long timeDifference = currentDate.getTime() - date.getTime(); // Time difference in milliseconds

            if (timeDifference < 60 * 60 * 1000) {
                // Less than 1 hour ago, hide the time
                time.setVisibility(View.GONE);
            } else if (timeDifference < 24 * 60 * 60 * 1000) {
                // Within the last day, show the time in HH:mm format
                final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                final String dateString = sdf.format(date);
                time.setText(dateString);
            } else {
                // Older than 1 day, show the date and time in yyyy/MM/dd HH:mm format
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                final String dateString = sdf.format(date);
                time.setText(dateString);
            }


            if(_chatList.get(i).containsKey("message")){
                message_value.setText(_chatList.get(i).get("message").toString());
            }else{
                message_value.setVisibility(View.GONE);
            }

            if(_chatList.get(i).get("r").toString().equals("0")){

                message_value.setTextColor(Color.parseColor("#000000"));


            }else{
                message_value.setTextColor(Color.parseColor("#66000000"));
            }

            // user info
            for (HashMap<String, Object> map : userList) {
                if (map.containsKey("id") && map.get("id").equals(_chatList.get(i).get("id").toString())) {
                    // Do something with the values at this key, for example:
                    String name = (String) map.get("name");
                    String image_url = (String) map.get("photo");

                    sender_name.setText(name);

                    glideManager = Glide.with(LastMessageActivity.this);
                    // Load image using Glide
                    glideManager.load(image_url)
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .into(sender_image);

                }
            if(map.get("id").equals(_chatList.get(i).get("sender").toString())){
                String name = (String) map.get("name");
                message_value.setText(name+": "+message_value.getText().toString());
            }


            }



            return _view;
        }

    }

}