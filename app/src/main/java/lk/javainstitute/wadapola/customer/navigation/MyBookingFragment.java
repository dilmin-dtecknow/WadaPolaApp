package lk.javainstitute.wadapola.customer.navigation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
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
import java.util.Locale;

import lk.javainstitute.wadapola.MapActivity;
import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.SendMessageActivity;
import lk.javainstitute.wadapola.customer.SellerSingleViewActivity;
import lk.javainstitute.wadapola.model.BookingData;
import lk.javainstitute.wadapola.model.CustomToast;
import lk.javainstitute.wadapola.model.CustomerData;
import lk.javainstitute.wadapola.model.TestData;


public class MyBookingFragment extends Fragment {

    private ArrayList<BookingData> bookingDataArrayList = new ArrayList<>();
    private BookingItemAdapter bookingItemAdapter;

    private String logUser_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_booking, container, false);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        logUser_id = customerData.getId();
loadFireBaseData();
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewBooking1);

//        ArrayList<TestData> bookingDataArrayList = new ArrayList<>();
//        bookingDataArrayList.add(new TestData("Sahan Perera","Meson",400));
//        bookingDataArrayList.add(new TestData("Janith Perera","Meson",400));
//        bookingDataArrayList.add(new TestData("Kamal Perera","Meson",400));

        // Add the ItemDecoration to the RecyclerView
        int verticalSpaceHeight = getResources().getDimensionPixelSize(R.dimen.vertical_space_height);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(verticalSpaceHeight));

        bookingItemAdapter = new BookingItemAdapter(bookingDataArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(bookingItemAdapter);

        return view;
    }

    private void loadFireBaseData(){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("booking")
                .whereEqualTo("customer_id",logUser_id).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(getContext(), "No Result found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (QueryDocumentSnapshot document: task.getResult()){
                            String statusBooking = document.getString("booking_status");
                            Timestamp dateTime = document.getTimestamp("date_time");
                            String workerId = document.getString("worker_id");

                            firestore.collection("worker").document(workerId)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (!task.isSuccessful()){
                                                Toast.makeText(getContext(), "No worker found", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            DocumentSnapshot result = task.getResult();
                                            String fname = result.getString("fname");
                                            String lname = result.getString("lname");
                                            String price = result.getString("price");
                                            String mobile = result.getString("mobile");
                                            String serviceCategory = result.getString("service_category");

                                            firestore.collection("address").whereEqualTo("user_id",workerId)
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (!task.isSuccessful()){
                                                                Toast.makeText(getContext(), "Booking worker address not found", Toast.LENGTH_SHORT).show();
                                                                return;
                                                            }
                                                            QuerySnapshot snapshot = task.getResult();
                                                            String latitude = snapshot.getDocuments().get(0).getString("latitude");
                                                            String longitude = snapshot.getDocuments().get(0).getString("longitude");

                                                            bookingDataArrayList.add(
                                                                    new BookingData(logUser_id,workerId,statusBooking,dateTime,fname,lname,serviceCategory,latitude,longitude,mobile,price)
                                                            );
                                                            bookingItemAdapter.notifyDataSetChanged();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.i("Log1","fail to load address booking worker");
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Log1","Booking Worker fail");
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Log1","Booking search fail");
                    }
                });
    }
}

class BookingItemAdapter extends RecyclerView.Adapter<BookingItemAdapter.BookingItemViewHolder>{
    ArrayList<BookingData> bookingDataArrayList;

    public BookingItemAdapter(ArrayList<BookingData> bookingDataArrayList) {
        this.bookingDataArrayList = bookingDataArrayList;
    }

    @NonNull
    @Override
    public BookingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView = layoutInflater.inflate(R.layout.booking_item_card, parent, false);

        BookingItemViewHolder bookingItemViewHolder = new BookingItemViewHolder(inflatedView);
        return bookingItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BookingItemViewHolder holder, int position) {
        BookingData bookingData = bookingDataArrayList.get(position);
        String name = bookingData.getFname();
        holder.textView1.setText(name+" "+bookingData.getLname());
        holder.textView4.setText(bookingData.getService_category());
        holder.textView2.setText(getFormattedDate(bookingData.getDate_time()));
        holder.textView3.setText(bookingData.getPrice());
        holder.textView5.setText(bookingData.getBooking_status());

        String mobile = bookingData.getMobile();
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
                            .into(holder.imageView1);
                }
            }).addOnFailureListener(e -> {
                // Handle errors (e.g., file not found or permission issues)
                if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageLoaded[0]) {
                    // Show an error message if no image was found
                    Toast.makeText(holder.itemView.getContext(), "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.logo_splash) // Replace with your default image resource
                            .into(holder.imageView1);
                }
            });

            if (imageLoaded[0]) {
                break; // Stop checking other extensions if the image is loaded
            }
        }
//load profile image

        holder.imageViewPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), mobile, Toast.LENGTH_SHORT).show();

                if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) view.getContext(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    Uri uri = Uri.parse("tel:" + mobile);
                    intent.setData(uri);
                    view.getContext().startActivity(intent);
                }
            }
        });

        holder.imageViewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SendMessageActivity.class);
                intent.putExtra("mobile",mobile);
                view.getContext().startActivity(intent);
            }
        });

        holder.imageViewLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bookingData.getLatitude().equals("0")&&bookingData.getLongitude().equals("0")){
                    CustomToast.cusErrorToast(view.getContext(),"Location noy found",false);
                }else {
                    Intent intent = new Intent(view.getContext(), MapActivity.class);
                    intent.putExtra("latitude",bookingData.getLatitude());
                    intent.putExtra("longitude",bookingData.getLongitude());
                    intent.putExtra("name",bookingData.getFname()+" "+bookingData.getLname());
                    intent.putExtra("category",bookingData.getService_category());
                    view.getContext().startActivity(intent);
                }
            }
        });

        holder.imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SellerSingleViewActivity.class);
                intent.putExtra("worker_id",bookingData.getWorker_id());
                view.getContext().startActivity(intent);
            }
        });

//        Accept Button
        holder.button1.setVisibility(View.INVISIBLE);
//        cancel
        holder.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Log.i("Log1","Delete");
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("booking")
                        .whereEqualTo("customer_id",bookingData.getCustomer_id())
                        .whereEqualTo("worker_id",bookingData.getWorker_id())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()){
                                    QuerySnapshot result = task.getResult();

                                    String id = result.getDocuments().get(0).getId();
                                    Log.i("Log1","booling"+id);

                                    firestore.collection("booking").document(id)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(view.getContext(), "Booking cancel successful", Toast.LENGTH_SHORT).show();
                                                    // Remove the deleted booking from the list
                                                    int position = holder.getAdapterPosition();
                                                    if (position != RecyclerView.NO_POSITION) {
                                                        bookingDataArrayList.remove(position);
                                                        notifyItemRemoved(position);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("Log1","Booking cancel fail");
                                                }
                                            });

                                }   else {
                                    Log.w("Firestore", "Error getting documents.", task.getException());
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Log1", "Error getting documents.");
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingDataArrayList.size();
    }

    static class BookingItemViewHolder extends RecyclerView.ViewHolder{

        TextView textView1;
        TextView textView2;
        TextView textView3;

        TextView textView4;
        TextView textView5;
        ImageView imageView1,imageViewPhone,imageViewMessage,imageViewLocation;
        Button button1,button2;


        public BookingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textViewSellerName1BookingItem);
            textView2=itemView.findViewById(R.id.textViewBookingItemDate);
            textView3=itemView.findViewById(R.id.textViewSellerRate1BookingItem);
            textView4 = itemView.findViewById(R.id.textViewSellerType1BookingItem);
            textView5 = itemView.findViewById(R.id.textViewBookingItemStatus1);
            imageView1 = itemView.findViewById(R.id.shapeImageSellerItemBookingItem1);
            button1 = itemView.findViewById(R.id.buttonAccept);
            button2 = itemView.findViewById(R.id.buttonCancel);
            imageViewPhone = itemView.findViewById(R.id.imageViewPhhone1BookingItem);
            imageViewMessage = itemView.findViewById(R.id.imageViewMessage1BookingItem);
            imageViewLocation = itemView.findViewById(R.id.imageViewLocation1BookingItem);
        }
    }
    public String getFormattedDate(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy 'at' HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }

}