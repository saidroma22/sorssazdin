package com.Azzadine.offline;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.Azzadine.offline.dummy.DummyContent;
import music.saidweb.playlist.offline.Ringtone.Operation;
import music.saidweb.playlist.offline.Ringtone.RingToneOperation;

public class MainActivity extends AppCompatActivity implements SongFragment.OnListSongsFragmentInteractionListener, MyForeGroundService.CallBacks {

    private static final String TAG = "##MainActivity##";

    static boolean active = false;
    private boolean serviceBound = false;

    private ImageView playPauseIV, next, previous, moreButton;
    private TextView songName, totalTimeTV, leftTimeTV;
    private SeekBar songSeekBar;

    private boolean setting = false;
    private boolean storage = false;
    private String fileName = "";
    private int type = 0;
    private int counter = 0;

    private MyForeGroundService mService;

    private final Runnable runnable = () -> {
        if (serviceBound && mService.mediaPlayer != null) {
            updateProgressLayout(mService.mediaPlayer.getDuration(), mService.mediaPlayer.getCurrentPosition());
        } else {
            updateProgressLayout(0, 0);
        }
        leftTimeTV.postDelayed(this.runnable, 250);
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyForeGroundService.LocalBinder binder = (MyForeGroundService.LocalBinder) service;
            mService = binder.getService();
            mService.registerClient(MainActivity.this);
            serviceBound = true;
            leftTimeTV.post(runnable);

            MediaPlayer mediaPlayer = mService.mediaPlayer;
            boolean isPlaying = mediaPlayer != null && mediaPlayer.isPlaying();
            if (mediaPlayer != null) {
                initializeLayout(mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), mService.curentSongNumber, isPlaying);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            leftTimeTV.removeCallbacks(runnable);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(true);
        }

        active = true;
        setContentView(R.layout.activity_main);

        AdsManager.getInstance().init(this, Constants.ad_banner);
        AdsManager.getInstance().init(this, Constants.ad_inter);
        AdsManager.getInstance().init(this, Constants.ad_native);
        AdsManager.getInstance().loadInterstitialAd(this, Constants.ad_inter);
        AdsManager.getInstance().loadBannerAd(this, Constants.ad_banner);

        Intent intent = new Intent(MainActivity.this, MyForeGroundService.class);
        intent.setAction(MyForeGroundService.ACTION_START_FOREGROUND_SERVICE);
        startService(intent);

        setFragment(SongFragment.newInstance(1));

        // ربط مكونات الواجهة
        songName = findViewById(R.id.song_name_tv);
        totalTimeTV = findViewById(R.id.total_time_tv);
        leftTimeTV = findViewById(R.id.left_time_tv);
        songSeekBar = findViewById(R.id.song_seek_bar);
        playPauseIV = findViewById(R.id.play_and_pause);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        moreButton = findViewById(R.id.more_button); // زر النقاط الثلاث

        // تشغيل القائمة المنبثقة المخصصة
        moreButton.setOnClickListener(view -> {
            Context themedContext = new android.view.ContextThemeWrapper(this, R.style.AppTheme);
            PopupMenu popupMenu = new PopupMenu(themedContext, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.more_app) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/azzadine-offline")));
                    return true;
                } else if (id == R.id.rate_app) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.Azzadine.offline")));
                    return true;
                } else if (id == R.id.share_app) {
                    try {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                        String sAux = "\n Hi! Try Out This Amazing App:\n\nhttps://play.google.com/store/apps/details?id=com.Azzadine.offline\n\n";
                        i.putExtra(Intent.EXTRA_TEXT, sAux);
                        startActivity(Intent.createChooser(i, "Choose one"));
                    } catch (Exception e) {
                        // ignore
                    }
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                seek(seekBar.getProgress() * 1000);
            }
        });

        playPauseIV.setOnClickListener(view -> playOrPause());
        next.setOnClickListener(view -> {
            if (++counter >= Constants.ad_interval) {
                AdsManager.getInstance().showInterstitialAd();
                counter = 0;
            }
            next();
        });

        previous.setOnClickListener(view -> {
            if (++counter >= Constants.ad_interval) {
                AdsManager.getInstance().showInterstitialAd();
                counter = 0;
            }
            previous();
        });
    }

    @Override protected void onStart() {
        super.onStart();
        bindService(new Intent(this, MyForeGroundService.class), serviceConnection, 0);
    }

    @Override protected void onStop() {
        super.onStop();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
            leftTimeTV.removeCallbacks(runnable);
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        active = false;
    }

    private void next() {
        if (isMyServiceRunning(MyForeGroundService.class) && mService != null && mService.next()) {
            playPauseIV.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
        }
    }

    private void previous() {
        if (isMyServiceRunning(MyForeGroundService.class) && mService != null && mService.previous()) {
            playPauseIV.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
        }
    }

    private void playOrPause() {
        if (isMyServiceRunning(MyForeGroundService.class) && mService != null && mService.mediaPlayer != null) {
            if (mService.mediaPlayer.isPlaying()) {
                playPauseIV.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
            } else {
                playPauseIV.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
            }
            mService.playOrPause();
        } else {
            Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
        }
    }

    private void seek(int seekpositon) {
        Intent intent = new Intent(MainActivity.this, MyForeGroundService.class);
        intent.setAction(MyForeGroundService.ACTION_SEEK);
        intent.putExtra(MyForeGroundService.SEEK_POSITION_KEY, seekpositon);
        startService(intent);
    }

    private void startSong(int songIndex, int seekPosition) {
        Intent intent = new Intent(MainActivity.this, MyForeGroundService.class);
        intent.setAction(MyForeGroundService.ACTION_START);
        intent.putExtra(MyForeGroundService.SONG_NUMBER_KEY, songIndex);
        intent.putExtra(MyForeGroundService.SEEK_POSITION_KEY, seekPosition);
        startService(intent);
    }

    @Override
    public void onListSongsFragmentInteraction(int songIndex) {
        if (++counter >= Constants.ad_interval) {
            AdsManager.getInstance().showInterstitialAd();
            counter = 0;
        }
        startSong(songIndex, 0);
        print();
    }

    @Override
    public void optionButtonClicked(int position, int type) {
        this.type = type;
        this.fileName = DummyContent.ITEMS.get(position).content;
    }

    @Override
    public void setRingtone() {
        if (storage && setting) {
            RingToneOperation ringtone = new RingToneOperation(this);
            Operation file = ringtone.createRingToneFile(fileName);
            if (file.isSuccess()) {
                ringtone.SetAsRingtoneOrNotification(file.getFile(), type);
                if (type == RingtoneManager.TYPE_ALARM) {
                    Toast.makeText(this, getString(R.string.add_to_alarem), Toast.LENGTH_SHORT).show();
                } else if (type == RingtoneManager.TYPE_RINGTONE) {
                    Toast.makeText(this, getString(R.string.add_to_rington), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Failed to set ringtone: " + file);
            }
        } else {
            Log.e(TAG, "Permission denied to set ringtone");
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void setFragment(Fragment fragment) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.songs_container, fragment);
        t.commit();
    }

    @Override public void onMediaPause() {}
    @Override public void onMediaStop() {}

    @Override
    public void onMediaStart() {
        int songnumber = mService.curentSongNumber;
        int songTime = mService.mediaPlayer.getDuration();
        initializeLayout(songTime, 0, songnumber, true);
    }

    private void initializeLayout(int songTime, int leftTime, int songnumber, boolean isPlaying) {
        songName.setText(DummyContent.ITEMS.get(songnumber).content);
        int minutes = (songTime / 1000) / 60;
        int seconds = (songTime / 1000) % 60;
        totalTimeTV.setText(minutes + ":" + seconds);
        updateProgressLayout(songTime, leftTime);
        playPauseIV.setImageDrawable(getResources().getDrawable(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play));
    }

    private void updateProgressLayout(int totalTime, int leftTime) {
        int secLeftTime = leftTime / 1000;
        int secTotalTime = totalTime / 1000;
        songSeekBar.setMax(secTotalTime);
        songSeekBar.setProgress(secLeftTime);
        int minutes = secLeftTime / 60;
        int seconds = secLeftTime % 60;
        leftTimeTV.setText(minutes + ":" + seconds);
    }

    public void print() {
        Log.d(TAG, "----------- Hi There ------------");
    }
}
