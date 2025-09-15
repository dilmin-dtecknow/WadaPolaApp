package lk.javainstitute.wadapola;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import lk.javainstitute.wadapola.customer.SignInActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageViewSplash1 = findViewById(R.id.imageViewSplash1);
        TextView textViewSplash1, textViewSplash2, textViewSplash3;
        textViewSplash2 = findViewById(R.id.textViewSplash2);
        textViewSplash1 = findViewById(R.id.textViewSplash1);
        textViewSplash3 = findViewById(R.id.textViewSplash3);

//        mainText
        Animation animation = AnimationUtils.loadAnimation(MainActivity.this,R.anim.splash_anim);
        textViewSplash1.startAnimation(animation);

//        fade in anim
        Animation animationFadeIn = AnimationUtils.loadAnimation(MainActivity.this,R.anim.fade_anim);
        textViewSplash2.startAnimation(animationFadeIn);
        textViewSplash3.startAnimation(animationFadeIn);

        SpringAnimation springAnimation = new SpringAnimation(imageViewSplash1, DynamicAnimation.TRANSLATION_Y);
        SpringForce springForce = new SpringForce();

        springForce.setStiffness(SpringForce.STIFFNESS_LOW);
        springForce.setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
        springForce.setFinalPosition(252f);

        springAnimation.setSpring(springForce);
        springAnimation.start();

        new Handler().postDelayed(()->{

//            FlingAnimation flingAnimation = new FlingAnimation(logoImageView,DynamicAnimation.ROTATION_X);
            FlingAnimation flingAnimation = new FlingAnimation(imageViewSplash1,DynamicAnimation.TRANSLATION_X);
            flingAnimation.setStartVelocity(-500f);
            flingAnimation.setFriction(0.2f);
            flingAnimation.start();

            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();

        },3000);


    }
}