package lk.javainstitute.wadapola.worker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import lk.javainstitute.wadapola.DeactivateActivity;
import lk.javainstitute.wadapola.R;
import lk.javainstitute.wadapola.customer.HomeActivity;
import lk.javainstitute.wadapola.customer.SignInActivity;
import lk.javainstitute.wadapola.customer.navigation.CusHomeFragment;
import lk.javainstitute.wadapola.customer.navigation.DashboardFragment;
import lk.javainstitute.wadapola.customer.navigation.MyBookingFragment;
import lk.javainstitute.wadapola.customer.navigation.MyLocationFragment;
import lk.javainstitute.wadapola.model.CustomerData;
import lk.javainstitute.wadapola.worker.navigationworker.HomeWorkerFragment;
import lk.javainstitute.wadapola.worker.navigationworker.MyAddressFragment;
import lk.javainstitute.wadapola.worker.navigationworker.WorkPaymentHistoryFragment;
import lk.javainstitute.wadapola.worker.navigationworker.WorkProofFragment;
import lk.javainstitute.wadapola.worker.navigationworker.WorkerBookingFragment;
import lk.javainstitute.wadapola.worker.navigationworker.WorkerProfileFragment;

public class WorkerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_worker_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayoutWorker1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        Log.i("Log1",customerData.getEmail()+" "+customerData.getStatus());

        if (customerData.getStatus().equals("Deactivate")){
            Intent intent = new Intent(WorkerHomeActivity.this, DeactivateActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutWorker1);
        Toolbar toolbar = findViewById(R.id.toolbarNavWorker1);
        NavigationView navigationView = findViewById(R.id.navigationViewWorker1);

        navigationView.setCheckedItem(R.id.nav_menu_worker_booking);
        loadFragment(new WorkerBookingFragment());
        toolbar.setSubtitle(navigationView.getMenu().findItem(R.id.nav_menu_worker_booking).getTitle());

        View headerView = navigationView.getHeaderView(0);
        ImageView imageView1 = headerView.findViewById(R.id.shapeable_img_cus_nav);
        TextView textViewEmail = headerView.findViewById(R.id.header_cus_text1);
        TextView textViewType = headerView.findViewById(R.id.header_cus_text2);

        textViewEmail.setText(customerData.getEmail());
        textViewType.setText("Worker");
        loadProfileImage(customerData.getMobile(),imageView1);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_menu_worker_booking){
                    Log.i("Log1","cahange");
                    loadFragment(new WorkerBookingFragment());
                } else if (item.getItemId() == R.id.nav_menu_worker_home) {
                    loadFragment(new HomeWorkerFragment());
                } else if (item.getItemId() == R.id.nav_menu_worker_location) {
                    loadFragment(new MyLocationFragment());
                } else if (item.getItemId() == R.id.nav_menu_worker_profile) {
                    loadFragment(new WorkerProfileFragment());
                } else if (item.getItemId() == R.id.nav_menu_worker_proof) {
                    loadFragment(new WorkProofFragment());
                } else if (item.getItemId() == R.id.nav_menu_worker_payment_history) {
                    loadFragment(new WorkPaymentHistoryFragment());
                } else if (item.getItemId() == R.id.nav_menu_worker_home_address) {
                    loadFragment(new MyAddressFragment());
                } else if (item.getItemId() == R.id.nav_menu_worker_log_out) {
// Clear SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Navigate to Login Activity
                    Intent intent = new Intent(WorkerHomeActivity.this, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears activity stack
                    startActivity(intent);
                    finish();
                }

                toolbar.setSubtitle(item.getTitle());
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container_view_nav_worker1,fragment,null)
                .setReorderingAllowed(true)
                .commit();

    }

    private void loadProfileImage(String mobile, ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String[] fileExtensions = {"png", "jpg", "jpeg"};
        final boolean[] imageLoaded = {false};

        for (String ext : fileExtensions) {
            String filePath = "images/worker/profileImg/" + mobile+"."+ext;
            StorageReference imageRef = storageReference.child(filePath);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (!imageLoaded[0]) {
                    imageLoaded[0] = true; // Set the flag to true when image is loaded

                    // Use the Glide library to load the image into the ImageView
//                    ImageView imageView = findViewById(R.id.logoImageViewSingle1);
                    Glide.with(WorkerHomeActivity.this)
                            .load(uri) // Load the image from Firebase
                            .into(imageView);
                }
            }).addOnFailureListener(e -> {
                // Handle errors (e.g., file not found or permission issues)
                if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageLoaded[0]) {
                    // Show an error message if no image was found
                    Toast.makeText(WorkerHomeActivity.this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            if (imageLoaded[0]) {
                break; // Stop checking other extensions if the image is loaded
            }
        }
    }
}