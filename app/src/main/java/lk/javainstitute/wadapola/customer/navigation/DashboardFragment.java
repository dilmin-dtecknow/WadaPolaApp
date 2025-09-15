package lk.javainstitute.wadapola.customer.navigation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import lk.javainstitute.wadapola.MapActivity;
import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.SendMessageActivity;
import lk.javainstitute.wadapola.customer.SellerSingleViewActivity;
import lk.javainstitute.wadapola.model.CustomToast;
import lk.javainstitute.wadapola.model.TestData;
import lk.javainstitute.wadapola.model.WorkerData;

public class DashboardFragment extends Fragment {
    private ArrayList<WorkerData> workerDataArrayList = new ArrayList<>(); // Initialize here
    private RecyclerView recyclerView1;
    private SellerItemAdapter sellerItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        EditText searchText = view.findViewById(R.id.editTextTextDashSearch1);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                fireBaseData(charSequence.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        fireBaseData("");
        recyclerView1 = view.findViewById(R.id.recyclerViewDash1);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView1.setLayoutManager(linearLayoutManager);

        // Add the ItemDecoration to the RecyclerView
        int verticalSpaceHeight = getResources().getDimensionPixelSize(R.dimen.vertical_space_height);
        recyclerView1.addItemDecoration(new VerticalSpaceItemDecoration(verticalSpaceHeight));

        // Pass the initialized workerDataArrayList
        sellerItemAdapter = new SellerItemAdapter(workerDataArrayList, new OnItemClickListener() {
            @Override
            public void onItemClick(WorkerData item) {
                Toast.makeText(getContext(), "Selected: " + item.getEmail(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), SellerSingleViewActivity.class);
                intent.putExtra("worker_id",item.getId());
                startActivity(intent);
            }
        });
        recyclerView1.setAdapter(sellerItemAdapter);

        //province load
        List<String> provinStringList = new ArrayList<>();
        provinStringList.add("Select Province");
        Spinner spinnerProvince = view.findViewById(R.id.spinnerDashProvince1);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("province").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()){
                                provinStringList.add(document.getString("province_name"));
                            }
                        }else {
                            Toast.makeText(getContext(), "Load fail province", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                provinStringList
        );

        spinnerProvince.setAdapter(arrayAdapter);

//        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                String selectedProvince = provinStringList.get(i);
//                if (!selectedProvince.equals("Select Province")){
//                    searchData(selectedProvince);
//                }else {
////                    fireBaseData();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        //category load

        ArrayList<String> categoryList = new ArrayList<>();
        categoryList.add("Select Category");

        firestore.collection("service_category").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()){
                            Toast.makeText(getContext(), "Category load not found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        for (QueryDocumentSnapshot document : task.getResult()){
                            categoryList.add(document.getString("name"));
                        }
                    }
                });

        Spinner spinnerCategory = view.findViewById(R.id.spinnerDashCategory1);
        ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<>(
                getContext(),
                R.layout.custom_province_spinner_item,
                categoryList
        );

        spinnerCategory.setAdapter(arrayAdapter1);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedProvince = spinnerProvince.getSelectedItem().toString();
                String selectedCategory = categoryList.get(i);

                // Call search with the latest selection
                searchData(selectedProvince, selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedProvince = provinStringList.get(i);
                String selectedCategory = spinnerCategory.getSelectedItem().toString();

                // Call search with the latest selection
                searchData(selectedProvince, selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        return view;
    }
    private void fireBaseData(String text) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Query query = firestore.collection("worker");

        // Apply search filter if text is not empty
        if (!text.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("fname", text)
                    .whereLessThanOrEqualTo("fname", text + "\uf8ff");
        }

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    workerDataArrayList.clear(); // Clear previous results
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String id = document.getId();
                        String email = document.getString("email");
                        String mobile = document.getString("mobile");
                        String fname = document.getString("fname");
                        String lname = document.getString("lname");
                        String price = document.getString("price");
                        String serviceCategory = document.getString("service_category");
                        String verifyStatus = document.getString("verify_status");
                        String status = document.getString("status");
                        String working_status = document.getString("working_status");

                        if (!"Deactivate".equalsIgnoreCase(status) && !"Working".equals(working_status)) {
                            firestore.collection("address")
                                    .whereEqualTo("user_id", id)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                                String latitude = documentSnapshot.getString("latitude");
                                                String longitude = documentSnapshot.getString("longitude");
                                                String provinceName = documentSnapshot.getString("province");

                                                WorkerData workerData = new WorkerData(id, email, mobile, fname, lname, serviceCategory, verifyStatus, status, price, latitude, longitude, provinceName);
                                                workerDataArrayList.add(workerData);
                                                sellerItemAdapter.notifyDataSetChanged();
                                            } else {
                                                Log.i("Log1", "No matching address found.");
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Log1", "Something went wrong while loading Address.");
                                        }
                                    });
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Log1", "Something went wrong while fetching workers.");
            }
        });
    }

//    private void fireBaseData() {
//        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//
//        firestore.collection("worker").get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                String id = document.getId();
//                                String email = document.getString("email");
//                                String mobile = document.getString("mobile");
//                                String fname = document.getString("fname");
//                                String lname = document.getString("lname");
//                                String price = document.getString("price");
//                                String serviceCategory = document.getString("service_category");
//                                String verifyStatus = document.getString("verify_status");
//                                String status = document.getString("status");
//                                String working_status = document.getString("working_status");
//
//                                if (!"Deactivate".equalsIgnoreCase(status)&&!"Working".equals(working_status)) {
//                                    firestore.collection("address")
//                                            .whereEqualTo("user_id", id)
//                                            .get()
//                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                                                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
//                                                        String latitude = documentSnapshot.getString("latitude");
//                                                        String longitude = documentSnapshot.getString("longitude");
//                                                        String provinceName = documentSnapshot.getString("province");
//
//                                                        WorkerData workerData = new WorkerData(id, email, mobile, fname, lname, serviceCategory, verifyStatus, status, price, latitude, longitude,provinceName);
//                                                        workerDataArrayList.add(workerData);
//                                                        sellerItemAdapter.notifyDataSetChanged();
//                                                    } else {
//                                                        Log.i("Log1", "No matching address found.");
//                                                    }
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Log.i("Log1", "Something went wrong while loading Address.");
//                                                }
//                                            });
//                                }
//                            }
//                        } else {
//                            Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.i("Log1", "Something went wrong while fetching workers.");
//                    }
//                });
//    }
    private void searchData(String provinceName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        workerDataArrayList.clear(); // Clear previous data
    Log.i("Log1",provinceName);
        firestore.collection("worker").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String email = document.getString("email");
                                String mobile = document.getString("mobile");
                                String fname = document.getString("fname");
                                String lname = document.getString("lname");
                                String price = document.getString("price");
                                String serviceCategory = document.getString("service_category");
                                String verifyStatus = document.getString("verify_status");
                                String status = document.getString("status");
                                String working_status = document.getString("working_status");

                                if (!"Deactivate".equalsIgnoreCase(status) && !"Working".equals(working_status)) {
                                    firestore.collection("address")
                                            .whereEqualTo("user_id", id)
                                            .whereEqualTo("province", provinceName) // Filter by province
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                                                        String latitude = documentSnapshot.getString("latitude");
                                                        String longitude = documentSnapshot.getString("longitude");
                                                        String provinceName1 = documentSnapshot.getString("province");

                                                        WorkerData workerData = new WorkerData(id, email, mobile, fname, lname, serviceCategory, verifyStatus, status, price, latitude, longitude,provinceName1);
                                                        workerDataArrayList.add(workerData);
                                                        sellerItemAdapter.notifyDataSetChanged();
                                                    } else {
                                                        Log.i("Log1", "No matching address found in selected province.");
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("Log1", "Error loading Address.");
                                                }
                                            });
                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Log1", "Error fetching workers.");
                    }
                });
    }
    private void searchData(String provinceName, String categoryName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        workerDataArrayList.clear(); // Clear previous data
        Log.i("Log1", "Province: " + provinceName + ", Category: " + categoryName);

        CollectionReference workerRef = firestore.collection("worker");
        Query query = workerRef;

        // Apply filters dynamically
        if (!provinceName.equals("Select Province") && !categoryName.equals("Select Category")) {
            query = query.whereEqualTo("service_category", categoryName);
        } else if (!provinceName.equals("Select Province")) {
            // If only province is selected, retrieve all workers (filter by province later)
        } else if (!categoryName.equals("Select Category")) {
            // If only category is selected, filter by category
            query = query.whereEqualTo("service_category", categoryName);
        } else {
            // If nothing is selected, show all workers
        }

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String email = document.getString("email");
                            String mobile = document.getString("mobile");
                            String fname = document.getString("fname");
                            String lname = document.getString("lname");
                            String price = document.getString("price");
                            String serviceCategory = document.getString("service_category");
                            String verifyStatus = document.getString("verify_status");
                            String status = document.getString("status");
                            String working_status = document.getString("working_status");

                            if (!"Deactivate".equalsIgnoreCase(status) && !"Working".equals(working_status)) {
                                if (!provinceName.equals("Select Province")) {
                                    // Check province from "address" collection
                                    firestore.collection("address")
                                            .whereEqualTo("user_id", id)
                                            .whereEqualTo("province", provinceName)
                                            .get()
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                                                    DocumentSnapshot documentSnapshot = task1.getResult().getDocuments().get(0);
                                                    String latitude = documentSnapshot.getString("latitude");
                                                    String longitude = documentSnapshot.getString("longitude");
                                                    String provinceName1 = documentSnapshot.getString("province");

                                                    WorkerData workerData = new WorkerData(id, email, mobile, fname, lname, serviceCategory, verifyStatus, status, price, latitude, longitude, provinceName1);
                                                    workerDataArrayList.add(workerData);
                                                    sellerItemAdapter.notifyDataSetChanged();
                                                } else {
                                                    Log.i("Log1", "No matching address found in selected province.");
                                                }
                                            })
                                            .addOnFailureListener(e -> Log.i("Log1", "Error loading Address."));
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.i("Log1", "Error fetching workers."));
    }


}


//class SellerItemAdapter extends RecyclerView.Adapter<SellerItemAdapter.SellerItemViewHolder>{
//    class SellerItemViewHolder extends RecyclerView.ViewHolder{
//
//        TextView textView1;
//        TextView textView2;
//        TextView textView3;
//        ImageView imageViewPhone;
//        ImageView imageViewMessage;
//        ImageView imageViewLocation;
//
//
//        public SellerItemViewHolder(@NonNull View itemView) {
//            super(itemView);
//            textView1 = itemView.findViewById(R.id.textViewSellerName1);
//            textView2 = itemView.findViewById(R.id.textViewSellerType1);
//            textView3 = itemView.findViewById(R.id.textViewSellerRate1);
//            imageViewPhone = itemView.findViewById(R.id.imageViewPhhone1);
//            imageViewMessage = itemView.findViewById(R.id.imageViewMessage1);
//            imageViewLocation  = itemView.findViewById(R.id.imageViewLocation1);
//        }
//    }
//
//    public ArrayList<TestData> testDataArrayList;
//
//    public SellerItemAdapter(ArrayList<TestData> testDataArrayList) {
//        this.testDataArrayList = testDataArrayList;
//    }
//
//    @NonNull
//    @Override
//    public SellerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
//        View view = layoutInflater.inflate(R.layout.seller_items_card, parent, false);
//
//        SellerItemViewHolder sellerItemViewHolder = new SellerItemViewHolder(view);
//        return sellerItemViewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull SellerItemViewHolder holder, int position) {
//
//        holder.textView1.setText(testDataArrayList.get(position).getName());
//        holder.textView2.setText(testDataArrayList.get(position).getType());
////        holder.textView3.setText((int) testDataArrayList.get(position).getPrice());
//
//        holder.imageViewPhone.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(view.getContext(), "Phone Call", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return testDataArrayList.size();
//    }
//}


class SellerItemAdapter extends RecyclerView.Adapter<SellerItemAdapter.SellerItemViewHolder> {
    private ArrayList<WorkerData> testDataArrayList;
    private OnItemClickListener listener;

    public SellerItemAdapter(ArrayList<WorkerData> testDataArrayList, OnItemClickListener listener) {
        this.testDataArrayList = testDataArrayList;
        this.listener = listener;
    }

    class SellerItemViewHolder extends RecyclerView.ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3,textViewProvince;
        ImageView imageViewPhone;
        ImageView imageViewMessage;
        ImageView imageViewLocation;
        ImageView imageViewProfileImage;

        public SellerItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textView1 = itemView.findViewById(R.id.textViewSellerName1);
            textView2 = itemView.findViewById(R.id.textViewSellerType1);
            textView3 = itemView.findViewById(R.id.textViewSellerRate1);
            imageViewPhone = itemView.findViewById(R.id.imageViewPhhone1);
            imageViewMessage = itemView.findViewById(R.id.imageViewMessage1);
            imageViewLocation = itemView.findViewById(R.id.imageViewLocation1);
            imageViewProfileImage = itemView.findViewById(R.id.shapeImageSellerItem);
            textViewProvince = itemView.findViewById(R.id.textView19);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(testDataArrayList.get(position));
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public SellerItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.seller_items_card, parent, false);
        return new SellerItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerItemViewHolder holder, int position) {
        WorkerData workerData = testDataArrayList.get(position);
        holder.textView1.setText(workerData.getfName()+" "+workerData.getlName());
        holder.textView2.setText(workerData.getService_category());
         holder.textView3.setText("1h = Rs."+workerData.getPrice());

         holder.textViewProvince.setText(workerData.getProvince());

        String mobile = testDataArrayList.get(position).getMobile();
        String latitude = testDataArrayList.get(position).getLatitude();
        String longitude = testDataArrayList.get(position).getLongitude();
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
                            .into(holder.imageViewProfileImage);
                }
            }).addOnFailureListener(e -> {
                // Handle errors (e.g., file not found or permission issues)
                if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageLoaded[0]) {
                    // Show an error message if no image was found
                    Toast.makeText(holder.itemView.getContext(), "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Glide.with(holder.itemView.getContext())
                            .load(R.drawable.logo_splash) // Replace with your default image resource
                            .into(holder.imageViewProfileImage);
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
                if (latitude.equals("0")&&longitude.equals("0")){
                    CustomToast.cusErrorToast(view.getContext(),"Location noy found",false);
                }else {
                    Intent intent = new Intent(view.getContext(), MapActivity.class);
                    intent.putExtra("latitude",latitude);
                    intent.putExtra("longitude",longitude);
                    intent.putExtra("name",testDataArrayList.get(position).getfName()+" "+testDataArrayList.get(position).getlName());
                    intent.putExtra("category",testDataArrayList.get(position).getService_category());
                    view.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return testDataArrayList.size();
    }
}

