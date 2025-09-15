package lk.javainstitute.wadapola.customer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import lk.javainstitute.wadapola.customer.navigation.CusHomeFragment;
import lk.javainstitute.wadapola.customer.navigation.DashboardFragment;
import lk.javainstitute.wadapola.customer.navigation.MyBookingFragment;
import lk.javainstitute.wadapola.customer.navigation.MyContactsFragment;
import lk.javainstitute.wadapola.customer.navigation.MyLocationFragment;
import lk.javainstitute.wadapola.customer.navigation.MyPaymentHistoryFragment;
import lk.javainstitute.wadapola.model.CustomerData;
import lk.javainstitute.wadapola.model.SQLiteHelper;
import lk.javainstitute.wadapola.worker.WorkerHomeActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayoutCus1), (v, insets) -> {
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
            Intent intent = new Intent(HomeActivity.this, DeactivateActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayoutCus1);
        Toolbar toolbar = findViewById(R.id.toolbarNavCus1);
        NavigationView navigationView = findViewById(R.id.navigationViewCus1);

        navigationView.setCheckedItem(R.id.nav_menu_cus_dashboard);
        loadFragment(new DashboardFragment());
        toolbar.setSubtitle(navigationView.getMenu().findItem(R.id.nav_menu_cus_dashboard).getTitle());

        View headerView = navigationView.getHeaderView(0);
        ImageView imageView1 = headerView.findViewById(R.id.shapeable_img_cus_nav);
        TextView textViewEmail = headerView.findViewById(R.id.header_cus_text1);
        TextView textViewType = headerView.findViewById(R.id.header_cus_text2);

        textViewEmail.setText(customerData.getEmail());
        textViewType.setText("Customer");
        loadProfileImage(customerData.getMobile(),imageView1);

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this,MyProfileActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.nav_menu_cus_dashboard){
                    Log.i("Log1","cahange");
                    loadFragment(new DashboardFragment());
                } else if (item.getItemId() == R.id.nav_menu_cus_booking) {
                    loadFragment(new MyBookingFragment());
                } else if (item.getItemId() == R.id.nav_menu_cus_home) {
                    loadFragment(new CusHomeFragment());
                } else if (item.getItemId() == R.id.nav_menu_cus_location) {
                    loadFragment(new MyLocationFragment());
                } else if (item.getItemId() == R.id.nav_menu_cus_payment_history) {
                    loadFragment(new MyPaymentHistoryFragment());
                } else if (item.getItemId() == R.id.nav_menu_cus_contact) {
                    loadFragment(new MyContactsFragment());
                } else if (item.getItemId() == R.id.nav_menu_cus_log_out) {
                    // Clear SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Clear SQLite Database
                    SQLiteHelper sqLiteHelper = new SQLiteHelper(HomeActivity.this, "mycontact.db", null, 1);
                    SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
                    db.delete("contact", null, null); // Delete all rows from the contact table
                    db.close(); // Close database

                    // Navigate to Login Activity
                    Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
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

        fragmentTransaction.replace(R.id.fragment_container_view_nav_cus1,fragment,null)
                .setReorderingAllowed(true)
                .commit();

    }

    private void loadProfileImage(String mobile,ImageView imageView) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();

        String[] fileExtensions = {"png", "jpg", "jpeg"};
        final boolean[] imageLoaded = {false};

        for (String ext : fileExtensions) {
            String filePath = "images/profileImg/" + mobile;
            StorageReference imageRef = storageReference.child(filePath);

            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (!imageLoaded[0]) {
                    imageLoaded[0] = true; // Set the flag to true when image is loaded

                    // Use the Glide library to load the image into the ImageView
//                    ImageView imageView = findViewById(R.id.logoImageViewSingle1);
                    Glide.with(HomeActivity.this)
                            .load(uri) // Load the image from Firebase
                            .into(imageView);
                }
            }).addOnFailureListener(e -> {
                // Handle errors (e.g., file not found or permission issues)
                if (ext.equals(fileExtensions[fileExtensions.length - 1]) && !imageLoaded[0]) {
                    // Show an error message if no image was found
                    Toast.makeText(HomeActivity.this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            if (imageLoaded[0]) {
                break; // Stop checking other extensions if the image is loaded
            }
        }
    }
}