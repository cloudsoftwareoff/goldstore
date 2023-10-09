package com.cloudsoftware.goldstorex;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private RequestManager glideManager;
    private ActivityResultLauncher<String> launcher;
    private ArrayList<HashMap<String, Object>> userList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> chatList = new ArrayList<>();
    Uri temp;
    boolean image_picked=false;
    private   ProgressBar progressBar;
    String uid,pfp_url,target_user,downloadUrl="",user_id;
    private DatabaseReference chatRef;
    private DatabaseReference LastRef;
    private LinearLayout linear_attach;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        ListView chatView =findViewById(R.id.chat);
        ImageView send=findViewById(R.id.send);
        ImageView go_back=findViewById(R.id.go_back);
        EditText messageEdit=findViewById(R.id.message);
        ImageView pick_image=findViewById(R.id.pick_image);
        ImageView attach_image=findViewById(R.id.attach_image);
         linear_attach=findViewById(R.id.linear_attach);
         progressBar=findViewById(R.id.progress);
         ImageView cancel=findViewById(R.id.cancel);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        progressBar.setVisibility(View.GONE);
        linear_attach.setVisibility(View.GONE);
        // Remove the divider
        chatView.setDivider(null);
        // Set the divider height to 0
        chatView.setDividerHeight(0);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid=currentUser.getUid();

        target_user=getIntent().getStringExtra("target");
         user_id =getIntent().getStringExtra("user");


        // get user list
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            final GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
            };
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot _snapshot : snapshot.getChildren()) {
                    if(_snapshot.child("id").getValue(String.class).equals(uid)){
                        pfp_url=_snapshot.child("photo").getValue(String.class);
                    }

                    HashMap<String, Object> item = _snapshot.getValue(genericTypeIndicator);
                    userList.add(item);

                }
            }
        @Override
            public void onCancelled(@NonNull DatabaseError error) {
            Toast.makeText(ChatActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

        }
        });

        // get chat
        chatRef = FirebaseDatabase.getInstance().getReference("chat/"+user_id);
         LastRef = FirebaseDatabase.getInstance().getReference("last/"+user_id);

        chatRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the chatList before populating it again
                chatList.clear();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    // Get the data from each child
                    HashMap<String, Object> chatData = (HashMap<String, Object>) childSnapshot.getValue();

                    chatList.add(chatData);
                }

                // Do something with the populated chatList
                ChatActivity.ChatAdapter adapter = new ChatActivity.ChatAdapter(chatList);
                chatView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                // Handle the selected image URI here
                attach_image.setImageURI(result);
                temp=result;
                image_picked=true;
                linear_attach.setVisibility(View.VISIBLE);

            }
        });

        //pick image
        pick_image.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            launcher.launch("image/*");
        });

        go_back.setOnClickListener(view -> finish());

        send.setOnClickListener(view -> {
            if(image_picked){
                uploadImageToFirebaseStorage(temp);
            }else if(!messageEdit.getText().toString().equals("")){
                HashMap<String, Object> data = new HashMap<>();

                data.put("sender", uid);
                data.put("receiver", target_user);
                long now = System.currentTimeMillis();
                String _now = String.valueOf(now);
                data.put("time", _now);
                data.put("message", messageEdit.getText().toString());
                data.put("r", "0");

                DatabaseReference childRef = chatRef.push(); // Generate a unique key
                String keydb = childRef.getKey(); // Get the generated key

                data.put("id", keydb);
                messageEdit.setText("");

                childRef.setValue(data);
                data.put("id", user_id);
                LastRef.setValue(data);

            }

        });

        cancel.setOnClickListener(view -> {
            linear_attach.setVisibility(View.GONE);
            image_picked=false;
        });
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
                _view = _inflater.inflate(R.layout.message,null);
            }
            final ImageView sender_image = _view.findViewById(R.id.sender);
            final TextView message_value = _view.findViewById(R.id.message_value);
            final TextView time = _view.findViewById(R.id.time);
            final TextView sender_name = _view.findViewById(R.id.sender_name);
            final ImageView attach=_view.findViewById(R.id.attachpic);
            final ImageView badge=_view.findViewById(R.id.badge);
            final ImageView verification=_view.findViewById(R.id.verification);
            final LinearLayout main_linear=_view.findViewById(R.id.linear1);
            final LinearLayout sender_linear=_view.findViewById(R.id.sender_linear);
            final LinearLayout content_linear =_view.findViewById(R.id.linear2);


            // make link clickable
            message_value.setClickable(true);
            android.text.util.Linkify.addLinks(message_value, android.text.util.Linkify.ALL);
            message_value.setLinkTextColor(Color.parseColor("#2196f3"));
            message_value.setLinksClickable(true);

            // sender background
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setColor(Color.parseColor("#00BCD4"));
            gd.setCornerRadius(20);

            // receveir background
            android.graphics.drawable.GradientDrawable megd = new android.graphics.drawable.GradientDrawable();
            megd.setColor(Color.parseColor("#e0e0e0"));
            megd.setCornerRadius(20);

            time.setVisibility(View.GONE);


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



            if(_chatList.get(i).containsKey("attach")){
                attach.setVisibility(View.VISIBLE);
                Glide.with(ChatActivity.this)
                        .load(_chatList.get(i).get("attach").toString())
                        .into(attach);
            }else{
                attach.setVisibility(View.GONE);
            }
            if(_chatList.get(i).containsKey("message")){
                message_value.setVisibility(View.VISIBLE);
                message_value.setText(_chatList.get(i).get("message").toString());
            }else{
                message_value.setVisibility(View.GONE);
            }

            if(uid.equals(_chatList.get(i).get("sender").toString())){
                content_linear.setBackground(gd);
                message_value.setTextColor(Color.parseColor("#FFFFFF"));
                main_linear.setGravity(Gravity.RIGHT);
                sender_linear.setVisibility(View.GONE);
                sender_name.setVisibility(View.GONE);
                badge.setVisibility(View.GONE);

            }else{
                main_linear.setGravity(Gravity.LEFT);
                content_linear.setBackground(megd);
                badge.setVisibility(View.VISIBLE);
            }

            sender_linear.setOnClickListener(view1 -> {
                time.setVisibility(View.VISIBLE);
            });

            // user info
            for (HashMap<String, Object> map : userList) {
                try {

                if (map.containsKey("id") && map.get("id").equals(_chatList.get(i).get("sender").toString())) {
                    // Do something with the values at this key, for example:
                    String name = (String) map.get("name");
                    String image_url = (String) map.get("photo");


                    if (map.containsKey("badge")) {
                        if (map.get("badge").toString().equals("dev")) {
                            badge.setVisibility(View.VISIBLE);
                            badge.setImageResource(R.drawable.dev_badge);
                        }else
                        if (map.get("badge").toString().equals("topadmin")) {
                            badge.setImageResource(R.drawable.protection);
                            badge.setVisibility(View.VISIBLE);
                        }else
                        if (map.get("badge").toString().equals("support")) {
                            badge.setImageResource(R.drawable.support_badge);
                            badge.setVisibility(View.VISIBLE);
                        }

                    }else{
                        badge.setVisibility(View.GONE);
                    }

                    if (map.containsKey("verify") && map.get("verify").toString().equals("yes")) {

                            verification.setVisibility(View.VISIBLE);

                    }else{
                        verification.setVisibility(View.GONE);
                    }


                    sender_name.setText(name);

                    glideManager = Glide.with(ChatActivity.this);
                    // Load image using Glide
                    glideManager.load(image_url)
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .into(sender_image);

                    }

                }catch (Exception e){
            }
            }

            return _view;
        }

    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = UUID.randomUUID().toString();
        StorageReference imageRef = storageRef.child("chat/" +uid);
        UploadTask uploadTask = imageRef.putFile(imageUri);
        progressBar.setVisibility(View.VISIBLE);

        uploadTask.addOnProgressListener(taskSnapshot -> {
                    // Get the progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    progressBar.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {

                    Task<Uri> downloadUrlTask = imageRef.getDownloadUrl();
                    downloadUrlTask.addOnSuccessListener(uri -> {

                        progressBar.setVisibility(View.GONE);

                        image_picked=false;
                        downloadUrl = uri.toString();

                        HashMap<String, Object> data = new HashMap<>();
                        data.put("sender",uid);
                        data.put("receiver",target_user);
                        long now = System.currentTimeMillis();
                        String _now = String.valueOf(now);
                        data.put("time",_now);
                       data.put("attach",downloadUrl);
                        data.put("r","0");

                        DatabaseReference childRef = chatRef.push(); // Generate a unique key
                        String keydb = childRef.getKey(); // Get the generated key

                        data.put("id", keydb);
                        childRef.setValue(data);
                        data.put("message",".sent a photo");
                        data.put("id", user_id);
                        LastRef.setValue(data);


                        Toast.makeText(ChatActivity.this, "Image sent", Toast.LENGTH_SHORT).show();
                        linear_attach.setVisibility(View.GONE);

                    });
                })
                .addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}