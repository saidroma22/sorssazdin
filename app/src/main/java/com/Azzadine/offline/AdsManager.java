package com.Azzadine.offline;

import android.app.Activity;
import android.widget.LinearLayout;

import com.solodroid.ads.sdk.BuildConfig;
import com.solodroid.ads.sdk.format.AdNetwork;
import com.solodroid.ads.sdk.format.BannerAd;
import com.solodroid.ads.sdk.format.InterstitialAd;
import com.solodroid.ads.sdk.format.NativeAd;

import android.app.AlertDialog;

public class AdsManager {
    private static AdsManager instance;
    AdNetwork.Initialize adNetwork;
    BannerAd.Builder bannerAd;
    InterstitialAd.Builder interstitialAd;
    NativeAd.Builder nativeAd;
    private AlertDialog loadingDialog;

    public AdsManager() {}

    public static AdsManager getInstance() {
        if (instance == null) {
            instance = new AdsManager();
        }
        return instance;
    }

    public void init(Activity activity, String ads) {
        adNetwork = new AdNetwork.Initialize(activity)
                .setAdStatus(Constants.ad_status)
                .setAdNetwork(ads)
                .setBackupAdNetwork(Constants.back_up)
                .setAdMobAppId(activity.getResources().getString(R.string.admob_app_id))
                .setStartappAppId(Constants.StartAppId)
                .setUnityGameId(Constants.IRONSOURCE_BANNER_ID)
                .setIronSourceAppKey(Constants.ironAppKey)
                .setDebug(BuildConfig.DEBUG)
                .build();
    }

    public void loadBannerAd(Activity activity, String ads) {
        bannerAd = new BannerAd.Builder(activity)
                .setAdStatus(Constants.ad_status)
                .setAdNetwork(ads)
                .setBackupAdNetwork(Constants.back_up)
                .setAdMobBannerId(Constants.adMobBannerId)
                .setGoogleAdManagerBannerId(Constants.gAdMangerBannerId)
                .setFanBannerId(Constants.fanBannerId)
                .setUnityBannerId("none")
                .setIronSourceBannerId(Constants.IRONSOURCE_BANNER_ID)
                .setDarkTheme(false)
                .build();
    }

    public void loadNativeAd(Activity activity, String ads) {
        LinearLayout placeHolder = activity.findViewById(R.id.ads);
        activity.getLayoutInflater().inflate(R.layout.nativeholder, placeHolder);
        nativeAd = new NativeAd.Builder(activity)
                .setAdStatus(Constants.ad_status)
                .setAdNetwork(ads)
                .setBackupAdNetwork(Constants.back_up)
                .setAdMobNativeId(Constants.adMobNativeId)
                .setAdManagerNativeId(Constants.gAdMangerNativeId)
                .setFanNativeId(Constants.fanNativeId)
                .setNativeAdStyle("default")
                .setDarkTheme(false)
                .build();
    }

    public void loadInterstitialAd(Activity activity, String ads) {
        interstitialAd = new InterstitialAd.Builder(activity)
                .setAdStatus(Constants.ad_status)
                .setAdNetwork(ads)
                .setBackupAdNetwork(Constants.back_up)
                .setAdMobInterstitialId(Constants.adMobInterstitialId)
                .setGoogleAdManagerInterstitialId(Constants.gAdMangerInterstitialId)
                .setFanInterstitialId(Constants.fanInterstitialId)
                .setUnityInterstitialId("none")
                .setIronSourceInterstitialId(Constants.IRONSOURCE_INTERSTITIAL_ID)
                .setInterval(1)
                .build();
    }

    public void showInterstitialAd() {
        if (interstitialAd != null)
            interstitialAd.show();
    }
}
