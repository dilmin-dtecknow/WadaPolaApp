package lk.javainstitute.wadapola;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import lk.javainstitute.wadapola.model.CustomerData;

public class VerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String userId = intent.getStringExtra("user_id");
        String logType = intent.getStringExtra("logType");
//        String userId = "12iotD6b6QHmZ2FKFsin";

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Log.i("Log1", userId);

        EditText codeEditText = findViewById(R.id.editTextcode);

        Button buttonVerification = findViewById(R.id.buttonVerification1);
        buttonVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = codeEditText.getText().toString();

                if (code.isEmpty()) {
                    Toast.makeText(VerificationActivity.this, "Please Enter Verification code", Toast.LENGTH_SHORT).show();
                } else {

                    if (logType.equals("Customer")) {


                        firestore.collection("customer").document(userId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String storedCode = document.getString("verify_status");

//                                            update verify status
                                                if (storedCode != null && storedCode.equals(code)) {
                                                    HashMap<String, Object> updatedDocument = new HashMap<>();
                                                    updatedDocument.put("verify_status", "Verified");
                                                    firestore.collection("customer").document(userId)
                                                            .update(updatedDocument)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(VerificationActivity.this, "Verification Successful!", Toast.LENGTH_SHORT).show();
                                                                    // Retrieve the updated document
                                                                    firestore.collection("customer").document(userId)
                                                                            .get()
                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                    if (documentSnapshot.exists()) {
                                                                                        // Handle the updated document
                                                                                        Map<String, Object> documentData = documentSnapshot.getData();
                                                                                        // For example, retrieve the updated verify_status
                                                                                        String verifyStatus = (String) documentData.get("verify_status");
                                                                                        String logEmail = (String) documentData.get("email");
                                                                                        String logMobile = (String) documentData.get("mobile");
                                                                                        String logFname = (String) documentData.get("fname");
                                                                                        String logLname = (String) documentData.get("lname");
                                                                                        String logStaus = (String) documentData.get("status");

                                                                                        CustomerData customerData = new CustomerData(userId, logEmail, logMobile, logFname, logLname, logStaus, verifyStatus);
                                                                                        Gson gson = new Gson();
                                                                                        String customerJson = gson.toJson(customerData);

                                                                                        SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
                                                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                                        editor.putString("customer", customerJson);
                                                                                        editor.putString("type","customer");//add new 02/12 9.23 am
                                                                                        editor.apply();

                                                                                        Intent intent = new Intent(VerificationActivity.this, SensorVerificationActivity.class);
                                                                                        intent.putExtra("logType","Customer");
                                                                                        startActivity(intent);

                                                                                        Log.i("LogVerification", customerJson);
                                                                                        Log.d("Updated Document", "verify_status: " + verifyStatus);
                                                                                    }
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(VerificationActivity.this, "Failed to retrieve updated document", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(VerificationActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

//                                                Intent intent = new Intent(VerificationActivity.this, HomeActivity.class);
//                                                startActivity(intent);
                                                } else {
                                                    Toast.makeText(VerificationActivity.this, "Invalid Code!", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(VerificationActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(VerificationActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(VerificationActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }else {
                        firestore.collection("worker").document(userId)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String storedCode = document.getString("verify_status");

//                                            update verify status
                                                if (storedCode != null && storedCode.equals(code)) {
                                                    HashMap<String, Object> updatedDocument = new HashMap<>();
                                                    updatedDocument.put("verify_status", "Verified");
                                                    firestore.collection("worker").document(userId)
                                                            .update(updatedDocument)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(VerificationActivity.this, "Verification Successful!", Toast.LENGTH_SHORT).show();
                                                                    // Retrieve the updated document
                                                                    firestore.collection("worker").document(userId)
                                                                            .get()
                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                    if (documentSnapshot.exists()) {
                                                                                        // Handle the updated document
                                                                                        Map<String, Object> documentData = documentSnapshot.getData();
                                                                                        // For example, retrieve the updated verify_status
                                                                                        String verifyStatus = (String) documentData.get("verify_status");
                                                                                        String logEmail = (String) documentData.get("email");
                                                                                        String logMobile = (String) documentData.get("mobile");
                                                                                        String logFname = (String) documentData.get("fname");
                                                                                        String logLname = (String) documentData.get("lname");
                                                                                        String logStaus = (String) documentData.get("status");

                                                                                        CustomerData customerData = new CustomerData(userId, logEmail, logMobile, logFname, logLname, logStaus, verifyStatus);
                                                                                        Gson gson = new Gson();
                                                                                        String customerJson = gson.toJson(customerData);

                                                                                        SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
                                                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                                        editor.putString("customer", customerJson);
                                                                                        editor.putString("type","worker");//add new 02/12 9.23 am
                                                                                        editor.apply();

                                                                                        Intent intent = new Intent(VerificationActivity.this, SensorVerificationActivity.class);
                                                                                        intent.putExtra("logType","Worker");
                                                                                        startActivity(intent);

                                                                                        Log.i("LogVerification", customerJson);
                                                                                        Log.d("Updated Document", "verify_status: " + verifyStatus);
                                                                                    }
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Toast.makeText(VerificationActivity.this, "Failed to retrieve updated document", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(VerificationActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

//                                                Intent intent = new Intent(VerificationActivity.this, HomeActivity.class);
//                                                startActivity(intent);
                                                } else {
                                                    Toast.makeText(VerificationActivity.this, "Invalid Code!", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(VerificationActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(VerificationActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(VerificationActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                }
//                Intent intent = new Intent(VerificationActivity.this, HomeActivity.class);
//                startActivity(intent);
            }
        });
    }
}