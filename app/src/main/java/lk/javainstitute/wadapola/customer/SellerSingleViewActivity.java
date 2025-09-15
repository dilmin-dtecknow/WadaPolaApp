package lk.javainstitute.wadapola.customer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.model.CustomToast;
import lk.javainstitute.wadapola.model.CustomerData;
import lk.javainstitute.wadapola.model.PaymentHistoryData;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

public class SellerSingleViewActivity extends AppCompatActivity {

    private String price;
    private static final int PAYHERE_REQUEST = 11001;
    private String customerId;
    private String selectWorkerId;

    private String paymentId;

    private String workerFName;
    private String workerLName;
    private String workerMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_seller_single_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String workerId = intent.getStringExtra("worker_id");
        selectWorkerId = workerId;

        TextView textViewName = findViewById(R.id.textViewSingle1);
        TextView textViewType = findViewById(R.id.textViewSingle2);
        TextView textViewProvince = findViewById(R.id.textViewSellerSingleProvinse1);
        TextView textViewSellerSingleAbout = findViewById(R.id.textViewSellerSingleAbout);
        TextView textViewSellerSinglePrice = findViewById(R.id.textViewSellerSinglePrice);
        EditText editTextNumberDecimalSellerSingle = findViewById(R.id.editTextNumberDecimalSellerSingle);
        ImageView imageView = findViewById(R.id.logoImageViewSingle1);
        Button buttonBook = findViewById(R.id.SellerSingleBookNow);
        Button buttonPay = findViewById(R.id.buttonSellerSinglePay);

        RatingBar ratingBar = findViewById(R.id.ratingBar2);
        TextView ratingText = findViewById(R.id.textViewRatings);

        SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        customerId = customerData.getId();

        Log.i("Log1", "Worker " + workerId);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("worker").document(workerId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null) {
                            if (value.exists()) {
                                String fname = value.getString("fname");
                                String lname = value.getString("lname");
                                String about = value.getString("about");
                                price = value.getString("price");
                                String serviceCategory = value.getString("service_category");
                                String status = value.getString("working_status");
                                String mobile = value.getString("mobile");

                                workerFName = fname;
                                workerLName = lname;
                                workerMobile = mobile;

                                loadProfileImage(mobile);
                                loadWorkProofImages(mobile);

                                textViewName.setText(fname + " " + lname);
                                textViewType.setText(serviceCategory);
                                textViewSellerSinglePrice.setText("1h = " + Double.parseDouble(price));
                                textViewSellerSingleAbout.setText(about);

                                firestore.collection("address")
                                        .whereEqualTo("user_id", workerId).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                    DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                                    String province = documentSnapshot.getString("province");

                                                    textViewProvince.setText(province + " Province");
                                                } else {
                                                    textViewProvince.setText("Province not fond");
                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("Log1", "SellerSingle Address load failed");
                                            }
                                        });
                                if (intent.getStringExtra("payment_id") != null) {
                                    // Check payment status
                                    firestore.collection("payments")
                                            .document(intent.getStringExtra("payment_id"))
                                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    if (value != null) {
                                                        String paymentStatus = value.getString("payment_status");

                                                        // Hide button if status is not "Working" OR payment status is not "Pending"
                                                        if (!"Working".equals(status) || !"Pending".equals(paymentStatus)) {
                                                            buttonPay.setVisibility(View.INVISIBLE);
                                                            editTextNumberDecimalSellerSingle.setVisibility(View.INVISIBLE);
                                                        } else {
                                                            buttonPay.setVisibility(View.VISIBLE);
                                                            editTextNumberDecimalSellerSingle.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                }
                                            });
                                    return;
                                }

                                if (!status.equals("Working")) {
                                    buttonPay.setVisibility(View.INVISIBLE);
                                    editTextNumberDecimalSellerSingle.setVisibility(View.INVISIBLE);
                                    return;
                                }

                            }
                        }
                    }
                });

        //ratings
        firestore.collection("rating").whereEqualTo("worker_id", workerId)
                .get()
                .addOnCompleteListener(task2 -> {
                    QuerySnapshot querySnapshot = task2.getResult();
                    int totalRatings = 0;
                    int ratingCount = 0;
                    float averageRating = 0;

                    for (QueryDocumentSnapshot ratingDoc : querySnapshot) {
                        Long rating = ratingDoc.getLong("rating");
                        if (rating != null) {
                            totalRatings += rating;
                            ratingCount++;
                        }
                    }

                    if (ratingCount > 0) {
                        averageRating = (float) totalRatings / ratingCount;
                    }
                    ratingBar.setRating(averageRating);
                    ratingText.setText(String.valueOf(averageRating));

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SellerSingleViewActivity.this, "Failed to fetch ratings", Toast.LENGTH_SHORT).show();
                });
        //ratings

//        pay
        buttonPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringHour = editTextNumberDecimalSellerSingle.getText().toString();
                if (stringHour.isBlank()) {
//                    Log.i("Log1", "working hours empty");
                    CustomToast.cusErrorToast(SellerSingleViewActivity.this, "working hours empty", false);
                    return;
                } else {
                    //pending payment update
                    if (intent.getStringExtra("payment_id") != null) {
                        String pending_payId = intent.getStringExtra("payment_id");
                        paymentId = pending_payId;
                        double hours1 = Double.parseDouble(stringHour);
                        if (hours1 <= 0) {
                            Log.i("Log1", "Invalid working hours: " + hours1);
                            // Handle the case where working hours are zero or negative
                            Log.i("Log1", "Working hours must be greater than zero");
                            return;
                        }
                        double hours = Double.parseDouble(stringHour);
                        double priceDouble = Double.parseDouble(price);

                        double totalPrice = hours * priceDouble;
                        DecimalFormat df = new DecimalFormat("#.00");
                        String formattedPrice = df.format(totalPrice);
                        double formattedDoublePrice = Double.parseDouble(formattedPrice);
                        Log.i("Log1", "Price: " + formattedDoublePrice);
                        HashMap<String, Object> document = new HashMap<>();
                        document.put("payment_status", "Paid");
                        firestore.collection("payments").document(pending_payId).update(document)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        InitRequest req = new InitRequest();
                                        req.setMerchantId("1223369");       // Merchant ID
                                        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
                                        req.setAmount(formattedDoublePrice);             // Final Amount to be charged
                                        req.setOrderId(pending_payId);        // Unique Reference ID
                                        req.setItemsDescription(workerId + "Payment");  // Item description title
                                        req.setCustom1("This is the custom message 1");
                                        req.setCustom2("This is the custom message 2");
                                        req.getCustomer().setFirstName(workerFName);
                                        req.getCustomer().setLastName(workerLName);
                                        req.getCustomer().setEmail("samanp@gmail.com");
                                        req.getCustomer().setPhone(workerMobile);
                                        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
                                        req.getCustomer().getAddress().setCity("Colombo");
                                        req.getCustomer().getAddress().setCountry("Sri Lanka");

                                        Intent intent = new Intent(SellerSingleViewActivity.this, PHMainActivity.class);
                                        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
                                        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
                                        startActivityForResult(intent, PAYHERE_REQUEST); //unique request ID e.g. "11001"
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        CustomToast.cusErrorToast(SellerSingleViewActivity.this, "Payment status Update fail", false);
                                    }
                                });
                        return;
                    }

                    double hours1 = Double.parseDouble(stringHour);
                    if (hours1 <= 0) {
                        Log.i("Log1", "Invalid working hours: " + hours1);
                        // Handle the case where working hours are zero or negative
                        Log.i("Log1", "Working hours must be greater than zero");
                        return;
                    }
                    double hours = Double.parseDouble(stringHour);
                    double priceDouble = Double.parseDouble(price);

                    double totalPrice = hours * priceDouble;
                    DecimalFormat df = new DecimalFormat("#.00");
                    String formattedPrice = df.format(totalPrice);
                    double formattedDoublePrice = Double.parseDouble(formattedPrice);
                    Log.i("Log1", "Price: " + formattedDoublePrice);

                    HashMap<String, Object> document = new HashMap<>();
                    document.put("cus_id", customerData.getId());
                    document.put("worker_id", workerId);
                    document.put("date_time", FieldValue.serverTimestamp());
                    document.put("price", price);
                    document.put("work_hors", stringHour);
                    document.put("payment_status", "Pending");


                    FirebaseFirestore firestorePayment = FirebaseFirestore.getInstance();
                    firestorePayment.collection("payments").add(document)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    paymentId = documentReference.getId();

                                    HashMap<String, Object> updateDocument = new HashMap<>();
                                    updateDocument.put("working_status", "Working");

                                    firestorePayment.collection("worker").document(workerId).update(updateDocument)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    InitRequest req = new InitRequest();
                                                    req.setMerchantId("1223369");       // Merchant ID
                                                    req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
                                                    req.setAmount(formattedDoublePrice);             // Final Amount to be charged
                                                    req.setOrderId(paymentId);        // Unique Reference ID
                                                    req.setItemsDescription(workerId + "Payment");  // Item description title
                                                    req.setCustom1("This is the custom message 1");
                                                    req.setCustom2("This is the custom message 2");
                                                    req.getCustomer().setFirstName(workerFName);
                                                    req.getCustomer().setLastName(workerLName);
                                                    req.getCustomer().setEmail("samanp@gmail.com");
                                                    req.getCustomer().setPhone(workerMobile);
                                                    req.getCustomer().getAddress().setAddress("No.1, Galle Road");
                                                    req.getCustomer().getAddress().setCity("Colombo");
                                                    req.getCustomer().getAddress().setCountry("Sri Lanka");

                                                    Intent intent = new Intent(SellerSingleViewActivity.this, PHMainActivity.class);
                                                    intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
                                                    PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
                                                    startActivityForResult(intent, PAYHERE_REQUEST); //unique request ID e.g. "11001"
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("Log1", "Payment insert and working status update fail");
                                                }
                                            });


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("Log1", "Payment insert fail");
                                }
                            });

                }
            }
        });

        buttonBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, Object> document = new HashMap<>();
                document.put("customer_id", customerId);
                document.put("worker_id", workerId);
                document.put("date_time", FieldValue.serverTimestamp());
                document.put("booking_status", "Pending");

                firestore.collection("booking")
                        .whereEqualTo("customer_id", customerId)
                        .whereEqualTo("worker_id", workerId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot result = task.getResult();
                                    if (result.size() == 1) {
                                        Toast.makeText(SellerSingleViewActivity.this, "You are already book this worker", Toast.LENGTH_SHORT).show();
                                    } else {
                                        firestore.collection("booking").add(document)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        CustomToast.cusErrorToast(SellerSingleViewActivity.this, "Booking success", true);
//                                buttonBook.setEnabled(false);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.i("Log1", "Booking insert fail");
                                                    }
                                                });
                                    }
                                } else {
                                    Log.i("Log1", "SellerSingle booking search");
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("Log1", "SellerSingle booking search fail");
                            }
                        });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {
                String msg;
                if (response != null) {
                    if (response.isSuccess()) {
                        msg = "Activity result:" + response.getData().toString();
                        Log.i("Log1", "response success" + msg); //sucess payment

                        HashMap<String, Object> document = new HashMap<>();
                        document.put("payment_status", "Paid");

                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection("payments").document(paymentId)
                                .update(document)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        firestore.collection("booking")
                                                .whereEqualTo("customer_id", customerId)
                                                .whereEqualTo("worker_id", selectWorkerId)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        QuerySnapshot result = task.getResult();
                                                        String bookingId = result.getDocuments().get(0).getId();
                                                        firestore.collection("booking").document(bookingId).delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        HashMap<String, Object> updateDocument = new HashMap<>();
                                                                        updateDocument.put("working_status", "search Work");
                                                                        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                                                                        firebaseFirestore.collection("worker").document(selectWorkerId).update(updateDocument)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void unused) {
                                                                                        CustomToast.cusErrorToast(SellerSingleViewActivity.this, "you are successfully paid", true);
                                                                                        Intent intent = new Intent(SellerSingleViewActivity.this, HomeActivity.class);
                                                                                        startActivity(intent);
                                                                                    }
                                                                                })
                                                                                .addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {

                                                                                    }
                                                                                });

//                                                                        CustomToast.cusErrorToast(SellerSingleViewActivity.this, "you are successfully paid", true);
//                                                                        Intent intent = new Intent(SellerSingleViewActivity.this, HomeActivity.class);
//                                                                        startActivity(intent);
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        CustomToast.cusErrorToast(SellerSingleViewActivity.this, "payment booking delete fail"
                                                                                , false);
                                                                    }
                                                                });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        CustomToast.cusErrorToast(SellerSingleViewActivity.this, "payment booking search faild"
                                                                , false);
                                                    }
                                                });

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Log1", "payment status update fail");
                                    }
                                });


                    } else {
                        msg = "Result:" + response.toString();
                        Log.e("Log1", "response" + msg);
                    }
                } else {
                    msg = "Result: no response";
                    Log.i("Log1", "payment " + msg);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response != null)
                    Log.i("Log1", "Response " + response.toString()); //cancel payment
                else
                    Log.e("Log1", "User canceled the request");
            }
        }
    }

    private void loadProfileImage(String mobile) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String[] fileExtensions = {"png", "jpg", "jpeg"};
        final boolean[] imageLoaded = {false};

        for (String ext : fileExtensions) {
            String filePath = "images/worker/profileImg/" + mobile + "." + ext;
            StorageReference imageRef = storageReference.child(filePath);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (!imageLoaded[0]) {
                    imageLoaded[0] = true; // Set the flag to true when image is loaded

                    // Use the Glide library to load the image into the ImageView
                    ImageView imageView = findViewById(R.id.logoImageViewSingle1);
                    Glide.with(SellerSingleViewActivity.this)
                            .load(uri) // Load the image from Firebase
                            .into(imageView);
                }
            }).addOnFailureListener(e -> {
                // Handle errors (e.g., file not found or permission issues)
                if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageLoaded[0]) {
                    // Show an error message if no image was found
                    Toast.makeText(SellerSingleViewActivity.this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            if (imageLoaded[0]) {
                break; // Stop checking other extensions if the image is loaded
            }
        }
    }

    private void loadWorkProofImages(String mobile) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("images/worker/workProof/" + mobile);

        ArrayList<SlideModel> slideModelArrayList = new ArrayList<>();
        ImageSlider imageSlider = findViewById(R.id.image_slider_single); // Your ImageSlider

        storageReference.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    slideModelArrayList.add(new SlideModel(uri.toString(), ScaleTypes.FIT));

                    // When all images are added, update the slider
                    if (slideModelArrayList.size() == listResult.getItems().size()) {
                        imageSlider.setImageList(slideModelArrayList, ScaleTypes.FIT);
                    }
                });
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load images: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

}