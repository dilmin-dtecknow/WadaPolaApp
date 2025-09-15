package lk.javainstitute.wadapola;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import lk.javainstitute.wadapola.model.CustomToast;

public class SendMessageActivity extends AppCompatActivity {

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_message);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String mobile = intent.getStringExtra("mobile");

        editText = findViewById(R.id.editTextTextSendMessage);
        Button buttonSenMsg = findViewById(R.id.buttonMessageSend);
        TextView textViewMobile = findViewById(R.id.textViewSendNumber);
        textViewMobile.setText(mobile);

        buttonSenMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                if (message.isBlank()){
                    CustomToast.cusErrorToast(SendMessageActivity.this,"Please enter message",false);
                    return;
                }

                String[] permissionArray = {Manifest.permission.SEND_SMS};

                if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("Log1", "Send SMS Permission Denied");
                    requestPermissions(permissionArray, 100);
                } else {
                    sendSMS(mobile, message);
                }

            }
        });


    }
    private void sendSMS(String mobile, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(mobile, null, message, null, null);
        editText.setText("");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == 100){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Log.i("Log1","Send msg Permission Granted");
                Log.i("Log1","Success");
            }else {
                Log.i("Log1","Send msg Permission Denied");
                Log.i("Log1","Error");
            }
        }
    }
}