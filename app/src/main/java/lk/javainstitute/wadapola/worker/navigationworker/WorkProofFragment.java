package lk.javainstitute.wadapola.worker.navigationworker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.model.CustomerData;


public class WorkProofFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private String selectedImageNumber = ""; // To store which image was selected
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_work_proof, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);
        String mobile = customerData.getMobile();

        ImageView imageViewAnim = view.findViewById(R.id.imageViewAnim2)
                ,imageViewImage1 = view.findViewById(R.id.imageViewProof1),
        imageViewImage2 = view.findViewById(R.id.imageViewProof2)
                ,imageViewImage3 = view.findViewById(R.id.imageViewProof3);

        Animation animation1 = AnimationUtils.loadAnimation(getContext(),R.anim.bounce_anim);

        imageViewAnim.startAnimation(animation1);

        // Load existing images
        loadProofImage(mobile, imageViewImage1, "1");
        loadProofImage(mobile, imageViewImage2, "2");
        loadProofImage(mobile, imageViewImage3, "3");

        // Click listeners to select an image
        imageViewImage1.setOnClickListener(v -> openImagePicker("1"));
        imageViewImage2.setOnClickListener(v -> openImagePicker("2"));
        imageViewImage3.setOnClickListener(v -> openImagePicker("3"));

        return view;
    }

    private void loadProofImage(String mobile, ImageView imageView, String imageNumber) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String[] fileExtensions = {"png", "jpg", "jpeg"};
        final boolean[] imageLoaded = {false};
        final int[] failedAttempts = {0};

        for (String ext : fileExtensions) {
            String filePath = "images/worker/workProof/" + mobile + "/" + imageNumber + "." + ext;
            StorageReference imageRef = storageReference.child(filePath);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (!imageLoaded[0]) {
                    imageLoaded[0] = true;

                    Glide.with(WorkProofFragment.this)
                            .load(uri)
                            .into(imageView);
                }
            }).addOnFailureListener(e -> {
                failedAttempts[0]++;
                if (failedAttempts[0] == fileExtensions.length && !imageLoaded[0]) {
                    imageView.setImageResource(R.drawable.logo_splash);
                }
            });

            if (imageLoaded[0]) break;
        }
    }

    private void openImagePicker(String imageNumber) {
        selectedImageNumber = imageNumber; // Store selected image number
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            if (selectedImageNumber.isEmpty()) return; // Ensure an image number is selected

            // Find corresponding ImageView
            ImageView targetImageView;
            switch (selectedImageNumber) {
                case "1": targetImageView = getView().findViewById(R.id.imageViewProof1); break;
                case "2": targetImageView = getView().findViewById(R.id.imageViewProof2); break;
                case "3": targetImageView = getView().findViewById(R.id.imageViewProof3); break;
                default: return;
            }

            // Display selected image in ImageView
            targetImageView.setImageURI(imageUri);

            // Upload the image to Firebase
            uploadImageToFirebase(imageUri, selectedImageNumber);
        }
    }
    private void uploadImageToFirebase(Uri imageUri, String imageNumber) {
        if (imageUri == null) return;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        // Get mobile number from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);
        String mobile = customerData.getMobile();

        String filePath = "images/worker/workProof/" + mobile + "/" + imageNumber + ".jpg";
        StorageReference imageRef = storageReference.child(filePath);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(getContext(), "Image updated successfully!", Toast.LENGTH_SHORT).show();
                    loadProofImage(mobile, getView().findViewById(getImageViewId(imageNumber)), imageNumber);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    // Helper function to get ImageView ID
    private int getImageViewId(String imageNumber) {
        switch (imageNumber) {
            case "1": return R.id.imageViewProof1;
            case "2": return R.id.imageViewProof2;
            case "3": return R.id.imageViewProof3;
            default: return -1;
        }
    }

}