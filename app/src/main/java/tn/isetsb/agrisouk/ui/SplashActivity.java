package tn.isetsb.agrisouk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import tn.isetsb.agrisouk.R;
import tn.isetsb.agrisouk.util.SessionManager;

/**
 * Écran d'ouverture : animation du logo puis redirection selon la session.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1300L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.ivSplashLogo);
        TextView name = findViewById(R.id.tvSplashName);

        logo.setAlpha(0f);
        logo.setScaleX(0.8f);
        logo.setScaleY(0.8f);
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        name.setAlpha(0f);
        name.setTranslationY(16f);
        name.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(250)
                .setDuration(500)
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(this::route, SPLASH_DELAY);
    }

    private void route() {
        SessionManager session = new SessionManager(this);
        Class<?> destination = session.isLoggedIn() ? MainActivity.class : LoginActivity.class;
        startActivity(new Intent(this, destination));
        finish();
    }
}
