package lk.javainstitute.wadapola.worker;

import static lk.javainstitute.wadapola.model.CustomToast.cusErrorToast;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.customer.SignUp2Activity;
import lk.javainstitute.wadapola.customer.SignUpActivity;
import lk.javainstitute.wadapola.model.CustomToast;
import lk.javainstitute.wadapola.model.ServiceCategoryData;
import lk.javainstitute.wadapola.model.Validations;

public class WorkerSignUpActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_worker_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signUpImage= findViewById(R.id.logoImageViewWorkerReg1);
        signUpImage.setVisibility(View.INVISIBLE);

        EditText editTextEmail = findViewById(R.id.editTextTextEmailWorkerReg1),
                editTextMobile = findViewById(R.id.editTextTextMobileWorkerReg1),
                editTextPassword = findViewById(R.id.editTextTextPasswordWorkerReg1),
                editTextConfirmPassword = findViewById(R.id.editTextTextWorkerRegConfPassword1),
                editTextFirstName = findViewById(R.id.editTextTextWorkerRegFname1),
                editTextLastName = findViewById(R.id.editTextTextWorkerRegLname1),
                editTextPrice = findViewById(R.id.editTextTextWorkerPrice1),
                editTextAbout = findViewById(R.id.editTextTextWorkerAbout1);

        editTextMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enteredMobile = charSequence.toString();
                if (!Validations.isMobileNumberValide(enteredMobile)){
                    cusErrorToast(WorkerSignUpActivity.this,"Please Enter valide mobile nuber after the\nselect image",false);
                }else {
                    signUpImage.setVisibility(View.VISIBLE);
                    cusErrorToast(WorkerSignUpActivity.this,"You can now choose image",true);
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

        Spinner spinnerCategory = findViewById(R.id.spinnerWorkerReg1);

        ArrayList<ServiceCategoryData> serviceCategoryDataArrayList = new ArrayList<>();
        serviceCategoryDataArrayList.add(new ServiceCategoryData("Select Service Category",R.drawable.auction));

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("service_category").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (!task.isSuccessful()){
                            cusErrorToast(WorkerSignUpActivity.this,"Some thing Happend",false);
                            return;
                        }

                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            Log.i("Log1", document.getString("name"));
                            String name = document.getString("name");
                            if (name.equals("Carpentry")) {
                                serviceCategoryDataArrayList.add(new ServiceCategoryData(name, R.drawable.carpenter));
                            } else if (name.equals("Masonry")) {
                                serviceCategoryDataArrayList.add(new ServiceCategoryData(name, R.drawable.mesonery));
                            } else if (name.equals("Plumbing")) {
                                serviceCategoryDataArrayList.add(new ServiceCategoryData(name, R.drawable.plumber));
                            } else if (name.equals("Electrical work")) {
                                serviceCategoryDataArrayList.add(new ServiceCategoryData(name, R.drawable.electrician));
                            } else if (name.equals("Cleaning")) {
                                serviceCategoryDataArrayList.add(new ServiceCategoryData(name, R.drawable.clener));
                            }
                        }

                        ServiceCategoryAdapter serviceCategoryAdapter = new ServiceCategoryAdapter(
                                WorkerSignUpActivity.this,
                                R.layout.service_category_spinner_item,
                                serviceCategoryDataArrayList
                        );

                        spinnerCategory.setAdapter(serviceCategoryAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Log1", "service category load fail");
                    }
                });


        TextView textViewLink = findViewById(R.id.textViewWorkerReg2);
        textViewLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkerSignUpActivity.this, SignUpActivity.class);
                startActivity(intent);

            }
        });

//        signUp
        Button buttonSignUp = findViewById(R.id.buttonWorkerSignUp1);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                mobile = editTextMobile.getText().toString();
                String password = editTextPassword.getText().toString();
                String confirmPassword = editTextConfirmPassword.getText().toString();
                String fName = editTextFirstName.getText().toString();
                String lName = editTextLastName.getText().toString();
                String about = editTextAbout.getText().toString();
                String price = editTextPrice.getText().toString();
                ServiceCategoryData service_category = (ServiceCategoryData) spinnerCategory.getSelectedItem();

                Log.i("Log1",service_category.getName());
                if (password.equals(confirmPassword)){
                    editTextConfirmPassword.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    editTextConfirmPassword.setCompoundDrawableTintList(ColorStateList.valueOf(Color.GREEN));
                }else {
                    editTextConfirmPassword.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    editTextConfirmPassword.setCompoundDrawableTintList(ColorStateList.valueOf(Color.RED));

                    cusErrorToast(WorkerSignUpActivity.this,"Dosen't match with password",false);
                    return;
                }

                if (email.isBlank()){
                    cusErrorToast(WorkerSignUpActivity.this,"Email Is Empty",false);
                } else if (!Validations.isEmailValide(email)) {
                    cusErrorToast(WorkerSignUpActivity.this,"Invalid Email address!",false);
                } else if (mobile.isBlank()) {
                    cusErrorToast(WorkerSignUpActivity.this,"Please enter your Mobile",false);
                } else if (!Validations.isMobileNumberValide(mobile)) {
                    cusErrorToast(WorkerSignUpActivity.this,"Invalid Mobile number.Please check! again",false);
                } else if (password.isBlank()) {
                    cusErrorToast(WorkerSignUpActivity.this,"Please enter password",false);
                } else if (!Validations.isPasswordValide(password)) {
                    cusErrorToast(WorkerSignUpActivity.this,"Please enter strong password ( password \"\n" +
                            "                    + \"containing atleast 1 lower case letter, 1 upper case letter, \"\n" +
                            "                    + \"number and one of the mentioned special characters and match 8 or more characters)",false);
                } else if (confirmPassword.isBlank()) {
                    cusErrorToast(WorkerSignUpActivity.this,"Please confirm the password",false);
                }  else if (fName.isBlank()) {
                    cusErrorToast(WorkerSignUpActivity.this,"please enter First Name",false);
                } else if (lName.isBlank()) {
                    cusErrorToast(WorkerSignUpActivity.this,"Please enter Last Name",false);
                } else if (price.isBlank()) {
                    cusErrorToast(WorkerSignUpActivity.this,"Please enter Price",false);
                } else if (Double.parseDouble(price) < 0) {
                    cusErrorToast(WorkerSignUpActivity.this,"Price must be greater than 0",false);
                } else if (about.isBlank()) {
                    cusErrorToast(WorkerSignUpActivity.this,"Please enter your about",false);
                } else if (service_category.getName().equals("Select Service Category")) {
                    cusErrorToast(WorkerSignUpActivity.this,"Please select your service category",false);
                } else {
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    firestore.collection("worker")
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
                                            cusErrorToast(WorkerSignUpActivity.this,"Email or Mobile number already exist!", false);
                                        } else {
                                            if (imageUri ==null){
                                                cusErrorToast(WorkerSignUpActivity.this,"Please select image!", false);
                                               return;
                                            }
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
                                            document.put("about",about);
                                            document.put("price",price);
                                            document.put("service_category",service_category.getName());
                                            document.put("working_status","search Work");
                                            firestore.collection("worker").add(document)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            String cus_id = documentReference.getId();
                                                            Log.i("Log1",cus_id+"worker Registered success");
                                                            Intent intent = new Intent(WorkerSignUpActivity.this,SignUp2Activity.class);
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
                                        cusErrorToast(WorkerSignUpActivity.this,"Error checking existing customer: " + task.getException(), false);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    cusErrorToast(WorkerSignUpActivity.this,"Error checking existing customer: " + e.getMessage(), false);
                                }
                            });

                }

            }
        });

        Button buttonSignInWorker = findViewById(R.id.buttonWorkerSigUp2);
        buttonSignInWorker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkerSignUpActivity.this,WorkerSignInActivity.class);
                startActivity(intent);

            }
        });

    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Display the selected image

            signUpImage.setImageURI(imageUri);

        }
    }


    private void uploadImageToFirebase() {
        if (imageUri != null) {
            // Get a reference to Firebase Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();

            // Create a unique file name
            String fileName = "images/worker/profileImg/" +mobile+".jpg";

            // Create a reference for the image
            StorageReference imageRef = storageReference.child(fileName);

            // Upload the image
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();
                                // Display the image URL
                                Toast.makeText(WorkerSignUpActivity.this, "Image URL: " + downloadUrl, Toast.LENGTH_LONG).show();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(WorkerSignUpActivity.this, "Failed to retrieve image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            })
                    )
                    .addOnFailureListener(e -> {
                        Toast.makeText(WorkerSignUpActivity.this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
        }
    }
}

class ServiceCategoryAdapter extends ArrayAdapter<ServiceCategoryData> {

    List<ServiceCategoryData> serviceCategoryData;
    int layout;

    public ServiceCategoryAdapter(@NonNull Context context, int resource, @NonNull List<ServiceCategoryData> objects) {
        super(context, resource, objects);
        this.serviceCategoryData = objects;
        this.layout = resource;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(layout, parent, false);

        ServiceCategoryData serviceCategoryData1 = serviceCategoryData.get(position);
        ImageView imageView = view.findViewById(R.id.imageViewServiceSpiner1);
        imageView.setImageResource(serviceCategoryData1.getResourceId());

        TextView textView1 =  view.findViewById(R.id.textViewSpinerService);
        textView1.setText(serviceCategoryData1.getName());

        return view;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(layout, parent, false);

        ServiceCategoryData serviceCategoryData1 = serviceCategoryData.get(position);
        ImageView imageView = view.findViewById(R.id.imageViewServiceSpiner1);
        imageView.setImageResource(serviceCategoryData1.getResourceId());

        TextView textView1 =  view.findViewById(R.id.textViewSpinerService);
        textView1.setText(serviceCategoryData1.getName());

        return view;
    }
}