package com.Azzadine.offline;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_SCREEN_DURATION = 3000; // 3 ثواني

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView videoView = findViewById(R.id.splashVideoView);

        // تحميل الفيديو من res/raw
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loading);
        videoView.setVideoURI(videoUri);

        // تكرار الفيديو
        videoView.setOnPreparedListener(mediaPlayer -> mediaPlayer.setLooping(true));

        // تشغيل الفيديو
        videoView.start();

        // الانتقال إلى FirstActivity بعد المدة المحددة
        new Handler().postDelayed(() -> {
            // تم حذف الرسالة هنا
            if (Constants.ad_status == null || Constants.ad_status.isEmpty()) {
                // مجرد انتقال إلى الشاشة التالية بدون إظهار رسالة
                startActivity(new Intent(SplashActivity.this, FirstActivity.class));
                finish();
                return;
            }

            // في حال كانت الإعدادات محملة بشكل صحيح
            Intent intent = new Intent(SplashActivity.this, FirstActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_SCREEN_DURATION);
    }
}
