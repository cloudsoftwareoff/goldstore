package com.cloudsoftware.goldstorex;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private ArrayList<HashMap<String, Object>> chatData = new ArrayList<>();
    private int i;
    private ListView listView;



    // Constructor to pass data
    public MyBottomSheetDialogFragment(ArrayList<HashMap<String, Object>> data, int i) {
        this.i=i;
        chatData = data;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_sheet, container, false);
        LinearLayout copy_ = rootView.findViewById(R.id.copy_linear);
        LinearLayout delete_message = rootView.findViewById(R.id.delete_linear);

        delete_message.setVisibility(View.GONE);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();
        if(uid.equals(chatData.get(i).get("sender").toString())){
            delete_message.setVisibility(View.VISIBLE);
        }

        // Use the memberList data here

        copy_.setOnClickListener(view -> {
            // Get the system clipboard manager
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);

            // Create a ClipData object to hold the text to be copied
            ClipData clip = ClipData.newPlainText("Copied Text",chatData.get(i).get("message").toString());

            // Set the ClipData to the clipboard
            clipboard.setPrimaryClip(clip);

            // Show a toast to indicate that the text has been copied
            Toast.makeText(requireContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();

        });

        delete_message.setOnClickListener(view -> {

            if(uid.equals(chatData.get(i).get("sender").toString())){

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                String childKeyToDelete = "main_chat/global/content/"+chatData.get(i).get("id").toString(); // Replace with the actual child key


                DatabaseReference childRefToDelete = databaseReference.child(childKeyToDelete);

// Remove the child using removeValue()
                childRefToDelete.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Child deleted successfully
                            Toast.makeText(getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // An error occurred while deleting the child
                            Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                        });

            }

        });

        return rootView;
    }
}


