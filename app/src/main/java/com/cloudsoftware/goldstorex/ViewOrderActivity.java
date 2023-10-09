package com.cloudsoftware.goldstorex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ViewOrderActivity extends AppCompatActivity {
    private RequestManager glideManager;
    boolean hasdata;
    String sys_email,sys_password,required;
    private String Screenshot,product_name,product_image,price_tnd,user,account,mailing;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order);

        Button submit=findViewById(R.id.submit);
        Button reject=findViewById(R.id.reject);

        ImageView screenshot_image=findViewById(R.id.screen);
        ImageView p_image=findViewById(R.id.product_image);
        TextView p_name=findViewById(R.id.product_name);
        TextView price_text=findViewById(R.id.price_tn);
        ImageView go_back=findViewById(R.id.go_back);
        ImageView copy=findViewById(R.id.copy_edit);
        TextView user_name=findViewById(R.id.username);
        TextView account_data=findViewById(R.id.account_detail);
        ImageView user_pfp=findViewById(R.id.userpfp);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        sharedPreferences = getSharedPreferences("audit", Context.MODE_PRIVATE);

        if(sharedPreferences.contains("sys_email") && sharedPreferences.contains("sys_password")){
            hasdata=true;
            sys_email=sharedPreferences.getString("sys_email","");
            sys_password=sharedPreferences.getString("sys_password","");
        }else{
            hasdata=false;
        }



        Intent intent=getIntent();

        Screenshot=intent.getStringExtra("screenshot");
        mailing=intent.getStringExtra("mail");

        required=intent.getStringExtra("required");
        product_name=intent.getStringExtra("product_name");
        String order_id=intent.getStringExtra("order_id");
        product_image=intent.getStringExtra("product_image");
        user=intent.getStringExtra("user");
        price_tnd=intent.getStringExtra("price");
        account=intent.getStringExtra("target");

        account_data.setText(account);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/"+user);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the user data from the snapshot
                String name = snapshot.child("name").getValue(String.class);
                //  String email = snapshot.child("email").getValue(String.class);
                String photo = snapshot.child("photo").getValue(String.class);

               // Toast.makeText(HomeActivity.this,"name loaded", Toast.LENGTH_SHORT).show();

                user_name.setText(name);

                try {


                    // Load image with Glide
                    glideManager = Glide.with(ViewOrderActivity.this);

                    // Load image using Glide
                    glideManager.load(photo)
                            .centerCrop()
                            .transform(new RoundedCorners(20))
                            .into(user_pfp);
                }catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                Toast.makeText(ViewOrderActivity.this, error.toException().toString(), Toast.LENGTH_SHORT).show();


            }
        });


        Glide.with(this)
                .load(Screenshot)
                .into(screenshot_image);

        Glide.with(this)
                .load(product_image)
                .into(p_image);

        price_text.setText(price_tnd+" TND");
        p_name.setText(product_name);

        go_back.setOnClickListener(view -> finish());

        copy.setOnClickListener(view -> {
            // Get the clipboard system service
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(SettingsActivity.CLIPBOARD_SERVICE);

            // Create a new ClipData with the text to copy
            ClipData clipData = ClipData.newPlainText("Copied Text",account_data.getText().toString());

            // Set the ClipData to the clipboard
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(this, "copied to clipboard", Toast.LENGTH_SHORT).show();

        });






        // submit data *
        submit.setOnClickListener(view -> {
            if(!hasdata){
                Toast.makeText(this, "Data missing", Toast.LENGTH_SHORT).show();
                return;

            }
            String recipientEmail = mailing;
            String subject = "Order Confirmation";
            String productName = product_name;
            String orderNumber = order_id;
            String purchasePrice = price_tnd+" TND";
            String deliveryEmail =account;

            String body = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; " +
                    "   background: linear-gradient(to bottom, #f1f1f1, #d8d8d8);}" +

                    "h1 { color: #333333; }" +
                    "p { color: #555555; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<h1>Order Confirmation</h1>" +
                    "<p>Dear Customer,</p>" +
                    "<p>Thank you for your purchase. We have confirmed that you have successfully sent the payment.</p>" +
                    "<h2>Order Details</h2>" +
                    "<p>Order Number:<b> " + orderNumber + "</b></p>" +
                    "<p>Product: <b>" + productName + "</b></p>" +
                    "<p>Price: <b>" + purchasePrice + "</b></p>" +
                    "<p>Please check your email for the product delivery information.</p>" +
                    "<h2>Product Delivery</h2>" +
                    "<p>The product will be delivered to the "+required+" associated with your order:</p>" +
                    "<p>"+required+": <b>" + deliveryEmail + "</b></p>" +
                    "<p>If you have any questions or need further assistance, please send us message" +
                    " in the customer support in our app.</p>" +
                    "<p>Thank you for shopping with us!</p>" +
                    "<p>Best regards,</p>" +
                    "<p>The Gold Store Team</p>" +
                    "</body>" +
                    "</html>";


            HashMap<String, Object> data = new HashMap<>();
            data.put("state","DONE");
            DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("order/"+order_id);

           sendEmail(mailing,subject,body,sys_email,sys_password);
            orderRef.updateChildren(data);
          Toast.makeText(this, "Oder Confirmed", Toast.LENGTH_SHORT).show();

            finish();

        });

        reject.setOnClickListener(view -> {
            if(!hasdata){
                Toast.makeText(this, "Data missing", Toast.LENGTH_SHORT).show();
                return;

            }
            HashMap<String, Object> data = new HashMap<>();
            data.put("state","DENIED");

            String recipientEmail =mailing;
            String subject = "Order Rejection";
            String productName = product_name;
            String orderNumber = order_id;
            String rejectionReason = "Fake Payment Screenshot or Incorrect Authorization Number";

            String body = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; }" +
                    "h1 { color: #333333; background: linear-gradient(to bottom, #f1f1f1, #d8d8d8); }" +
                    "p { color: #555555; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<h2>Order Rejection</h2>" +
                    "<p>Dear Customer,</p>" +
                    "<p>We regret to inform you that your order has been rejected.</p>" +
                    "<h3>Order Details</h3>" +
                    "<p>Order Number: <b>" + orderNumber + "</b></p>" +
                    "<p>Product: <b>" + productName + "</b></p>" +
                    "<h3>Rejection Reason</h3>" +
                    "<p>We apologize, but your order has been rejected due to the following reason:</p>" +
                    "<p>" + rejectionReason + "</p>" +
                    "<p>Please ensure that the payment screenshot provided" +
                    " is legitimate and the authorization number is accurate. If you believe this rejection is in error, kindly contact our customer support team " +
                    "in the app for further assistance.</p>" +
                    "<p>Thank you for considering our store.</p>" +
                    "<p>Best regards,</p>" +
                    "<p>The Gold Store Team</p>" +
                    "</body>" +
                    "</html>";

            DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("order");

            DatabaseReference currentUserRef = orderRef.child(order_id);
            currentUserRef.updateChildren(data);
            Toast.makeText(this, "Oder Rejected", Toast.LENGTH_SHORT).show();


            sendEmail(mailing,subject,body,sys_email,sys_password);

            finish();

        });

    }
    //
    private void sendEmail(String recipientEmail, String subject, String body, String senderEmail, String senderPassword) {
        SendEmailTask emailTask = new SendEmailTask();
        emailTask.execute(recipientEmail, subject, body, senderEmail, senderPassword);
    }

}


class SendEmailTask extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
        String recipientEmail = params[0];
        String subject = params[1];
        String body = params[2];
        String senderEmail = params[3];
        String senderPassword = params[4];

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // Replace with your SMTP server
        props.put("mail.smtp.port", "587"); // Replace with the appropriate port number
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject(subject);
            message.setContent(body, "text/html");

            //message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return null;
    }
}