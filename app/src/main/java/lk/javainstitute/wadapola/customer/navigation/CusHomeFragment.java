package lk.javainstitute.wadapola.customer.navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.model.CustomerData;

public class CusHomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cus_home, container, false);

        ImageSlider imageSlider = view.findViewById(R.id.image_slider_cus_home1);
        ArrayList<SlideModel> slideModelArrayList = new ArrayList<>();

        slideModelArrayList.add(new SlideModel(R.drawable.s1, ScaleTypes.FIT));
        slideModelArrayList.add(new SlideModel(R.drawable.s2, ScaleTypes.FIT));
        slideModelArrayList.add(new SlideModel(R.drawable.s3, ScaleTypes.FIT));
        slideModelArrayList.add(new SlideModel(R.drawable.s4, ScaleTypes.FIT));
        slideModelArrayList.add(new SlideModel(R.drawable.s5, ScaleTypes.FIT));
        slideModelArrayList.add(new SlideModel(R.drawable.s6, ScaleTypes.FIT));

        imageSlider.setImageList(slideModelArrayList,ScaleTypes.FIT);

        TextView workerCount = view.findViewById(R.id.textViewWorkerCount);
        TextView serviceCount = view.findViewById(R.id.textViewServicesCount);
        TextView textViewName = view.findViewById(R.id.textView12);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        String fName = customerData.getfName();
        String lName = customerData.getlName();

        textViewName.setText(fName+" "+lName);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("worker").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            int size = task.getResult().size();
                            workerCount.setText(String.valueOf(size));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Log1","worker search fail");
                    }
                });

        firebaseFirestore.collection("service_category").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            int size = task.getResult().size();
                            serviceCount.setText(String.valueOf(size));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Log1","Service search fail");
                    }
                });


        return view;
    }
}