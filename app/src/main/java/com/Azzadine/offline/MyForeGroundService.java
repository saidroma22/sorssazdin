package com.Azzadine.offline;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import java.io.IOException;

import com.Azzadine.offline.dummy.DummyContent;

/**
 * خدمة تشغيل الصوت في الخلفية باستخدام Foreground Service.
 * تتعامل مع الإشعارات، التحكم بالميديا، والتحكم الصوتي.
 */
public class MyForeGroundService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    // الثوابت للتعرف على الأوامر القادمة من الإشعارات أو الأنشطة
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_SEEK = "ACTION_SEEK";

    // مفاتيح تمرير البيانات
    public static final String SONG_NUMBER_KEY = "SONG_NUMBER_KEY";
    public static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";

    private static final String TAG = "%%FOREGROUND_SERVICE%%";
    private final IBinder iBinder = new LocalBinder();

    public MediaPlayer mediaPlayer;
    public int curentSongNumber = 0;
    public int INDEX = 0;
    public int PLAY_STATE = 0;
    public int PAUSE_STATE = 1;
    public int CURRENT_STATE = 1;

    public CallBacks activity;
    private AudioManager audioManager;
    private NotificationManager manager;
    private NotificationCompat.Builder notificationBuilder;
    private PendingIntent pausePendingIntent;
    private PendingIntent nextPendingIntent;
    private PendingIntent previousPendingIntent;
    private RemoteViews contentView;

    public MyForeGroundService() {
    }

    /**
     * طلب التحكم في الصوت (Audio Focus).
     */
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "My foreground service onCreate().");
        createNotificationChannel();
    }

    /**
     * التعامل مع بدء الخدمة واستقبال الأوامر.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            requestAudioFocus();
            if (intent.getAction() != null && !intent.getAction().matches("")) {
                String action = intent.getAction();
                switch (action) {
                    case ACTION_START_FOREGROUND_SERVICE:
                        startForegroundService();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopForegroundService();
                        break;
                    case ACTION_START:
                        int songIndex = intent.getIntExtra(SONG_NUMBER_KEY, 0);
                        int seekPositon = intent.getIntExtra(SEEK_POSITION_KEY, 0);
                        start(songIndex);
                        break;
                    case ACTION_PLAY:
                    case ACTION_PAUSE:
                        playOrPause();
                        break;
                    case ACTION_NEXT:
                        next();
                        break;
                    case ACTION_PREVIOUS:
                        previous();
                        break;
                    case ACTION_SEEK:
                        int seekPositons = intent.getIntExtra(SEEK_POSITION_KEY, 0);
                        seek(seekPositons);
                        break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        activity = null;
        return super.onUnbind(intent);
    }

    /**
     * تشغيل أو إيقاف الميديا.
     */
    public void playOrPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                CURRENT_STATE = PLAY_STATE;
            } else {
                mediaPlayer.start();
                CURRENT_STATE = PAUSE_STATE;
            }
            pauseAndPlayNotify();
        }
    }

    /**
     * تحديث الإشعار لعرض حالة التشغيل.
     */
    private void pauseAndPlayNotify() {
        notificationBuilder.clearActions()
                .addAction(R.drawable.previous_icon, "previous", previousPendingIntent)
                .addAction(pauseOrPlay(CURRENT_STATE), "pause/play", pausePendingIntent)
                .addAction(R.drawable.next_icon, "next", nextPendingIntent);

        contentView.setImageViewResource(R.id.pause, pauseOrPlay(CURRENT_STATE));
        manager.notify(2, notificationBuilder.build());
    }

    /**
     * بدء تشغيل الملف الصوتي.
     */
    public void start(int songIndex) {
        curentSongNumber = songIndex;
        initMediaPlayer(curentSongNumber);
        changeTitle();
    }

    /**
     * تحديث عنوان الإشعار باسم الأغنية.
     */
    private void changeTitle() {
        try {
            notificationBuilder.setContentTitle(DummyContent.ITEMS.get(curentSongNumber).content);
        } catch (Exception e) {
            // التعامل مع الخطأ في حال لم يتم العثور على العنوان
        }
    }

    /**
     * عرض أيقونة التشغيل أو الإيقاف حسب الحالة.
     */
    private int pauseOrPlay(int state) {
        return (state == PLAY_STATE) ? R.drawable.play_icon : R.drawable.pause_icon;
    }

    public boolean next() {
        if (curentSongNumber < (DummyContent.ITEMS.size() - 1)) {
            curentSongNumber++;
            initMediaPlayer(curentSongNumber);
            changeTitle();
            return true;
        } else {
            return false;
        }
    }

    public boolean previous() {
        if (curentSongNumber > 0) {
            curentSongNumber--;
            initMediaPlayer(curentSongNumber);
            changeTitle();
            return true;
        } else {
            return false;
        }
    }

    private void seek(int seekPosition) {
        if (mediaPlayer != null)
            mediaPlayer.seekTo(seekPosition);
    }

    /**
     * إنشاء إشعار وتشغيل الخدمة في المقدمة.
     */
    private void startForegroundService() {
        Log.d(TAG, "Start foreground service.");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // إعداد الأوامر الخاصة بالإشعار
        Intent stopIntent = new Intent(this, MyForeGroundService.class);
        stopIntent.setAction(ACTION_STOP_FOREGROUND_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent = new Intent(this, MyForeGroundService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, MyForeGroundService.class);
        nextIntent.setAction(ACTION_NEXT);
        nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent previousIntent = new Intent(this, MyForeGroundService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        previousPendingIntent = PendingIntent.getService(this, 0, previousIntent, PendingIntent.FLAG_IMMUTABLE);

        String NOTIFICATION_CHANNEL_ID = "xom.music.test.offline";
        String channelName = "My Background Service";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        contentView = new RemoteViews(getPackageName(), R.layout.notification);

        contentView.setOnClickPendingIntent(R.id.previous, previousPendingIntent);
        contentView.setOnClickPendingIntent(R.id.pause, pausePendingIntent);
        contentView.setOnClickPendingIntent(R.id.next, nextPendingIntent);
        contentView.setTextViewText(R.id.trackTitle, DummyContent.ITEMS.get(curentSongNumber).content);

        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setLargeIcon(largeIcon)
                .setContentTitle(DummyContent.ITEMS.get(curentSongNumber).content)
                .addAction(R.drawable.previous_icon, "previous", previousPendingIntent)
                .addAction(pauseOrPlay(CURRENT_STATE), "pause/play", pausePendingIntent)
                .addAction(R.drawable.next_icon, "next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(2, notification);
    }

    /**
     * إيقاف الخدمة في المقدمة وتحرير الموارد.
     */
    private void stopForegroundService() {
        Log.e(TAG, "Stop foreground service.");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (!MainActivity.active) {
            stopForeground(true);
            stopSelf();
        }
    }

    /**
     * تهيئة وتشغيل مشغل الصوت.
     */
    private void initMediaPlayer(int songIndex) {
        INDEX = songIndex;
        DummyContent.DummyItem item = DummyContent.ITEMS.get(songIndex);
        String mediaFile = item.content + "." + item.details;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            AssetFileDescriptor descriptor = getAssets().openFd(mediaFile);
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }

        mediaPlayer.prepareAsync();
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            switch (focusState) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mediaPlayer == null) initMediaPlayer(curentSongNumber);
                    else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                    break;
            }
        }
    }

    @Override public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {}
    @Override public void onCompletion(MediaPlayer mediaPlayer) { next(); }
    @Override public boolean onError(MediaPlayer mediaPlayer, int i, int i1) { return true; }
    @Override public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) { return false; }
    @Override public void onPrepared(MediaPlayer mediaPlayer) {
        this.mediaPlayer.start();
        if (activity != null) activity.onMediaStart();
    }
    @Override public void onSeekComplete(MediaPlayer mediaPlayer) {}

    public void registerClient(Activity activity) {
        this.activity = (CallBacks) activity;
    }

    /**
     * إنشاء قناة إشعار مطلوبة في Android O+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "chanel name";
            String description = "chanel discription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    interface CallBacks {
        void onMediaStart();
        void onMediaPause();
        void onMediaStop();
    }

    public class LocalBinder extends Binder {
        public MyForeGroundService getService() {
            return MyForeGroundService.this;
        }
    }
}
