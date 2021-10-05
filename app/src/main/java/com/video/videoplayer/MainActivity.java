package com.video.videoplayer;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Pair;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    SimpleExoPlayer simpleExoPlayer;
    PlayerView myVideo;
    DefaultTrackSelector trackSelector;
    DataSource.Factory customHeaders;

    boolean isShowingTrackSelectionDialog = false;
    String url;
    long playerMS = 0;

    boolean isRotation = false;
    boolean Notch = true;
    boolean resume = true;
    boolean fullScreen = false;
    ImageView play, pause, mute, unmute, setFull, exitFull;
    boolean exo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        autoRotate();


        myVideo = findViewById(R.id.myVideo);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        mute = findViewById(R.id.mute);
        unmute = findViewById(R.id.unmute);
        setFull = findViewById(R.id.setFull);
        exitFull = findViewById(R.id.exitFull);
        url = "https://5b44cf20b0388.streamlock.net:8443/vod/smil:bbb.smil/playlist.m3u8";
        //playOnline(url);

    }

    public void submit(View view){
        EditText linkbox = findViewById(R.id.boxLink);
        EditText useragentbox = findViewById(R.id.boxUserAgent);
        EditText headersbox = findViewById(R.id.boxHeaders);
        String link = linkbox.getText().toString();
        String userAgent = useragentbox.getText().toString();
        String headers = headersbox.getText().toString();
        ScrollView box = findViewById(R.id.box);

        if (!link.isEmpty()){
            url = link;

            if (headers.isEmpty()){
                customHeaders = headers();
            }
            if (!headers.isEmpty()){
                String[] arr = headers.split("\n");
                Map<String,String> map = new HashMap<>();
                for (String str: arr){
                    String b = str.substring(str.indexOf(':'));
                    String a = str.replace(b,"");
                    b = str.replace(a+": ","");
                    map.put(a,b);
                }
                customHeaders = new DefaultHttpDataSource.Factory().setDefaultRequestProperties(map);
            }



            playOnline(url);
            box.setVisibility(View.GONE);
        }

    }
    public String trimStr(String str,String a, String b){
        String x1 = str.substring(str.indexOf(a));
        String x2 = x1.substring(x1.indexOf(b));
        return x1.replace(x2,"");
    }


    public DataSource.Factory headers(){
        Map<String,String> map = new HashMap<>();
        map.put("a","b");

        return new DefaultHttpDataSource.Factory().setDefaultRequestProperties(map);
    }



    public void playOnline(String url){
        exo = true;
        trackSelector = new DefaultTrackSelector(this);

        simpleExoPlayer = new SimpleExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(customHeaders)).setTrackSelector(trackSelector).build();
        myVideo.setPlayer(simpleExoPlayer);
        MediaItem mediaItem = MediaItem.fromUri(url);
        simpleExoPlayer.addMediaItem(mediaItem);
        simpleExoPlayer.prepare();
        myVideo.setVisibility(View.VISIBLE);
        simpleExoPlayer.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (exo) {
            playerMS = simpleExoPlayer.getCurrentPosition();
            simpleExoPlayer.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resume){
            resume = false;
        }
        else {
            if (exo) {
                playOnline(url);
                simpleExoPlayer.seekTo(playerMS);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (exo) {
            simpleExoPlayer.release();
        }
    }

    public void Play(View view) {
        simpleExoPlayer.play();
        play.setVisibility(View.GONE);
        pause.setVisibility(View.VISIBLE);
    }

    public void Pause(View view) {
        simpleExoPlayer.pause();
        pause.setVisibility(View.GONE);
        play.setVisibility(View.VISIBLE);
    }

    public void Mute(View view) {
        simpleExoPlayer.setVolume(0);
        mute.setVisibility(View.GONE);
        unmute.setVisibility(View.VISIBLE);
    }

    public void Unmute(View view) {
        simpleExoPlayer.setVolume(1);
        unmute.setVisibility(View.GONE);
        mute.setVisibility(View.VISIBLE);
    }
    public void changeRate(View view){
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null){
            int renderIndex = 0;
            int renderType = mappedTrackInfo.getRendererType(renderIndex);
            boolean allowSelections =
                    renderType == C.TRACK_TYPE_VIDEO
                    || (renderType == C.TRACK_TYPE_AUDIO && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                    == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);
            TrackSelectionDialogBuilder builder= new TrackSelectionDialogBuilder(this,"Change Bitrate",trackSelector, renderIndex);
            builder.setAllowAdaptiveSelections(allowSelections);
            builder.build().show();

        }

    }

    public void autoRotate() {
        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                int epsilon = 10;
                int leftL = 90;
                int rightL = 270;
                if (epsilonCheck(orientation, leftL, epsilon) || epsilonCheck(orientation, rightL, epsilon)) {
                    if (isRotation) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }
                }

            }

            private boolean epsilonCheck(int a, int b, int epsilon) {
                return a > b - epsilon && a < b + epsilon;
            }
        };
        orientationEventListener.enable();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setFull.setVisibility(View.GONE);
            exitFull.setVisibility(View.VISIBLE);
        } else {
            if (Notch) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                setFull.setVisibility(View.VISIBLE);
                exitFull.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!fullScreen) {
            super.onBackPressed();
            finish();
        } else {
            setFullScreen();
        }


    }
    public void FullScreen(View view){
        setFullScreen();
    }

    private void setFullScreen() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            isRotation = true;
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            isRotation = false;
        }
    }
}