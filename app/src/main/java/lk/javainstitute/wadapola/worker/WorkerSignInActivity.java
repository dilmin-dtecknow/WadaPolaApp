package lk.javainstitute.wadapola.worker;

import static lk.javainstitute.wadapola.model.CustomToast.cusErrorToast;

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
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.SensorVerificationActivity;
import lk.javainstitute.wadapola.VerificationActivity;
import lk.javainstitute.wadapola.customer.HomeActivity;
import lk.javainstitute.wadapola.customer.SignInActivity;
import lk.javainstitute.wadapola.model.CustomerData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WorkerSignInActivity extends AppCompatActivity {
    private String logUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_worker_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        if (customer != null){
            Intent intent = new Intent(WorkerSignInActivity.this, WorkerHomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        EditText editText1 = findViewById(R.id.editTextSigninWorkerEmail1)
                ,editText2 = findViewById(R.id.signInPasswordWorker1);

        Button buttonLogin = findViewById(R.id.buttonLoginWorker1)
                ,buttonSignUp = findViewById(R.id.buttonWorkerSignin2),
                buttonCustommerSignIn= findViewById(R.id.buttonCustomerSignIn1);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                cusErrorToast("Hello",false);

                String emailSign = editText1.getText().toString();
                String password = editText2.getText().toString();
//
//
                if (emailSign.isBlank()){
                    cusErrorToast(WorkerSignInActivity.this,"please enter email",false);
                }else if (password.isBlank()){
                    cusErrorToast(WorkerSignInActivity.this,"please enter password",false);
                }else {

                    firestore.collection("worker")
                            .where(
                                    Filter.and(
                                            Filter.equalTo("email",emailSign),
                                            Filter.equalTo("password",password)
                                    )
                            )
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()){
                                        QuerySnapshot result = task.getResult();
                                        if (result.size() == 1){
                                            for (DocumentSnapshot document : result.getDocuments()) {
                                                document.getId();
                                                Log.i("Log1",String.valueOf(document.get("email")));
                                            }

                                            String verify_status = result.getDocuments().get(0).get("verify_status").toString();
                                            String logEmail = result.getDocuments().get(0).get("email").toString();
                                            logUserId = result.getDocuments().get(0).getId();
                                            String logMobile = result.getDocuments().get(0).get("mobile").toString();
                                            String logFname = result.getDocuments().get(0).get("fname").toString();
                                            String logLname = result.getDocuments().get(0).get("lname").toString();
                                            String logStaus = result.getDocuments().get(0).get("status").toString();
                                            if (verify_status.equals("Verified")){
                                                CustomerData customerData = new CustomerData(logUserId,logEmail,logMobile,logFname,logLname,logStaus,
                                                        verify_status);
                                                Gson gson = new Gson();
                                                String customerJson = gson.toJson(customerData);

                                                SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("customer",customerJson);
                                                editor.putString("type","worker");//add new 02/12 9.23 am
                                                editor.apply();

                                                Intent intent = new Intent(WorkerSignInActivity.this, SensorVerificationActivity.class);
                                                intent.putExtra("logType","Worker");
                                                startActivity(intent);

                                                Toast.makeText(WorkerSignInActivity.this,"Done", Toast.LENGTH_SHORT).show();



                                            }else {
//                                            not verified
                                                Toast.makeText(WorkerSignInActivity.this,verify_status, Toast.LENGTH_SHORT).show();
//                                            Intent intent = new Intent();
                                                Gson gson = new Gson();
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        OkHttpClient okHttpClient = new OkHttpClient();
                                                        Request request = new Request.Builder()
                                                                .url("https://2b1b-103-247-50-170.ngrok-free.app/WadaPolaServer/MailVerification?email="+emailSign)
                                                                .build();

                                                        try {
                                                            Response response = okHttpClient.newCall(request).execute();
                                                            String responseText = response.body().string();
                                                            Log.i("Log1",responseText);

                                                            JsonObject jsonObject = JsonParser.parseString(responseText).getAsJsonObject();
                                                            String status = jsonObject.get("status").getAsString();

//                                                        check response
                                                            if ("success".equals(status)) {

                                                                int verificationCode = jsonObject.get("verification_code").getAsInt();
                                                                String stringCode = String.valueOf(verificationCode);
                                                                runOnUiThread(() -> {
//                                                                Toast.makeText(SignInActivity.this, "Verification Code: " + verificationCode, Toast.LENGTH_LONG).show();
                                                                    HashMap<String,Object> document = new HashMap<>();
                                                                    document.put("verify_status",stringCode);
                                                                    firestore.collection("worker").document(logUserId).update(document)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    Intent intent = new Intent(WorkerSignInActivity.this, VerificationActivity.class);
                                                                                    intent.putExtra("user_id",logUserId);
                                                                                    intent.putExtra("logType","Worker");
                                                                                    startActivity(intent);
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {

                                                                                }
                                                                            });
                                                                });
                                                            } else {
                                                                String message = jsonObject.get("message").getAsString();
                                                                runOnUiThread(() -> {
                                                                    Toast.makeText(WorkerSignInActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                                });
                                                            }

                                                        } catch (IOException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    }
                                                }).start();
                                            }

                                        }else {
//                                        CustomToast.cusErrorToast(SignInActivity.this,"Invalid Details",false);
                                            Toast.makeText(WorkerSignInActivity.this,"Invalid details", Toast.LENGTH_SHORT).show();
                                        }

                                    }else {
//                                    CustomToast.cusErrorToast(SignInActivity.this,"Some thing went wrong",false);
                                        Toast.makeText(WorkerSignInActivity.this, "some thing went wrong", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(WorkerSignInActivity.this,"some thing went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }
        });
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkerSignInActivity.this,WorkerSignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonCustommerSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkerSignInActivity.this,SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}