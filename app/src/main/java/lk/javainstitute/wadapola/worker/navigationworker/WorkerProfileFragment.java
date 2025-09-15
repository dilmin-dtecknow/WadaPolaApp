package lk.javainstitute.wadapola.worker.navigationworker;

import static android.app.Activity.RESULT_OK;
import static lk.javainstitute.wadapola.model.CustomToast.cusErrorToast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.model.CustomToast;
import lk.javainstitute.wadapola.model.CustomerData;
import lk.javainstitute.wadapola.model.ServiceCategoryData;
import lk.javainstitute.wadapola.worker.WorkerHomeActivity;
import lk.javainstitute.wadapola.worker.WorkerSignUpActivity;


public class WorkerProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imageView;
    private String mobile;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_worker_profile, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        String log_userid = customerData.getId();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        EditText editTextEmail = view.findViewById(R.id.workerEmailEditText)
                ,editTextMobile = view.findViewById(R.id.profileMobileEditText1)
                ,editTextFname = view.findViewById(R.id.profileFnameEditText1)
                ,editTextLname = view.findViewById(R.id.profileLnameEditText1)
                ,editTextPrice = view.findViewById(R.id.profilePriceEditText1)
                ,editTextAbout = view.findViewById(R.id.aboutProfileEditText1)
                ,editTextCategory = view.findViewById(R.id.categoryEditText1)
                ;

        imageView = view.findViewById(R.id.logoImageView1WorkerProfile);

        Switch  working_status = view.findViewById(R.id.switch1);
        Button updateBtn = view.findViewById(R.id.updateProfileBtn1);

        firestore.collection("worker").document(log_userid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value !=null){

                            if (value.exists()){
                                editTextEmail.setText(value.getString("email"));
                                editTextMobile.setText(value.getString("mobile"));
                                editTextFname.setText(value.getString("fname"));
                                editTextLname.setText(value.getString("lname"));
                                editTextPrice.setText(value.getString("price"));
                                editTextAbout.setText(value.getString("about"));
                                editTextCategory.setText(value.getString("service_category"));

                                String workingStatus = value.getString("working_status");
                                mobile = value.getString("mobile");

                                loadProfileImage(mobile,imageView);

                                if (workingStatus.equals("search Work")){
                                    working_status.setChecked(false);
                                }else if (workingStatus.equals("Working")){
                                    working_status.setChecked(true);
                                }
                            }

                        }   else {
                            CustomToast.cusErrorToast(getContext(),"No result found",false);
                        }
                    }
                });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        //update working status
        working_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b==true){
                    HashMap<String,Object> document = new HashMap<>();
                    document.put("working_status","Working");

                    firestore.collection("worker").document(log_userid).update(document)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                         CustomToast.cusErrorToast(getContext(),"You're now change to Working staus",true);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("Log1","Profile: status update fail ");
                                }
                            });
                }else {
                    HashMap<String,Object> document = new HashMap<>();
                    document.put("working_status","search Work");

                    firestore.collection("worker").document(log_userid).update(document)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    CustomToast.cusErrorToast(getContext(),"You're now change to search Work status",true);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("Log1","Profile: status update fail ");
                                }
                            });
                }
            }
        });

//update profile info
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> document = new HashMap<>();
                document.put("about",editTextAbout.getText().toString());
                document.put("price",editTextPrice.getText().toString());
                document.put("fname",editTextFname.getText().toString());
                document.put("lname",editTextLname.getText().toString());

                firestore.collection("worker").document(log_userid).update(document)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                CustomToast.cusErrorToast(getContext(),"You're now Update your Profile details",true);
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

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        updateProfileImage(mobile,imageUri,imageView);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
            String filePath = "images/worker/profileImg/" + mobile+"."+ext;
            StorageReference imageRef = storageReference.child(filePath);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (!imageLoaded[0]) {
                    imageLoaded[0] = true; // Set the flag to true when image is loaded

                    // Use the Glide library to load the image into the ImageView
//                    ImageView imageView = findViewById(R.id.logoImageViewSingle1);
                    Glide.with(WorkerProfileFragment.this)
                            .load(uri) // Load the image from Firebase
                            .into(imageView);
                }
            }).addOnFailureListener(e -> {
                // Handle errors (e.g., file not found or permission issues)
                if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageLoaded[0]) {
                    // Show an error message if no image was found
                    Toast.makeText(getContext(), "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            if (imageLoaded[0]) {
                break; // Stop checking other extensions if the image is loaded
            }
        }
    }
    private void updateProfileImage(String mobile, Uri imageUri,ImageView imageView) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            String[] fileExtensions = {"png", "jpg", "jpeg"};
            boolean[] imageDeleted = {false};

            for (String ext : fileExtensions) {
                String filePath = "images/worker/profileImg/" + mobile + "." + ext;
                StorageReference imageRef = storageReference.child(filePath);

                imageRef.delete().addOnSuccessListener(unused -> {
                    if (!imageDeleted[0]) {
                        imageDeleted[0] = true; // Mark as deleted so it doesn’t loop again

                        // Upload the new image
                        String newFilePath = "images/worker/profileImg/" + mobile + ".jpg"; // Default to .jpg
                        StorageReference newImageRef = storageReference.child(newFilePath);

                        newImageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> newImageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            String downloadUrl = uri.toString();
                                            Toast.makeText(getContext(), "Image updated successfully!", Toast.LENGTH_SHORT).show();
                                            // Update the UI with the new image
                                            Glide.with(getContext()).load(downloadUrl).into(imageView);
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to retrieve new image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload new image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).addOnFailureListener(e -> {
                    if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageDeleted[0]) {
                        // If all deletions fail, upload new image anyway
                        String newFilePath = "images/worker/profileImg/" + mobile + ".jpg"; // Default to .jpg
                        StorageReference newImageRef = storageReference.child(newFilePath);

                        newImageRef.putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> newImageRef.getDownloadUrl()
                                        .addOnSuccessListener(uri -> {
                                            String downloadUrl = uri.toString();
                                            Toast.makeText(getContext(), "Image updated successfully!", Toast.LENGTH_SHORT).show();
                                            // Update the UI with the new image
                                            Glide.with(getContext()).load(downloadUrl).into(imageView);
                                        })
                                        .addOnFailureListener(ex -> Toast.makeText(getContext(), "Failed to retrieve new image URL: " + ex.getMessage(), Toast.LENGTH_SHORT).show()))
                                .addOnFailureListener(ex -> Toast.makeText(getContext(), "Failed to upload new image: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });

                if (imageDeleted[0]) break; // Stop looping if image is deleted
            }
        } else {
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

}

