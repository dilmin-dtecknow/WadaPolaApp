package lk.javainstitute.wadapola.customer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.HashMap;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.model.CustomToast;
import lk.javainstitute.wadapola.model.CustomerData;
import lk.javainstitute.wadapola.worker.navigationworker.WorkerProfileFragment;

public class MyProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imageView;
    private String mobile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = MyProfileActivity.this.getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        String log_userid = customerData.getId();

        EditText editTextEmail = findViewById(R.id.customerEmailEditText)
                ,editTextMobile = findViewById(R.id.customer_profileMobileEditText1)
                ,editTextFname = findViewById(R.id.customer_profileFnameEditText1)
                ,editTextLname = findViewById(R.id.customer_profileLnameEditText1);

        Button updateBtn = findViewById(R.id.customer_updateProfileBtn1);
        imageView = findViewById(R.id.customer_logoImageViewProfile);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("customer").document(log_userid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value !=null){

                            if (value.exists()){
                                editTextEmail.setText(value.getString("email"));
                                editTextMobile.setText(value.getString("mobile"));
                                editTextFname.setText(value.getString("fname"));
                                editTextLname.setText(value.getString("lname"));
                                mobile = value.getString("mobile");

                                loadProfileImage(mobile,imageView);

                            }

                        }   else {
                            CustomToast.cusErrorToast(MyProfileActivity.this,"No result found",false);
                        }
                    }
                });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> document = new HashMap<>();
                document.put("fname",editTextFname.getText().toString());
                document.put("lname",editTextLname.getText().toString());

                firestore.collection("customer").document(log_userid).update(document)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                CustomToast.cusErrorToast(MyProfileActivity.this,"You're now Update your Profile details",true);
                                showNotification(editTextFname.getText().toString());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("Log1","Profile: Profile details update fail ");
                            }
                        });
            }
        });

    }
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        updateProfileImage(mobile,imageUri,imageView);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Display the selected image

            imageView.setImageURI(imageUri);

        }
    }

    private void loadProfileImage(String mobile, ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String[] fileExtensions = {"png", "jpg", "jpeg"};
        final boolean[] imageLoaded = {false};

        for (String ext : fileExtensions) {
            String filePath = "images/profileImg/" + mobile;
            StorageReference imageRef = storageReference.child(filePath);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (!imageLoaded[0]) {
                    imageLoaded[0] = true; // Set the flag to true when image is loaded

                    // Use the Glide library to load the image into the ImageView
//                    ImageView imageView = findViewById(R.id.logoImageViewSingle1);
                    Glide.with(MyProfileActivity.this)
                            .load(uri) // Load the image from Firebase
                            .into(imageView);
                }
            }).addOnFailureListener(e -> {
                // Handle errors (e.g., file not found or permission issues)
                if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageLoaded[0]) {
                    // Show an error message if no image was found
                    Toast.makeText(MyProfileActivity.this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            if (imageLoaded[0]) {
                break; // Stop checking other extensions if the image is loaded
            }
        }
    }
    private void updateProfileImage(String mobile, Uri imageUri, ImageView imageView) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            String[] fileExtensions = {"png", "jpg", "jpeg"};
            boolean[] imageDeleted = {false};

            for (String ext : fileExtensions) {
                String filePath = "images/profileImg/" + mobile;
                StorageReference imageRef = storageReference.child(filePath);

                imageRef.delete().addOnSuccessListener(unused -> {
                    if (!imageDeleted[0]) {
                        imageDeleted[0] = true; // Mark as deleted so it doesn’t loop again

                        // Upload the new image
                        String newFilePath = "images/profileImg/" +mobile; // Default to .jpg
                        StorageReference newImageRef = storageReference.child(newFilePath);

                        newImageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> newImageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            String downloadUrl = uri.toString();
                                            Toast.makeText(MyProfileActivity.this, "Image updated successfully!", Toast.LENGTH_SHORT).show();
                                            // Update the UI with the new image
                                            Glide.with(MyProfileActivity.this).load(downloadUrl).into(imageView);
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(MyProfileActivity.this, "Failed to retrieve new image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                                .addOnFailureListener(e -> Toast.makeText(MyProfileActivity.this, "Failed to upload new image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).addOnFailureListener(e -> {
                    if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageDeleted[0]) {
                        // If all deletions fail, upload new image anyway
                        String newFilePath = "images/profileImg/" +mobile; // Default to .jpg
                        StorageReference newImageRef = storageReference.child(newFilePath);

                        newImageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> newImageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            String downloadUrl = uri.toString();
                                            Toast.makeText(MyProfileActivity.this, "Image updated successfully!", Toast.LENGTH_SHORT).show();
                                            // Update the UI with the new image
                                            Glide.with(MyProfileActivity.this).load(downloadUrl).into(imageView);
                                        })
                                        .addOnFailureListener(ex -> Toast.makeText(MyProfileActivity.this, "Failed to retrieve new image URL: " + ex.getMessage(), Toast.LENGTH_SHORT).show()))
                                .addOnFailureListener(ex -> Toast.makeText(MyProfileActivity.this, "Failed to upload new image: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });

                if (imageDeleted[0]) break; // Stop looping if image is deleted
            }
        } else {
//            Toast.makeText(MyProfileActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
            CustomToast.cusErrorToast(MyProfileActivity.this,"No Image Selected",false);
        }
    }

    private void showNotification(String name){
        Intent intent = new Intent(MyProfileActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyProfileActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyProfileActivity.this, "default_channel_id")
                .setSmallIcon(R.drawable.logo_splash)
                .setContentTitle("WadaPola App")
                .setContentText("This is new Success notification")
                .setStyle(
                        new NotificationCompat.InboxStyle()
                                .addLine("Re: "+name+" Your profile detail update success")
//                                .addLine("Delivery on its way")
//                                .addLine("Follow-up")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default_channel_id",
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Display the notification
        notificationManager.notify(1, builder.build());
    }
}