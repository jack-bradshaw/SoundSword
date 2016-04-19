package samaritanheuristics.testapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.matthewtamlin.android_utilities_library.helpers.AssetsHelper;
import com.matthewtamlin.soundsword.ImmutableVolumeProfile;
import com.matthewtamlin.soundsword.PlayableMedia;
import com.matthewtamlin.soundsword.PlaybackService;
import com.matthewtamlin.soundsword.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestPlaybackService extends AppCompatActivity {
	/**
	 * Used during logging to identify this class.
	 */
	private static final String TAG = "[TestPlaybackService]";

	/**
	 * Very quiet volume profile.
	 */
	private static final ImmutableVolumeProfile QUIET_VOLUME_PROFILE = ImmutableVolumeProfile
			.newInstance()
			.withDuckingVolume(0.05f).withNormalVolume(0.2f);

	/**
	 * Very loud volume profile.
	 */
	private static final ImmutableVolumeProfile LOUD_VOLUME_PROFILE = ImmutableVolumeProfile
			.newInstance()
			.withDuckingVolume(0.1f).withNormalVolume(1f);

	/**
	 * The filenames of the songs to play (in assets).
	 */
	private static final String[] SONG_FILENAMES = new String[]{"track1.mp3", "track2.mp3",
			"track3.mp3", "track4.mp3"};

	/**
	 * Plays music in the background.
	 */
	private PlaybackService playbackService = null;

	/**
	 * The View to display the testing buttons in.
	 */
	private LinearLayout content;

	/**
	 * The songs to play.
	 */
	private final List<Song> songs = new ArrayList<>();

	/**
	 * The index of the currently playing song.
	 */
	private int mediaIndex = 0;

	/**
	 * Whether or not the service has been explicitly started.
	 */
	private boolean serviceIsStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialiseMedia();
		bindViews();
		createButtons();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindPlaybackService();
	}

	private void initialiseMedia() {
		File target = new File(Environment.getExternalStorageDirectory(), "/SoundSword");
		target.mkdir();

		try {
			AssetsHelper.copyAssetsToDirectory(getAssets(), SONG_FILENAMES, target);

			for (String s : SONG_FILENAMES) {
				songs.add(Song.fromFile(new File(target, "/" + s)));
			}
		} catch (IOException e) {
			Log.e(TAG, "[Asset copy error]");
		}
	}

	private void bindViews() {
		content = (LinearLayout) findViewById(R.id.content);
	}

	private void createButtons() {
		final Button bindUnbind = new Button(this);
		content.addView(bindUnbind);
		bindUnbind.setText("bind to service");
		bindUnbind.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: bind]");

				if (mediaServiceIsBound()) {
					unbindPlaybackService();
					bindUnbind.setText("bind to service");
				} else {
					bindPlaybackService();
					bindUnbind.setText("unbind from service");
				}
			}
		});

		final Button startStop = new Button(this);
		content.addView(startStop);
		startStop.setText("start service");
		startStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: bind]");

				if (serviceIsStarted) {
					stopService(new Intent(TestPlaybackService.this, PlaybackService.class));
					startStop.setText("start service");
					serviceIsStarted = false;
				} else {
					startService(new Intent(TestPlaybackService.this, PlaybackService.class));
					startStop.setText("stop service");
					serviceIsStarted = true;
				}
			}
		});

		final Button setMedia = new Button(this);
		content.addView(setMedia);
		setMedia.setText("set media source (toggles between sources)");
		setMedia.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: set media button]");

				if (mediaServiceIsBound()) {
					Log.d(TAG, "[Operation should be valid: " + playbackService.isValidOperation
							(PlaybackService.Operation.CHANGE_MEDIA_SOURCE) + "]");

					playbackService.requestChangeMediaSourceOperation(songs.get(mediaIndex), null);
					mediaIndex = (mediaIndex + 1) % songs.size();
				}
			}
		});

		final Button play = new Button(this);
		content.addView(play);
		play.setText("play");
		play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: play button]");

				if (mediaServiceIsBound()) {

					Log.d(TAG, "[Operation should be valid: " + playbackService.isValidOperation
							(PlaybackService.Operation.PLAY) + "]");

					playbackService.requestPlayMediaOperation();
				}
			}
		});

		final Button pause = new Button(this);
		content.addView(pause);
		pause.setText("pause");
		pause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: pause button]");

				if (mediaServiceIsBound()) {
					Log.d(TAG, "[Operation should be valid: " + playbackService.isValidOperation
							(PlaybackService.Operation.PAUSE) + "]");

					playbackService.requestPauseMediaOperation();
				}
			}
		});

		final Button stop = new Button(this);
		content.addView(stop);
		stop.setText("stop");
		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: stop button]");

				if (mediaServiceIsBound()) {
					Log.d(TAG, "[Operation should be valid: " + playbackService.isValidOperation
							(PlaybackService.Operation.STOP) + "]");

					playbackService.requestStopMediaOperation();
				}
			}
		});

		final Button seek = new Button(this);
		content.addView(seek);
		seek.setText("seek to 4 sec");
		seek.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: seek button]");

				if (mediaServiceIsBound()) {
					Log.d(TAG, "[Operation should be valid: " + playbackService.isValidOperation
							(PlaybackService.Operation.SEEK_TO) + "]");

					playbackService.requestSeekToOperation(4000);
				}
			}
		});

		final Button looping = new Button(this);
		content.addView(looping);
		looping.setText("enable looping");
		looping.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: looping button]");

				if (mediaServiceIsBound()) {
					playbackService.enableLooping(!playbackService.loopingIsEnabled());
					looping.setText(
							playbackService.loopingIsEnabled() ? "disable looping" : "enable " +
									"looping");
				}
			}
		});

		final Button showPosition = new Button(this);
		content.addView(showPosition);
		showPosition.setText("show current position");
		showPosition.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: show position]");

				if (mediaServiceIsBound()) {
					Log.d(TAG, "[current position: " + playbackService.getCurrentPosition() + "]");
				}
			}
		});

		final Button isPlaying = new Button(this);
		content.addView(isPlaying);
		isPlaying.setText("currently playing?");
		isPlaying.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: is playing");

				if (mediaServiceIsBound()) {
					Log.d(TAG, "[currently playing?: " + playbackService.isPlaying() + "]");
				}
			}
		});

		final Button volume = new Button(this);
		content.addView(volume);
		volume.setText("use quiet volume");
		volume.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: volume]");

				if (mediaServiceIsBound()) {
					if (playbackService.getVolumeProfile() == QUIET_VOLUME_PROFILE) {
						playbackService.setVolumeProfile(LOUD_VOLUME_PROFILE);
						volume.setText("use quiet volume");
					} else {
						playbackService.setVolumeProfile(QUIET_VOLUME_PROFILE);
						volume.setText("use loud volume");
					}
				}
			}
		});

		final Button reset = new Button(this);
		content.addView(reset);
		reset.setText("reset service");
		reset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: reset]");

				if (mediaServiceIsBound()) {
					playbackService.reset();
				}
			}
		});

		final Button automaticStop = new Button(this);
		content.addView(automaticStop);
		automaticStop.setText("Enable automatic stop");
		automaticStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "[on click: enable/disable automatic stop]");

				if (mediaServiceIsBound()) {
					if (playbackService.serviceWillStopAutomatically()) {
						playbackService.stopServiceAutomatically(false);
						automaticStop.setText("Enable automatic stop");
					} else {
						playbackService.stopServiceAutomatically(true);
						automaticStop.setText("Disable automatic stop");
					}
				}
			}
		});
	}

	private void registerListeners() {
		playbackService.setOnOperationFinishedListener(
				new PlaybackService.OnOperationFinishedListener() {
					@Override
					public void onOperationFinished(PlaybackService service,
							PlaybackService.Operation operation, PlaybackService.FailureMode
							failureMode) {
						Log.d(TAG, "[OnOperationFinishedListener] [operation: " + operation + "]" +
								"[failure mode: " + failureMode + "]");
					}
				});

		playbackService.setOnOperationStartedListener(
				new PlaybackService.OnOperationStartedListener() {
					@Override
					public void OnOperationStarted(PlaybackService service,
							PlaybackService.Operation operation) {
						Log.d(TAG, "[OnOperationStartedListener] [operation: " + operation + "]");
						Log.d(TAG, "[Current operation according to " +
								"playbackService.getCurrentOperation(): " +
								playbackService.getCurrentOperation() + "]");
					}
				});

		playbackService.setOnPendingOperationsCancelledListener(
				new PlaybackService.OnPendingOperationsCancelledListener() {
					@Override
					public void onPendingOperationsCancelled(PlaybackService service) {
						Log.d(TAG, "[OnPendingOperationsCancelledListener]");
					}
				});

		playbackService.setOnPlaybackCompleteListener(
				new PlaybackService.OnPlaybackCompleteListener() {
					@Override
					public void onPlaybackComplete(PlaybackService service,
							PlayableMedia completedMedia) {
						Log.d(TAG, "[OnPlaybackCompleteListener]");
					}
				});
	}

	private boolean mediaServiceIsBound() {
		return playbackService != null;
	}

	private void bindPlaybackService() {
		if (!mediaServiceIsBound()) {
			Intent launchMediaService = new Intent(this, PlaybackService.class);
			bindService(launchMediaService, mediaServiceConnection, BIND_AUTO_CREATE);
		}
	}

	private void unbindPlaybackService() {
		if (mediaServiceIsBound()) {
			unbindService(mediaServiceConnection);
			playbackService = null;
		}
	}

	private final ServiceConnection mediaServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			playbackService = ((PlaybackService.LocalBinder) binder).getService();
			registerListeners();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			playbackService = null;
		}
	};
}
