package com.toonera.build;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import com.google.android.exoplayer2.*;
import com.toonera.build.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class PlayerActivity extends Activity {
	
	private PlayerBinding binding;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = PlayerBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		// 1. Landscape orientation lock
		setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		// 2. Base layout background black karo
		android.widget.LinearLayout baseLayout = (android.widget.LinearLayout) findViewById(getResources().getIdentifier("linear1", "id", getPackageName()));
		baseLayout.setBackgroundColor(android.graphics.Color.BLACK);
		baseLayout.removeAllViews();
		
		// Agar pehle se player chal raha hai toh release karo
		if (baseLayout.getTag() != null) {
			try {
				Object oldPlayer = baseLayout.getTag();
				oldPlayer.getClass().getMethod("release").invoke(oldPlayer);
			} catch(Exception e) {}
		}
		
		// 3. URL pakdo
		final String videoUrl = getIntent().getStringExtra("url");
		
		if (videoUrl != null && !videoUrl.isEmpty()) {
			try {
				final android.content.Context ctx = this;
				
				// PlayerView dynamic create karo
				Class<?> playerViewClass = Class.forName("com.google.android.exoplayer2.ui.SimpleExoPlayerView");
				android.view.View playerView = (android.view.View) playerViewClass.getConstructor(android.content.Context.class).newInstance(ctx);
				playerView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT
				));
				baseLayout.addView(playerView);
				
				// TrackSelector setup via reflection
				Object bandwidthMeter = Class.forName("com.google.android.exoplayer2.upstream.DefaultBandwidthMeter").getConstructor().newInstance();
				Class<?> bfClass = Class.forName("com.google.android.exoplayer2.upstream.BandwidthMeter");
				
				Object videoTrackSelectionFactory = Class.forName("com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection$Factory")
				.getConstructor(bfClass).newInstance(bandwidthMeter);
				
				Class<?> tsFactoryClass = Class.forName("com.google.android.exoplayer2.trackselection.TrackSelection$Factory");
				Object trackSelector = Class.forName("com.google.android.exoplayer2.trackselection.DefaultTrackSelector")
				.getConstructor(tsFactoryClass).newInstance(videoTrackSelectionFactory);
				
				// SimpleExoPlayer instance
				Class<?> tsClass = Class.forName("com.google.android.exoplayer2.trackselection.TrackSelector");
				Object player = Class.forName("com.google.android.exoplayer2.ExoPlayerFactory")
				.getMethod("newSimpleInstance", android.content.Context.class, tsClass)
				.invoke(null, ctx, trackSelector);
				
				// Tag mein save karo taaki back press par control ho
				baseLayout.setTag(player);
				
				// PlayerView ko player do
				Class<?> playerClass = Class.forName("com.google.android.exoplayer2.Player");
				playerViewClass.getMethod("setPlayer", playerClass).invoke(playerView, player);
				
				// DataSource & MediaSource
				Object dataSourceFactory = Class.forName("com.google.android.exoplayer2.upstream.DefaultDataSourceFactory")
				.getConstructor(android.content.Context.class, String.class)
				.newInstance(ctx, "ModernPlayer");
				
				Class<?> uriClass = Class.forName("android.net.Uri");
				Object uri = uriClass.getMethod("parse", String.class).invoke(null, videoUrl);
				
				Class<?> dsFactoryClass = Class.forName("com.google.android.exoplayer2.upstream.DataSource$Factory");
				Object mediaSourceFactory = Class.forName("com.google.android.exoplayer2.source.ExtractorMediaSource$Factory")
				.getConstructor(dsFactoryClass).newInstance(dataSourceFactory);
				
				Object mediaSource = mediaSourceFactory.getClass()
				.getMethod("createMediaSource", uriClass)
				.invoke(mediaSourceFactory, uri);
				
				// Prepare & Play
				Class<?> mediaSourceClass = Class.forName("com.google.android.exoplayer2.source.MediaSource");
				player.getClass().getMethod("prepare", mediaSourceClass).invoke(player, mediaSource);
				player.getClass().getMethod("setPlayWhenReady", boolean.class).invoke(player, true);
				
			} catch (Exception e) {
				e.printStackTrace();
				android.widget.Toast.makeText(this, "Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
}