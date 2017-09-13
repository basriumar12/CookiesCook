package com.ray.cookiescook;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.ray.cookiescook.database.StepsColumns;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Olis on 9/11/2017.
 */

public class StepDetailFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "StepDetailFragment";

    public interface NavigationListener {
        void onNextPressed();

        void onPrevPressed();
    }

    @BindView(R.id.text_step)
    TextView textDescription;

    @BindView(R.id.exo_video_view)
    SimpleExoPlayerView exoPlayerView;

    @BindView(R.id.button_next)
    Button btnNext;

    @BindView(R.id.button_prev)
    Button btnPrev;

    private String videoUrl;

    private NavigationListener mNavCallback;

    private SimpleExoPlayer mExoPlayer;
    private boolean playWhenReady = true;
    private long playBackPosition = 0;
    private int currentWindow = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        String description = getArguments().getString(StepsColumns.DESCRIPTION);
        videoUrl = getArguments().getString(StepsColumns.VIDEO_URL);
        try {
            textDescription.setText(URLDecoder.decode(description, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "onViewCreated: " + e.toString());
        }

        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getArguments().getString(getResources().getString(R.string.text_title)));

        if (getArguments().getBoolean("islast")) {
            btnNext.setVisibility(View.GONE);
        } else btnNext.setVisibility(View.VISIBLE);

        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            exoPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mNavCallback = (NavigationListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: " + e.toString());
        }
    }

    private void initializePlayer() {
        if (videoUrl.isEmpty()) exoPlayerView.setVisibility(View.GONE);
        else {
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), new DefaultTrackSelector(), new DefaultLoadControl());
            exoPlayerView.setPlayer(mExoPlayer);

            mExoPlayer.setPlayWhenReady(playWhenReady);
            mExoPlayer.seekTo(currentWindow, playBackPosition);

            Uri uri = Uri.parse(videoUrl);
            MediaSource mediaSource = buildMediaSource(uri);
            mExoPlayer.prepare(mediaSource, true, false);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource(uri,
                new DefaultHttpDataSourceFactory("ua"),
                new DefaultExtractorsFactory(), null, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        hideSystemUi();
        if ((Util.SDK_INT <= 23 || mExoPlayer == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (mExoPlayer != null) {
            playBackPosition = mExoPlayer.getCurrentPosition();
            currentWindow = mExoPlayer.getCurrentWindowIndex();
            playWhenReady = mExoPlayer.getPlayWhenReady();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_next) {
            mNavCallback.onNextPressed();
        } else if (view.getId() == R.id.button_prev) {
            mNavCallback.onPrevPressed();
        }
    }
}