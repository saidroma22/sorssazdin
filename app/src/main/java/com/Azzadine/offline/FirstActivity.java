package com.Azzadine.offline;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import android.util.Log;

public class FirstActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_more_apps, btn_rate, btn_privacy, btn_skip;
    private ConsentInformation consentInformation;
    private ConsentForm consentForm;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;
    private static final String PREFS_NAME = "gdpr_prefs";
    private static final String PREFS_CONSENT_SHOWN = "consent_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        requestNotificationPermission();
        initConsentForm();
        initAds();
        setupButtons();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    private void initConsentForm() {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                () -> {
                    if (consentInformation.isConsentFormAvailable()) {
                        loadForm();
                    } else {
                        Log.d("GDPR", "نموذج الموافقة غير متوفر في منطقتك. لا حاجة لعرضه.");
                    }
                },
                formError -> {
                    Log.e("GDPR", "فشل تحديث معلومات الموافقة: " + formError.getMessage());
                }
        );
    }

    public void loadForm() {
        UserMessagingPlatform.loadConsentForm(
                this,
                consentForm -> {
                    FirstActivity.this.consentForm = consentForm;

                    boolean alreadyShown = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .getBoolean(PREFS_CONSENT_SHOWN, false);

                    if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED && !alreadyShown) {
                        consentForm.show(
                                FirstActivity.this,
                                formError -> {
                                    if (formError != null) {
                                        Log.e("GDPR", "فشل عرض نموذج الموافقة: " + formError.getMessage());
                                    }

                                    if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {
                                        Log.d("GDPR", "تم الحصول على موافقة المستخدم.");
                                    }

                                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                                            .edit()
                                            .putBoolean(PREFS_CONSENT_SHOWN, true)
                                            .apply();
                                });
                    } else {
                        Log.d("GDPR", "لا حاجة لعرض نموذج الموافقة.");
                    }
                },
                formError -> {
                    Log.e("GDPR", "فشل تحميل نموذج الموافقة: " + formError.getMessage());
                }
        );
    }

    private void initAds() {
        AdsManager.getInstance().init(this, Constants.ad_banner);
        AdsManager.getInstance().init(this, Constants.ad_inter);
        AdsManager.getInstance().init(this, Constants.ad_native);
        AdsManager.getInstance().loadNativeAd(this, Constants.ad_native);
        AdsManager.getInstance().loadInterstitialAd(this, Constants.ad_inter);
    }

    private void setupButtons() {
        btn_more_apps = findViewById(R.id.btn_more_apps);
        btn_rate = findViewById(R.id.btn_rate);
        btn_privacy = findViewById(R.id.btn_privacy);
        btn_skip = findViewById(R.id.btn_skip);

        btn_more_apps.setOnClickListener(this);
        btn_rate.setOnClickListener(this);
        btn_privacy.setOnClickListener(this);
        btn_skip.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_more_apps:
                openUrl("https://play.google.com/store/apps/developer?id=khalaf+said");
                break;
            case R.id.btn_rate:
                openUrl("https://play.google.com/store/apps/details?id=com.Azzadine.offline");
                break;
            case R.id.btn_privacy:
                openUrl("https://sites.google.com/view/azzadine-offline");
                break;
            case R.id.btn_skip:
                startActivity(new Intent(FirstActivity.this, MainActivity.class));
                AdsManager.getInstance().showInterstitialAd();
                finish();
                break;
        }
    }



    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "تم تفعيل الإشعارات بنجاح", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "لم يتم منح إذن الإشعارات", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
