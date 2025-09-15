package lk.javainstitute.wadapola.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.VerificationActivity;

public class SignUp2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Spinner spinner1 = findViewById(R.id.spinnerProvince1);

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
                            cusErrorToast("Something went wrong",false);
                        }
                    }
                });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                SignUp2Activity.this,
                R.layout.custom_province_spinner_item,
                provinceList
        );

        spinner1.setAdapter(arrayAdapter);

        Intent intent = getIntent();
        String reg_user_id = intent.getStringExtra("reg_u_id");
//        String reg_user_id = "12iotD6b6QHmZ2FKFsin";
        EditText editTextStreet = findViewById(R.id.editTextStreet1)
                ,editTextLine1 = findViewById(R.id.editTextLine1)
                ,editTextLine2 = findViewById(R.id.editTextLine2)
                ,editTextCity1 = findViewById(R.id.editTextCity1)
                ,editTextZipCode = findViewById(R.id.editTextZipCode1);




//Signup
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String street =  editTextStreet.getText().toString();
                String line1 =  editTextLine1.getText().toString();
                String line2 =  editTextLine2.getText().toString();
                String city =  editTextCity1.getText().toString();
                String zip =  editTextZipCode.getText().toString();
                String province = String.valueOf(spinner1.getSelectedItem());

                if (street.isBlank()){
                    cusErrorToast("street is empty",false);
                } else if (line1.isBlank()) {
                    cusErrorToast("line1 is empty",false);
                } else if (line2.isBlank()) {
                    cusErrorToast("line2 is empty",false);
                } else if (city.isBlank()) {
                    cusErrorToast("city is empty",false);
                } else if (province.equals("Select Province")) {
                    cusErrorToast("Please Select province",false);
                } else if (zip.isBlank()) {
                    cusErrorToast("zip code is empty",false);
                } else if (zip.length() < 5) {
                    cusErrorToast("invalide Zip code",false);
                } else {
                    Toast.makeText(SignUp2Activity.this, "Done", Toast.LENGTH_SHORT).show();

                    HashMap<String,Object> document = new HashMap<>();
                    document.put("street",street);
                    document.put("line1",line1);
                    document.put("line2",line2);
                    document.put("city",city);
                    document.put("province",province);
                    document.put("user_id",reg_user_id);
                    document.put("zip_code",zip);
                    document.put("latitude","0");
                    document.put("longitude","0");

                    firestore.collection("address").add(document)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.i("Log1","Address add success");
                                    Intent intent = new Intent(SignUp2Activity.this, SignInActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    cusErrorToast("Some thing went wrong",false);
                                }
                            });
                }

//                Intent intent = new Intent(SignUp2Activity.this, VerificationActivity.class);
//                startActivity(intent);
            }
        });
    }

    public Toast cusErrorToast(String msg, boolean isSuccess){
        //        Custom Toast
        if (isSuccess){
            Toast toast = new Toast(SignUp2Activity.this);

            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.custom_toast_error, null, false);

            TextView textViewMsg = view.findViewById(R.id.textViewCusError1);
            ImageView typeImage = view.findViewById(R.id.imageViewError1);

            textViewMsg.setText(msg);
            typeImage.setImageResource(R.drawable.check);

            toast.setView(view);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();

            return toast;

        }else {
            Toast toast = new Toast(SignUp2Activity.this);

            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.custom_toast_error, null, false);

            TextView textViewMsg = view.findViewById(R.id.textViewCusError1);
            ImageView typeImage = view.findViewById(R.id.imageViewError1);

            textViewMsg.setText(msg);

            toast.setView(view);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();

            return toast;
        }
    }
}