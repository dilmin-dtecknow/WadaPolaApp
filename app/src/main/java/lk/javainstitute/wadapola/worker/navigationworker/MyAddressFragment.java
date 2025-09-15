package lk.javainstitute.wadapola.worker.navigationworker;

import static lk.javainstitute.wadapola.model.CustomToast.cusErrorToast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.customer.MyProfileActivity;
import lk.javainstitute.wadapola.customer.SignUp2Activity;
import lk.javainstitute.wadapola.model.CustomerData;


public class MyAddressFragment extends Fragment {

    private String log_userid;
    private String addressId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_address, container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        log_userid = customerData.getId();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        EditText editTextStreet = view.findViewById(R.id.editTextMyStreet1)
                ,editTextLine1  = view.findViewById(R.id.editTextMyLine1)
                ,editTextLine2 = view.findViewById(R.id.editTextMyLine2)
                ,editTextCity = view.findViewById(R.id.editTextMyCity1)
                ,editTextZipCode = view.findViewById(R.id.editTextMyZipCode1);

        Button updateBtn = view.findViewById(R.id.buttonAddUp);

        Spinner spinner2 = view.findViewById(R.id.spinnerMyProvince);

        List<String> provinceList = new ArrayList<>();
        provinceList.add("Select Province");

//        load province
        firestore.collection("province").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot document: task.getResult()){
                                provinceList.add(document.getString("province_name"));
                            }
                        }else {
                            cusErrorToast(getContext(),"Something went wrong",false);
                        }
                    }
                });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.custom_province_spinner_item,
                provinceList
        );

        spinner2.setAdapter(arrayAdapter);

        firestore.collection("address")
                .whereEqualTo("user_id",log_userid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.isEmpty()){
                            DocumentSnapshot documentSnapshot = value.getDocuments().get(0);
                            String street = documentSnapshot.getString("street");
                            String line1 = documentSnapshot.getString("line1");
                            String line2 = documentSnapshot.getString("line2");
                            String city = documentSnapshot.getString("city");
                            String zipCode = documentSnapshot.getString("zip_code");
                            String province = documentSnapshot.getString("province");
                            addressId = documentSnapshot.getId();

                            editTextStreet.setText(street);
                            editTextCity.setText(city);
                            editTextLine1.setText(line1);
                            editTextLine2.setText(line2);
                            editTextZipCode.setText(zipCode);

                            Log.i("Log1","Street:"+street+"Line1: "+line1+"Line2 :"+line2+"City: "+
                                    city+"zip Code: "+zipCode+"province:"+province+"log user"+log_userid);

                            spinner2.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    int position = provinceList.indexOf(province);
                                    if (position != -1) {
                                        spinner2.setSelection(position);
                                    }
                                }
                            }, 500);

                        }    else {
                            cusErrorToast(getContext(),"no result found",false);
                        }
                    }
                });


        //update
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String street = editTextStreet.getText().toString();
                String line1 = editTextLine1.getText().toString();
                String line2 = editTextLine2.getText().toString();
                String city = editTextCity.getText().toString();
                String zipCode = editTextZipCode.getText().toString();
                String province = String.valueOf(spinner2.getSelectedItem());
                if (street.isBlank()){
                    cusErrorToast(getContext(),"street is empty",false);
                } else if (line1.isBlank()) {
                    cusErrorToast(getContext(),"line1 is empty",false);
                } else if (line2.isBlank()) {
                    cusErrorToast(getContext(),"line2 is empty",false);
                } else if (city.isBlank()) {
                    cusErrorToast(getContext(),"city is empty",false);
                } else if (province.equals("Select Province")) {
                    cusErrorToast(getContext(),"Please Select province",false);
                } else if (zipCode.isBlank()) {
                    cusErrorToast(getContext(),"zip code is empty",false);
                } else if (zipCode.length() < 5) {
                    cusErrorToast(getContext(),"invalide Zip code",false);
                } else {
                    HashMap<String,Object> document = new HashMap<>();
                    document.put("street",street);
                    document.put("line1",line1);
                    document.put("line2",line2);
                    document.put("city",city);
                    document.put("province",province);
                    document.put("zip_code",zipCode);

                    firestore.collection("address").document(addressId)
                            .update(document)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                   cusErrorToast(getContext(),"Address update success",true);
                                   sendUpdateNotification();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "address update fail", Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            }
        });
        return view;
    }

    private void sendUpdateNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "BOOKING_CHANNEL")
                .setSmallIcon(R.drawable.logo_splash)
                .setContentTitle("New Update details")
                .setContentText("You have a Successfully update Address.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("BOOKING_CHANNEL", "Booking Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1, builder.build());
    }
}