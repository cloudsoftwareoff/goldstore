package com.cloudsoftware.goldstorex;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;
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

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
public class AllChatActivity extends AppCompatActivity {
    private RequestManager glideManager;
    private ActivityResultLauncher<String> launcher;
    private ArrayList<HashMap<String, Object>> userList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> memberList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> requestList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> modList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> chatList = new ArrayList<>();
    Uri temp;
    boolean image_picked=false;
    private ProgressBar progressBar;
    HashMap<String, Object> chatData,member;
    String uid,pfp_url,target_user,downloadUrl="",user_id;
    private DatabaseReference chatRef,allowed_users;

    private LinearLayout linear_attach;
    TextInputEditText message_value;
    private boolean resquest_=false,manageMember=false,toplevel=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_all_chat);


        DrawerLayout drawerLayout1 = findViewById(R.id._drawer);
        ListView normalMember=drawerLayout1.findViewById(R.id.list_member);
        ListView modMember=drawerLayout1.findViewById(R.id.list_mod);
        ListView RequestList=drawerLayout1.findViewById(R.id.list_request);
        LinearLayout request_linear=drawerLayout1.findViewById(R.id.linear_request);

        ImageView go_back=findViewById(R.id.go_back);
        ProgressBar check_allow=findViewById(R.id.loading);
        ImageView show_list=findViewById(R.id.show_list);
        ImageView send=findViewById(R.id.send);
        ListView chatView = findViewById(R.id.chat);
        message_value = findViewById(R.id.message);
        ImageView pick_image = findViewById(R.id.pick_image);
        ImageView attach_image = findViewById(R.id.attach_image);
        linear_attach = findViewById(R.id.linear_attach);
        progressBar = findViewById(R.id.progress);
        ImageView cancel = findViewById(R.id.cancel);

        Button Request_to_join = findViewById(R.id.request_btn);
        message_value.setVisibility(View.GONE);
        Request_to_join.setVisibility(View.GONE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        progressBar.setVisibility(View.GONE);
        linear_attach.setVisibility(View.GONE);
        pick_image.setVisibility(View.GONE);
        send.setVisibility(View.GONE);
        // Remove the divider
        chatView.setDivider(null);
        // Set the divider height to 0
        chatView.setDividerHeight(2);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

        request_linear.setVisibility(View.GONE);

        DatabaseReference sys_state = FirebaseDatabase.getInstance().getReference("default/sudo");
        sys_state.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentUser.getUid())) {
                    // Sudo users found

                    if(dataSnapshot.child(currentUser.getUid()).hasChild("toplevel")){
                        if(dataSnapshot.child(currentUser.getUid()).child("toplevel").getValue(String.class).equals("yes")){
                            toplevel=true;
                            request_linear.setVisibility(View.VISIBLE);
                        }

                    }
                    if(dataSnapshot.child(currentUser.getUid()).hasChild("manageMember")){
                        if(dataSnapshot.child(currentUser.getUid()).child("manageMember").getValue(String.class).equals("yes")){
                            manageMember=true;
                            request_linear.setVisibility(View.VISIBLE);
                        }

                    }


                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
            }
        });


        // get user list
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");

        userRef.addValueEventListener(new ValueEventListener() {
            final GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Object>>() {
            };

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot _snapshot : snapshot.getChildren()) {
                    if (_snapshot.child("id").getValue(String.class).equals(uid)) {
                        pfp_url = _snapshot.child("photo").getValue(String.class);
                    }

                    HashMap<String, Object> item = _snapshot.getValue(genericTypeIndicator);
                    userList.add(item);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllChatActivity.this, error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        chatRef = FirebaseDatabase.getInstance().getReference("main_chat/global/content");

        chatRef.orderByKey()
                .limitToLast(15) // Limit to the last 15 items
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear the chatList before populating it again
                        chatList.clear();

                        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                            // Get the data from each child
                            chatData = (HashMap<String, Object>) childSnapshot.getValue();
                            chatList.add(chatData);
                        }

                        // Do something with the populated chatList
                        AllChatActivity.ChatAdapter adapter = new AllChatActivity.ChatAdapter(chatList);
                        chatView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle onCancelled
                    }
                });



        // GET MEMBERS LIST
        allowed_users = FirebaseDatabase.getInstance().getReference("main_chat/global/members");
        allowed_users.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberList.clear();
                modList.clear();
                requestList.clear();


                for (DataSnapshot _snap : snapshot.getChildren()) {

                    if (_snap.child("allowed").getValue(String.class).equals("yes")) {
                        String role = _snap.child("role").getValue(String.class);

                        if (role.equals("mod")) {
                           member = (HashMap<String, Object>) _snap.getValue();
                            modList.add( member);
                        } else if (role.equals("member")) {
                            //member = (HashMap<String, Object>) _snap.getValue();
                            memberList.add( (HashMap<String, Object>) _snap.getValue());
                        }
                    } else if (_snap.child("allowed").getValue(String.class).equals("requested") && (toplevel || manageMember)) {
                        member = (HashMap<String, Object>) _snap.getValue();
                        requestList.add( member);
                    }
                    //  member.clear();
                }
                check_allow.setVisibility(View.GONE);

                if(snapshot.hasChild(uid)){
                    if(snapshot.child(uid).child("allowed").getValue(String.class).equals("yes")){

                        Request_to_join.setVisibility(View.GONE);
                        message_value.setVisibility(View.VISIBLE);
                        send.setVisibility(View.VISIBLE);
                        pick_image.setVisibility(View.VISIBLE);



                    }else{
                        if(snapshot.child(uid).child("allowed").getValue(String.class).equals("requested")){
                            Request_to_join.setText("Requested");
                            Request_to_join.setBackgroundColor(Color.GRAY);
                            resquest_=true;
                            Request_to_join.setEnabled(false);



                        }
                        else {
                            Request_to_join.setVisibility(View.VISIBLE);
                        }
                    }

                }else {
                    Request_to_join.setVisibility(View.VISIBLE);
                }
                AllChatActivity.MemberAdapter adapter1 = new AllChatActivity.MemberAdapter(memberList);
                AllChatActivity.MemberAdapter modAdapter = new AllChatActivity.MemberAdapter(modList);
                AllChatActivity.RequestJoinAdapter requestJoinAdapter = new AllChatActivity.RequestJoinAdapter(requestList);

                modMember.setAdapter(modAdapter);
                modAdapter.notifyDataSetChanged();

                RequestList.setAdapter(requestJoinAdapter);
                requestJoinAdapter.notifyDataSetChanged();

                normalMember.setAdapter(adapter1);
                adapter1.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        go_back.setOnClickListener(view -> finish());



        // change member state
        normalMember.setOnItemLongClickListener((parent, view, position, id) -> {
            // Handle the long click action here
            String member_uid=memberList.get(position).get("id").toString();
            if(manageMember || toplevel){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(new CharSequence[]{"Promote", "Ban"}, (dialog, which) -> {
                    if (which == 0) {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("role", "mod");
                        allowed_users.child(member_uid).updateChildren(data);
                    } else if (which == 1) {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("role", "blackListed");
                        allowed_users.child(member_uid).updateChildren(data);
                    }
                })
                        .create()
                        .show();

            }




            return true;
        });


        // change mods
        // change member state
        modMember.setOnItemLongClickListener((parent, view, position, id) -> {
            // Handle the long click action here
            String member_uid=modList.get(position).get("id").toString();
            if(toplevel){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(new CharSequence[]{"Demote", "Ban"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HashMap<String, Object> data = new HashMap<>();
                                if (which == 0) {

                                    data.put("role", "member");
                                    allowed_users.child(member_uid).updateChildren(data);
                                } else if (which == 1) {

                                    data.put("role", "blackListed");
                                    allowed_users.child(member_uid).updateChildren(data);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();

            }




            return true;
        });
        // show list
        show_list.setOnClickListener(view ->
        {
            if (drawerLayout1.isDrawerOpen(GravityCompat.END)) {
                // If the drawer is open, close it
                drawerLayout1.closeDrawer(GravityCompat.END);
            } else {
                // If the drawer is closed, open it
                drawerLayout1.openDrawer(GravityCompat.END);
            }

        });


        // SEND MESSAGE
        send.setOnClickListener(view -> {
            if(image_picked){
                uploadImageToFirebaseStorage(temp);
            }else if(!message_value.getText().toString().equals("")){
                HashMap<String, Object> data = new HashMap<>();

                data.put("sender", uid);
                long now = System.currentTimeMillis();
                String _now = String.valueOf(now);
                data.put("time", _now);

                data.put("message", message_value.getText().toString());
                data.put("r", "0");

                DatabaseReference childRef = chatRef.push(); // Generate a unique key
                String keydb = childRef.getKey(); // Get the generated key

                data.put("id", keydb);
                message_value.setText("");

                childRef.setValue(data);
                data.put("id", user_id);
                //chatRef.setValue(data);

            }

        });

        cancel.setOnClickListener(view -> {
            linear_attach.setVisibility(View.GONE);
            image_picked=false;
        });



        // Request to join
        Request_to_join.setOnClickListener(view -> {

            if(!resquest_){
                HashMap<String, Object> my_request = new HashMap<>();
                my_request.put("id",uid);
                long now_ = System.currentTimeMillis();
                String _now_ = String.valueOf(now_);
                my_request.put("lastActive",_now_);
                my_request.put("role","member");
                my_request.put("allowed","requested");
                allowed_users.child(uid).updateChildren(my_request);
                Toast.makeText(this, "Resquest sent", Toast.LENGTH_SHORT).show();

            }

        });


        launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                // Check the file size of the selected image
                try {
                    InputStream inputStream = getContentResolver().openInputStream(result);
                    int fileSize = inputStream.available();
                    inputStream.close();

                    // Check if the file size is less than 5MB (5,000,000 bytes)
                    if (fileSize <= 5000000) {
                        // Handle the selected image URI here
                        attach_image.setImageURI(result);
                        temp = result;
                        image_picked = true;
                        linear_attach.setVisibility(View.VISIBLE);
                    } else {
                        // Display a message that the file is too large
                        Toast.makeText(this, "Selected image is too large (max 5MB)", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //pick image
        pick_image.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            launcher.launch("image/*");
        });

        // Set long click listener for ListView items
        chatView.setOnItemLongClickListener((parent, view, position, id) -> {
           // String selectedItem = (String) parent.getItemAtPosition(position);
            showBottomSheet(position);
           // Toast.makeText(requireContext(), "Long clicked: " + selectedItem, Toast.LENGTH_SHORT).show();
            return true; // Return true to consume the long click event
        });


    }

    private void showBottomSheet(int i) {
        MyBottomSheetDialogFragment bottomSheetDialogFragment = new MyBottomSheetDialogFragment(chatList,i);
        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
    }

    public class MemberAdapter extends BaseAdapter
    {
        ArrayList<HashMap<String, Object>> _memberList;
        MemberAdapter(ArrayList<HashMap<String, Object>> List){
            _memberList =List;

        }
        @Override
        public int getCount() {
            return _memberList.size();
        }

        @Override
        public Object getItem(int i) {
            return _memberList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            LayoutInflater _inflater= getLayoutInflater();
            View _view=view;
            if (_view == null){
                _view = _inflater.inflate(R.layout.user,null);
            }
            final TextView username= _view.findViewById(R.id.usr_name);
            final ImageView avatar = _view.findViewById(R.id.avatar);
            final ImageView verification = _view.findViewById(R.id.verification);
            final ImageView badge = _view.findViewById(R.id.badge);


            // user info
            for (HashMap<String, Object> map : userList) {
                try {

                    if (map.containsKey("id") && map.get("id").equals(_memberList.get(i).get("id").toString())) {
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
                            verification.setImageResource(R.drawable.verify);

                        }else{
                            verification.setVisibility(View.GONE);
                        }


                        int randomColor = getRandomColor();

                        // Set the text color of the TextView
                        username.setTextColor(randomColor);
                        username.setText(name);

                        glideManager = Glide.with(AllChatActivity.this);
                        // Load image using Glide
                        glideManager.load(image_url)
                                .centerCrop()
                                .apply(RequestOptions.circleCropTransform())
                                .into(avatar);

                    }

                }catch (Exception e){
                }
            }



            return _view;
        }
    }
    public class RequestJoinAdapter extends BaseAdapter
    {
        ArrayList<HashMap<String, Object>> _memberList;
        RequestJoinAdapter(ArrayList<HashMap<String, Object>> List){
            _memberList =List;

        }
        @Override
        public int getCount() {
            return _memberList.size();
        }

        @Override
        public Object getItem(int i) {
            return _memberList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            LayoutInflater _inflater= getLayoutInflater();
            View _view=view;
            if (_view == null){
                _view = _inflater.inflate(R.layout.join_user_request,null);
            }
            final TextView username= _view.findViewById(R.id.usr_name);
            final ImageView avatar = _view.findViewById(R.id.avatar);
            final ImageView verification = _view.findViewById(R.id.verification);
            final ImageView badge = _view.findViewById(R.id.badge);
            final ImageView accept = _view.findViewById(R.id.accept);
            final ImageView refuse = _view.findViewById(R.id.refuse);


            // user info
            for (HashMap<String, Object> map : userList) {
                try {

                    if (map.containsKey("id") && map.get("id").equals(_memberList.get(i).get("id").toString())) {
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
                            verification.setImageResource(R.drawable.verify);

                        }else{
                            verification.setVisibility(View.GONE);
                        }


                        int randomColor = getRandomColor();

                        // Set the text color of the TextView
                        username.setTextColor(randomColor);
                        username.setText(name);

                        glideManager = Glide.with(AllChatActivity.this);
                        // Load image using Glide
                        glideManager.load(image_url)
                                .centerCrop()
                                .apply(RequestOptions.circleCropTransform())
                                .into(avatar);

                    }

                }catch (Exception e){
                }
            }

            accept.setOnClickListener(view1 -> {
                HashMap<String, Object> data = new HashMap<>();
                data.put("allowed", "yes");
                allowed_users.child(_memberList.get(i).get("id").toString()).updateChildren(data);
            });
            refuse.setOnClickListener(view1 -> {
                HashMap<String, Object> data = new HashMap<>();
                data.put("allowed", "refused");
                data.put("refusedby",uid);
                allowed_users.child(_memberList.get(i).get("id").toString()).updateChildren(data);
            });



            return _view;
        }
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
                _view = _inflater.inflate(R.layout.all_chat_xml,null);
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
            Linkify.addLinks(message_value, Linkify.ALL);
            message_value.setLinkTextColor(Color.parseColor("#2196f3"));
            message_value.setLinksClickable(true);


            //time.setVisibility(View.GONE);
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
                Glide.with(AllChatActivity.this)
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



                        if(map.containsKey("chat_color")){
                            int color = Color.parseColor(map.get("chat_color").toString());
                            sender_name.setTextColor(color);

                        }
                        else{
                            int color = Color.parseColor("#2E3338");
                            sender_name.setTextColor(color);
                        }

                        // Set the text color of the TextView

                        sender_name.setText(name);

                        glideManager = Glide.with(AllChatActivity.this);
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
        StorageReference imageRef = storageRef.child("main_chat/" +uid+"/"+fileName);
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



                        Toast.makeText(AllChatActivity.this, "Image sent", Toast.LENGTH_SHORT).show();
                        linear_attach.setVisibility(View.GONE);

                    });
                })
                .addOnFailureListener(e -> Toast.makeText(AllChatActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private int getRandomColor() {
        Random random = new Random();
        // Generate a random color with random red, green, and blue values
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return Color.rgb(r, g, b);
    }
}