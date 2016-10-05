package oliver.com.atestffmpeg;

import android.Manifest;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(this);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        configureFFMpeg();
    }

    @Override
    public void onClick(View view) {
        String inputVideo = "/storage/emulated/0/Movies/SOLO/test/solo_in_000.mp4";
        String inputAudio = "/storage/emulated/0/Movies/SOLO/test/trimmedAudio.mp3";

        String inputVideo1 = "/storage/emulated/0/Movies/SOLO/test/solo_filtered_video.mp4";
        String inputAudio1 = "/storage/emulated/0/Movies/SOLO/test/audio_from_video.aac";
        String audioStartTime = "00:00:00.000";
        String audioDuration = "00:00:09.925";
        String originalAudio = "/storage/emulated/0/Files/music/PaRaMoRe/Albums/2013 - Paramore (Deluxe Edition)/14. (One Of Those) Crazy Girls.mp3";
        String audioOut = "/storage/emulated/0/Movies/SOLO/test/audioOut.mp3";


        Log.d(TAG, "onClick: " + originalAudio + " exist: " + new File(originalAudio).exists());
//        trimAudio(startTime, duration, originalAudio, audioOut);
        String outVideo = "/storage/emulated/0/Movies/SOLO/test/video_with_audio.mp4";
        addAudioToVideo(inputVideo, inputAudio, outVideo);
//        testVideo(inputVideo);
    }

    private void addAudioToVideo(String inputVideo, String inputAudio, final String outVideo) {
        final long startTime = System.currentTimeMillis();
        FFMpegUtils.addAudioToVideo(this, inputVideo, inputAudio, outVideo, new FFMpegSimpleListener() {
            @Override
            public void onSuccess(String message) {
                long endTime = System.currentTimeMillis();
                Log.d(TAG, "addAudioToVideo onSuccess: take: " + (endTime - startTime));
                testVideo(outVideo);
            }

            @Override
            public void onFail(String message) {
                long endTime = System.currentTimeMillis();
                Log.d(TAG, "addAudioToVideo onFail: take: " + (endTime - startTime));
            }
        });
    }


    private void testVideo(String outVideo) {
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.setVideoURI(Uri.fromFile(new File(outVideo)));
        mVideoView.requestFocus();
        mVideoView.start();
    }


    private void trimAudio(String startTime, String duration, String originalAudio, String audioOut) {
        FFMpegUtils.trimAudio(this,
                originalAudio,
                startTime, duration, audioOut,
                new FFMpegSimpleListener() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "onSuccess: ");
            }

            @Override
            public void onFail(String message) {
                Log.d(TAG, "onFail: ");
            }
        });
    }


    private void configureFFMpeg() {
        FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d(TAG, "FFmpeg on start");
                }

                @Override
                public void onFailure() {
                    Log.d(TAG, "FFmpeg on failure");
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "FFmpeg on success");
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "FFmpeg on finish");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
            Log.d(TAG, "Handle if FFmpeg is not supported by device");
        }
    }
}
