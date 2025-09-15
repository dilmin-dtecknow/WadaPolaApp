package lk.javainstitute.wadapola.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import lk.javainstitute.wadapola.R;

public class CustomToast {
    @SuppressLint("ResourceAsColor")
    public static Toast cusErrorToast(Context context, String msg, boolean isSuccess){
        //        Custom Toast
        if (isSuccess){
            Toast toast = new Toast(context);

            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View view = layoutInflater.inflate(R.layout.custom_toast_error, null, false);

            TextView textViewMsg = view.findViewById(R.id.textViewCusError1);
            ImageView typeImage = view.findViewById(R.id.imageViewError1);

            textViewMsg.setText(msg);
            typeImage.setImageResource(R.drawable.check);
            textViewMsg.setTextColor(ContextCompat.getColor(context, R.color.c1));

            toast.setView(view);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();

            return toast;

        }else {
            Toast toast = new Toast(context);

            LayoutInflater layoutInflater = LayoutInflater.from(context);
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

