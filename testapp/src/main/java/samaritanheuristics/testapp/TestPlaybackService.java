package samaritanheuristics.testapp;

import android.annotation.SuppressLint;
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

@SuppressLint("SetTextI18n") // Localisation not necessary for testing
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
		setContentView(R.layout.activity_test_playback_service);
		initialiseMedia();
		bindViews();
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

	public void testBindUnbindService(final View v) {
		if (mediaServiceIsBound()) {
			unbindPlaybackService();
			((Button) v).setText("Bind to service");
		} else {
			bindPlaybackService();
			((Button) v).setText("Unbind from service");
		}
	}

	public void testStartStopService(final View v) {
		if (serviceIsStarted) {
			stopService(new Intent(TestPlaybackService.this, PlaybackService.class));
			serviceIsStarted = false;
			((Button) v).setText("Start service");
		} else {
			startService(new Intent(TestPlaybackService.this, PlaybackService.class));
			serviceIsStarted = true;
			((Button) v).setText("Stop service");
		}
	}

	public void testSetMedia(final View v) {
		if (mediaServiceIsBound()) {
			playbackService.requestChangeMediaSourceOperation(songs.get(mediaIndex), null);
			mediaIndex = (mediaIndex + 1) % songs.size();
		}
	}

	public void testPlayMedia(final View v) {
		if (mediaServiceIsBound()) {
			playbackService.requestPlayMediaOperation();
		}
	}

	public void testPauseMedia(final View v) {
		if (mediaServiceIsBound()) {
			playbackService.requestPauseMediaOperation();
		}
	}

	public void testStopMedia(final View v) {
		if (mediaServiceIsBound()) {
			playbackService.requestStopMediaOperation();
		}
	}

	public void testSeek(final View v) {
		if (mediaServiceIsBound()) {
			playbackService.requestSeekToOperation(4000);
		}
	}

	public void testToggleLooping(final View v) {
		if (mediaServiceIsBound()) {
			playbackService.enableLooping(!playbackService.loopingIsEnabled());
			((Button) v).setText(playbackService.loopingIsEnabled() ? "Disable looping" : "Enable looping");
		}
	}

	public void testShowStatus(final View v) {
		if (mediaServiceIsBound()) {
			// TODO
		}
	}

	public void testChangeVolume(final View v) {
		if (mediaServiceIsBound()) {
			if (playbackService.getVolumeProfile() == QUIET_VOLUME_PROFILE) {
				playbackService.setVolumeProfile(LOUD_VOLUME_PROFILE);
				((Button) v).setText("Use quiet volume profile");
			} else {
				playbackService.setVolumeProfile(QUIET_VOLUME_PROFILE);
				((Button) v).setText("Use loud volume profile");
			}
		}
	}

	public void testReset(final View v) {
		if (mediaServiceIsBound()) {
			playbackService.reset();
		}
	}

	public void testToggleAutomaticStop(final View v) {
		if (mediaServiceIsBound()) {
			if (playbackService.serviceWillStopAutomatically()) {
				playbackService.stopServiceAutomatically(false);
				((Button) v).setText("Enable automatic stop");
			} else {
				playbackService.stopServiceAutomatically(true);
				((Button) v).setText("Disable automatic stop");
			}
		}
	}

	public void registerListeners() {
		if (mediaServiceIsBound()) {
			playbackService.addOnOperationFinishedListener(
					new PlaybackService.OnOperationFinishedListener() {
						@Override
						public void onOperationFinished(PlaybackService service,
								PlaybackService.Operation operation, PlaybackService.FailureMode
								failureMode) {
							Log.d(TAG,
									"[OnOperationFinishedListener] [operation: " + operation + "]" +
											"[failure mode: " + failureMode + "]");
						}
					});

			playbackService.addOnOperationStartedListener(
					new PlaybackService.OnOperationStartedListener() {
						@Override
						public void OnOperationStarted(PlaybackService service,
								PlaybackService.Operation operation) {
							Log.d(TAG,
									"[OnOperationStartedListener] [operation: " + operation + "]");
							Log.d(TAG, "[Current operation according to " +
									"playbackService.getCurrentOperation(): " +
									playbackService.getCurrentOperation() + "]");
						}
					});

			playbackService.addOnPendingOperationsCancelledListener(
					new PlaybackService.OnPendingOperationsCancelledListener() {
						@Override
						public void onPendingOperationsCancelled(PlaybackService service) {
							Log.d(TAG, "[OnPendingOperationsCancelledListener]");
						}
					});

			playbackService.addOnPlaybackCompleteListener(
					new PlaybackService.OnPlaybackCompleteListener() {
						@Override
						public void onPlaybackComplete(PlaybackService service,
								PlayableMedia completedMedia) {
							Log.d(TAG, "[OnPlaybackCompleteListener]");
						}
					});
		}
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