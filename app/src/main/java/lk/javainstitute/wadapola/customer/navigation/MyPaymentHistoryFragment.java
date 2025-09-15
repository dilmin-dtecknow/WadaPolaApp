package lk.javainstitute.wadapola.customer.navigation;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.customer.SellerSingleViewActivity;
import lk.javainstitute.wadapola.model.CustomToast;
import lk.javainstitute.wadapola.model.CustomerData;
import lk.javainstitute.wadapola.model.PaymentHistoryData;
import lk.javainstitute.wadapola.model.SQLiteHelper;


public class MyPaymentHistoryFragment extends Fragment {
    private String log_user;
    private ArrayList<PaymentHistoryData> paymentHistoryDataArrayList = new ArrayList<>();
    private CustomerPaymentHistoryItemAdapter paymentHistoryItemAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_payment_history, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        log_user = customerData.getId();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCusPay1);

        paymentHistoryItemAdapter = new CustomerPaymentHistoryItemAdapter(paymentHistoryDataArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(paymentHistoryItemAdapter);
        loadFirebaseData();

        return view;
    }

    private void loadFirebaseData() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("payments")
                .whereEqualTo("cus_id", log_user).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            paymentHistoryDataArrayList.clear(); // Clear old data
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String cusId = document.getString("worker_id");
                                Timestamp dateTime = document.getTimestamp("date_time");
                                String paymentStatus = document.getString("payment_status");
                                String price = document.getString("price");
                                String workHours = document.getString("work_hors");
                                String pay_id = document.getId();

                                firestore.collection("worker").document(cusId)
                                        .get().addOnCompleteListener(task1 -> {
                                            if (!task1.isSuccessful()) {
                                                CustomToast.cusErrorToast(getContext(), "User not found", false);
                                                return;
                                            }

                                            DocumentSnapshot result = task1.getResult();
                                            String fname = result.getString("fname");
                                            String lname = result.getString("lname");
                                            String mobile = result.getString("mobile");
                                            String serviceCategory = result.getString("service_category");
                                            String email = result.getString("email");

                                            firestore.collection("rating").whereEqualTo("worker_id", cusId)
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

                                                        // Create a new object and add to the list
                                                        PaymentHistoryData historyData = new PaymentHistoryData(pay_id,log_user,cusId,workHours,price,dateTime,paymentStatus,fname,lname
                                                                ,mobile,averageRating,serviceCategory,email);

                                                        paymentHistoryDataArrayList.add(historyData);
                                                        paymentHistoryItemAdapter.notifyDataSetChanged(); // Refresh UI
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getContext(), "Failed to fetch ratings", Toast.LENGTH_SHORT).show();
                                                    });
                                        }).addOnFailureListener(e -> {
                                            CustomToast.cusErrorToast(getContext(), "Customer data fetch failed", false);
                                        });
                            }
                        } else {
                            CustomToast.cusErrorToast(getContext(), "No payment history found", false);
                        }
                    }
                }).addOnFailureListener(e -> Log.i("Log1", "Payment History load failed"));
    }
}
class CustomerPaymentHistoryItemAdapter extends RecyclerView.Adapter<CustomerPaymentHistoryItemAdapter.CustomerPaymentHistoryItemAdapterViewHolder>{
    ArrayList<PaymentHistoryData> paymentHistoryDataArrayList;
    public CustomerPaymentHistoryItemAdapter(ArrayList<PaymentHistoryData> paymentHistoryDataArrayList) {
        this.paymentHistoryDataArrayList = paymentHistoryDataArrayList;
    }

    @NonNull
    @Override
    public CustomerPaymentHistoryItemAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.payment_history_item, parent, false);
        CustomerPaymentHistoryItemAdapter.CustomerPaymentHistoryItemAdapterViewHolder paymentHistoryItemViewHolder = new CustomerPaymentHistoryItemAdapter.CustomerPaymentHistoryItemAdapterViewHolder(view);
        return paymentHistoryItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerPaymentHistoryItemAdapterViewHolder holder, int position) {
        PaymentHistoryData paymentHistoryData = paymentHistoryDataArrayList.get(position);
        holder.textViewName.setText(paymentHistoryData.getfName()+" "+paymentHistoryData.getlName());
        holder.textViewHour.setText(paymentHistoryData.getWork_hors()+"Hours");
        holder.textViewPrice.setText("Rs."+paymentHistoryData.getPrice());
        holder.textViewRating.setText(String.valueOf(paymentHistoryData.getRating()));
        holder.textViewDate.setText(getFormattedDate(paymentHistoryData.getDate_time()));
        holder.ratingBar.setRating(paymentHistoryData.getRating());
        holder.textViewPaidStatus.setText(paymentHistoryData.getPayment_status());

        String mobile = paymentHistoryData.getMobile();

        //load profile image
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
//                    ImageView imageView = findViewById(R.id.logoImageViewSingle1);
                    Glide.with(holder.itemView.getContext())
                            .load(uri) // Load the image from Firebase
                            .into(holder.imageView);
                }
            }).addOnFailureListener(e -> {
                // Handle errors (e.g., file not found or permission issues)
                if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageLoaded[0]) {
                    // Show an error message if no image was found
                    Toast.makeText(holder.itemView.getContext(), "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.logo_splash) // Replace with your default image resource
                            .into(holder.imageView);
                }
            });

            if (imageLoaded[0]) {
                break; // Stop checking other extensions if the image is loaded
            }
        }
//load profile image

//        rating
        holder.ratingBar.setOnRatingBarChangeListener(null); // Remove previous listener to avoid multiple triggers

        holder.ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                if (!b) return; // Ensure the change is from user input

                float rating = ratingBar.getRating();
                Log.i("Log1", String.valueOf(rating));

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                HashMap<String, Object> document = new HashMap<>();
                document.put("rating", rating);
                document.put("worker_id", paymentHistoryData.getWorker_id());

                // Disable listener temporarily to prevent duplicate execution
                ratingBar.setOnRatingBarChangeListener(null);

                firestore.collection("rating").add(document)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                ratingBar.setIsIndicator(true);  // Disable RatingBar after rating
                                Log.i("Log1", "Rating stored successfully");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                CustomToast.cusErrorToast(ratingBar.getContext(), "Rating add failed", false);
                            }
                        });
            }
        });

//        rating
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SellerSingleViewActivity.class);
                intent.putExtra("worker_id",paymentHistoryData.getWorker_id());
                intent.putExtra("payment_id",paymentHistoryData.getPay_id());
                intent.putExtra("work_hour",holder.textViewHour.getText().toString());
                view.getContext().startActivity(intent);
            }
        });

        //save contact
        holder.imageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String worId = paymentHistoryData.getWorker_id();
                String saveWorFName = paymentHistoryData.getfName();
                String saveWorLName = paymentHistoryData.getlName();
                String saveType = paymentHistoryData.getType();
                String saveEmail = paymentHistoryData.getEmail();
                String savMobile = paymentHistoryData.getMobile();

//                Log.i("Log1","Name: "+paymentHistoryData.getfName()+" "+paymentHistoryData.getlName()
//                +"Type: "+paymentHistoryData.getType()+" "+"Email: "+paymentHistoryData.getEmail()+" "+
//                        "Mobile: "+paymentHistoryData.getMobile());
                //save
                SQLiteHelper sqLiteHelper = new SQLiteHelper(view.getContext(), "mycontact.db",null,1);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();

                        ContentValues contentValues = new ContentValues();
                        contentValues.put("worker_id",worId);
                        contentValues.put("fname",saveWorFName);
                        contentValues.put("lname",saveWorLName);
                        contentValues.put("mobile",savMobile);
                        contentValues.put("email",saveEmail);
                        contentValues.put("type",saveType);

                        Log.i("Log1",worId);

                        if (worId !=null){
// Check if worker_id exists in the database
                            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM contact WHERE worker_id = ?", new String[]{worId});

                            if (cursor.getCount() > 0) {
                                // If exists, update the existing record
                                int updatedRows = sqLiteDatabase.update("contact", contentValues, "worker_id=?", new String[]{worId});
                                view.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (updatedRows > 0) {
//                                    Log.d("DB", "Contact updated successfully.");
                                            CustomToast.cusErrorToast(view.getContext(), "Contact updated successfully.",true);

                                        } else {
//                                    Log.e("DB", "Error updating contact.");
                                            CustomToast.cusErrorToast(view.getContext(), "Error updating contact.",false);

                                        }
                                    }
                                });

                            } else {
                                // If not exists, insert new record
                                long insertResult = sqLiteDatabase.insert("contact", null, contentValues);

                                view.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (insertResult != -1) {
                                            Log.d("DB", "Contact added successfully.");
                                            CustomToast.cusErrorToast(view.getContext(), "Contact added successfully.",true);

                                        } else {
                                            Log.e("DB", "Error inserting contact.");
                                            CustomToast.cusErrorToast(view.getContext(), "Error inserting contact.",false);

                                        }
                                    }
                                });

                            }

                            cursor.close();
                        } else {
                            Log.e("DB", "worker_id is null. Cannot insert/update.");
                        }

                        sqLiteDatabase.close();


                    }
                }).start();
            }
        });

    }

    @Override
    public int getItemCount() {
        return paymentHistoryDataArrayList.size();
    }

    static class CustomerPaymentHistoryItemAdapterViewHolder extends RecyclerView.ViewHolder{
        TextView textViewName,textViewHour,textViewPrice,textViewDate,textViewPaidStatus,textViewRating;
        ImageView imageView,imageViewSave;
        RatingBar ratingBar;
        public CustomerPaymentHistoryItemAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewSellerName1PayHistory);
            textViewHour = itemView.findViewById(R.id.textViewWorkHourPayHistory1);
            textViewPrice = itemView.findViewById(R.id.textViewTotalPrice);
            textViewPaidStatus = itemView.findViewById(R.id.textViewPaidStatus1);
            textViewDate = itemView.findViewById(R.id.textViewPaidDate);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            imageView = itemView.findViewById(R.id.shapeImageSellerItemPayHistory);
            imageViewSave = itemView.findViewById(R.id.imageViewSave);
        }
    }
    public String getFormattedDate(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy 'at' HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
}
