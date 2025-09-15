package lk.javainstitute.wadapola.customer;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.UUID;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.model.Validations;

public class SignUpActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int UC_REQUEST = 2;

    private Uri imageUri;
    private ImageView signUpImage;
    private String mobile;
    private String enteredMobile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button signUpButton1 = findViewById(R.id.buttonSignUp1);

        EditText editTextEmail = findViewById(R.id.editTextTextEmailSignup1)
                ,editTextMobile = findViewById(R.id.editTextTextMobileSignup1)
                ,editTextPassword = findViewById(R.id.editTextTextPassword1)
                ,editTextConfirmPassword = findViewById(R.id.editTextTextConfPassword1)
                ,editTextFname = findViewById(R.id.editTextTextFname1)
                ,editTextLname = findViewById(R.id.editTextTextLname1);

        signUpImage = findViewById(R.id.logoImageView1);
        signUpImage.setVisibility(View.INVISIBLE);

        editTextMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enteredMobile = charSequence.toString();
                if (!Validations.isMobileNumberValide(enteredMobile)){
                    cusErrorToast("Please Enter valide mobile nuber after the\nselect image",false);
                }else {
                    signUpImage.setVisibility(View.VISIBLE);
                    cusErrorToast("You can now choose image",true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });


        signUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openImagePicker();
            }
        });

        signUpButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = editTextEmail.getText().toString();
                mobile = editTextMobile.getText().toString();
                String password = editTextPassword.getText().toString();
                String fName = editTextFname.getText().toString();
                String lName = editTextLname.getText().toString();
                String conPassword = editTextConfirmPassword.getText().toString();

                if (password.equals(conPassword)){
                    editTextConfirmPassword.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    editTextConfirmPassword.setCompoundDrawableTintList(ColorStateList.valueOf(Color.GREEN));
                }else {
                    editTextConfirmPassword.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    editTextConfirmPassword.setCompoundDrawableTintList(ColorStateList.valueOf(Color.RED));

                    cusErrorToast("Dosen't match with password",false);
                    return;
                }

                if (email.isBlank()){
                    cusErrorToast("Email Is Empty",false);
                } else if (!Validations.isEmailValide(email)) {
                    cusErrorToast("Invalid Email address!",false);
                } else if (mobile.isBlank()) {
                    cusErrorToast("Please enter your Mobile",false);
                } else if (!Validations.isMobileNumberValide(mobile)) {
                    cusErrorToast("Invalid Mobile number.Please check! again",false);
                } else if (password.isBlank()) {
                    cusErrorToast("Please enter password",false);
                } else if (!Validations.isPasswordValide(password)) {
                    cusErrorToast("Please enter strong password ( password \"\n" +
                            "                    + \"containing atleast 1 lower case letter, 1 upper case letter, \"\n" +
                            "                    + \"number and one of the mentioned special characters and match 8 or more characters)",false);
                } else if (conPassword.isBlank()) {
                    cusErrorToast("Please confirm the password",false);
                }  else if (fName.isBlank()) {
                    cusErrorToast("please enter First Name",false);
                } else if (lName.isBlank()) {
                    cusErrorToast("Please enter Last Name",false);
                } else {

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    firestore.collection("customer")
                            .where(
                                    Filter.or(
                                            Filter.equalTo("email",email),
                                            Filter.equalTo("mobile",mobile)
                                    )
                            ).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        // Check if the query returned any documents
                                        if (!task.getResult().isEmpty()) {
                                            // Customer with the given email and mobile exists
                                            cusErrorToast("Email or Mobile number already exist!", false);
                                        } else {
                                            // Upload the image to Firebase
                                            uploadImageToFirebase();
                                            // Proceed with the sign-up logic
                                            // Your sign-up code here
//                                            cusErrorToast("Email or Mobile number Not already exist!", true);
                                            HashMap<String,Object> document = new HashMap<>();
                                            document.put("email",email);
                                            document.put("mobile",mobile);
                                            document.put("password",password);
                                            document.put("fname",fName);
                                            document.put("lname",lName);
                                            document.put("status","Deactivate");
                                            document.put("verify_status","not");
                                            firestore.collection("customer").add(document)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            String cus_id = documentReference.getId();
                                                            Log.i("Log1",cus_id+"User Registered success");
                                                            Intent intent = new Intent(SignUpActivity.this,SignUp2Activity.class);
                                                            intent.putExtra("reg_u_id",cus_id);
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("Log1","User Registered Unsuccessful");
                                                        }
                                                    });
                                        }
                                    } else {
                                        // Handle the error if the query was not successful
                                        cusErrorToast("Error checking existing customer: " + task.getException(), false);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    cusErrorToast("Error checking existing customer: " + e.getMessage(), false);
                                }
                            });

//                    Intent intent = new Intent(SignUpActivity.this, SignUp2Activity.class);
//                    startActivity(intent);
                }
            }
        });
        Button buttonSignin = findViewById(R.id.button4);
        buttonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this,SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Display the selected image

            signUpImage.setImageURI(imageUri);

        }
    }

    public Toast cusErrorToast(String msg,boolean isSuccess){
        //        Custom Toast
        if (isSuccess){
            Toast toast = new Toast(SignUpActivity.this);

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
            Toast toast = new Toast(SignUpActivity.this);

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

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            // Get a reference to Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            // Create a unique file name
            String fileName = "images/profileImg/" +mobile;

            // Create a reference for the image
            StorageReference imageRef = storageReference.child(fileName);

            // Upload the image
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();
                                // Display the image URL
                                Toast.makeText(SignUpActivity.this, "Image URL: " + downloadUrl, Toast.LENGTH_LONG).show();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(SignUpActivity.this, "Failed to retrieve image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                    )
                    .addOnFailureListener(e -> {
                        Toast.makeText(SignUpActivity.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }

}