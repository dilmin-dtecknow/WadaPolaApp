package lk.javainstitute.wadapola;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import lk.javainstitute.wadapola.customer.HomeActivity;
import lk.javainstitute.wadapola.model.CustomToast;
import lk.javainstitute.wadapola.model.CustomerData;
import lk.javainstitute.wadapola.worker.WorkerHomeActivity;

public class SensorVerificationActivity extends AppCompatActivity {
    private boolean isHomeActivityStarted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor_verification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("lk.javainstitute.wadapola.data", Context.MODE_PRIVATE);
        String customer = sharedPreferences.getString("customer", null);

        Gson gson = new Gson();
        CustomerData customerData = gson.fromJson(customer, CustomerData.class);

        TextView textView = findViewById(R.id.textViewSensorName1);
        textView.setText(customerData.getfName()+" "+customerData.getlName());

        Intent intent = getIntent();
        String logType = intent.getStringExtra("logType");

//        Sensor
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor : sensorList){
            Log.i("SensorLog",sensor.getName());
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            Log.i("Log1", "TYPE_ACCELEROMETER found");
            Sensor useSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            SensorEventListener listener = new SensorEventListener() {

                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    float values[] = sensorEvent.values;
                    Log.i("SensorLog", String.valueOf(values[0]));
                    // Check if the proximity sensor detects an object near the face and home activity hasn't started
                    if (sensorEvent.values[0] < useSensor.getMaximumRange() && !isHomeActivityStarted) {
                        isHomeActivityStarted = true; // Set the flag to true

                        NotificationManager notificationManager = getSystemService(NotificationManager.class);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(
                                    "default_channel_id",
                                    "Default Channel",
                                    NotificationManager.IMPORTANCE_DEFAULT
                            );
                            notificationManager.createNotificationChannel(channel);
                        }

                        Notification notification = new NotificationCompat.Builder(SensorVerificationActivity.this, "default_channel_id")
                                .setContentTitle("Wada Pola Success Notify")
                                .setContentText("You're successfully Verified")
                                .setSmallIcon(R.drawable.logo_splash)
                                .setPriority(Notification.PRIORITY_DEFAULT)
                                .build();

                        notificationManager.notify(1,notification);


//
                        if (logType.equals("Worker")){
                            // Navigate to the home activity
                            Intent intent = new Intent(SensorVerificationActivity.this, WorkerHomeActivity.class);
                            startActivity(intent);
                            finish(); // Optionally finish the current activity
                            return;
                        }
                        // Navigate to the home activity
                        Intent intent = new Intent(SensorVerificationActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Optionally finish the current activity
                    }else {
                        CustomToast.cusErrorToast(SensorVerificationActivity.this,"Please Verifiy",false);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };
            sensorManager.registerListener(listener,useSensor,SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.i("Log1", "TYPE_ACCELEROMETER Not found");
        }



    }
}