package samaritanheuristics.testapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import com.matthewtamlin.android_utilities_library.helpers.AssetsHelper;
import com.matthewtamlin.android_utilities_library.helpers.PermissionsHelper;
import com.matthewtamlin.soundsword.ImmutableVolumeProfile;
import com.matthewtamlin.soundsword.LocalPlayableMedia;
import com.matthewtamlin.soundsword.PlayableMedia;
import com.matthewtamlin.soundsword.PlaybackService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@SuppressLint("SetTextI18n") // Localisation not necessary for testing
public class TestPlaybackService extends AppCompatActivity {
	/**
	 * Used during logging to identify this class.
	 */
	private static final String TAG = "[TestPlaybackService]";

	/**
	 * The location to store the testing music files.
	 */
	private static final File TEST_MUSIC_LOCATION = new File(Environment
			.getExternalStorageDirectory(), "/SoundSwordTesting");

	/**
	 * The filenames of the localPlayableMedias to play (stored in assets).
	 */
	private static final String[] TEST_SONG_FILENAMES = new String[]{"track1.mp3", "track2.mp3",
			"track3.mp3", "track4.mp3"};

	/**
	 * Very quiet volume profile.
	 */
	private static final ImmutableVolumeProfile QUIET_VOLUME_PROFILE = ImmutableVolumeProfile
			.newInstance().withDuckingVolume(0.05f).withNormalVolume(0.2f);

	/**
	 * Very loud volume profile.
	 */
	private static final ImmutableVolumeProfile LOUD_VOLUME_PROFILE = ImmutableVolumeProfile
			.newInstance().withDuckingVolume(0.1f).withNormalVolume(1f);

	/**
	 * The service under test
	 */
	private PlaybackService playbackService = null;

	/**
	 * The root view of the Activity layout.
	 */
	private ScrollView rootView;

	/**
	 * The media to play.
	 */
	private final List<LocalPlayableMedia> localPlayableMedias = new ArrayList<>();

	/**
	 * The index of the currently playing song.
	 */
	private int mediaIndex = 0;

	/**
	 * Whether or not the service has been explicitly started.
	 */
	private boolean serviceExplicitlyStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_playback_service);
		setup();
		rootView = (ScrollView) findViewById(R.id.activity_test_playback_service_root);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindPlaybackService();
	}

	/**
	 * Initialises the testing environment and checks that all preconditions pass.
	 */
	private void setup() {
		// Check precondition 1: Read/write external file storage permission is granted
		final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
		assertThat("Precondition failed. Write external storage permission is required.",
				PermissionsHelper.permissionsAreGranted(this, permissions));

		// Make the directory if it already exists
		TEST_MUSIC_LOCATION.mkdir();
		assertThat("Precondition failed. Test music location must exist.", TEST_MUSIC_LOCATION
				.exists());

		// Prepare music files
		try {
			AssetsHelper.copyAssetsToDirectory(getAssets(), TEST_SONG_FILENAMES,
					TEST_MUSIC_LOCATION);

			for (final String s : TEST_SONG_FILENAMES) {
				localPlayableMedias
						.add(LocalPlayableMedia.fromFile(new File(TEST_MUSIC_LOCATION, "/" + s)));
			}
		} catch (final IOException e) {
			assertThat("Precondition failed. Could not prepare all music files", false);
		}
	}

	public void testBindUnbindService(final View v) {
		if (playbackServiceIsBound()) {
			unbindPlaybackService();
			((Button) v).setText("Bind to service");
		} else {
			bindPlaybackService();
			((Button) v).setText("Unbind from service");
		}
	}

	public void testStartStopService(final View v) {
		if (serviceExplicitlyStarted) {
			stopService(new Intent(TestPlaybackService.this, PlaybackService.class));
			serviceExplicitlyStarted = false;
			((Button) v).setText("Start service");
		} else {
			startService(new Intent(TestPlaybackService.this, PlaybackService.class));
			serviceExplicitlyStarted = true;
			((Button) v).setText("Stop service");
		}
	}

	public void testSetMedia(final View v) {
		if (playbackServiceIsBound()) {
			playbackService.requestChangeMediaSourceOperation(localPlayableMedias.get(mediaIndex), null);
			mediaIndex = (mediaIndex + 1) % localPlayableMedias.size();
		}
	}

	public void testPlayMedia(final View v) {
		if (playbackServiceIsBound()) {
			playbackService.requestPlayMediaOperation();
		}
	}

	public void testPauseMedia(final View v) {
		if (playbackServiceIsBound()) {
			playbackService.requestPauseMediaOperation();
		}
	}

	public void testStopMedia(final View v) {
		if (playbackServiceIsBound()) {
			playbackService.requestStopMediaOperation();
		}
	}

	public void testSeek(final View v) {
		if (playbackServiceIsBound()) {
			playbackService.requestSeekToOperation(4000);
		}
	}

	public void testToggleLooping(final View v) {
		if (playbackServiceIsBound()) {
			playbackService.enableLooping(!playbackService.loopingIsEnabled());
			((Button) v).setText(
					playbackService.loopingIsEnabled() ? "Disable looping" : "Enable looping");
		}
	}

	public void testShowStatus(final View v) {
		if (playbackServiceIsBound()) {
			Snackbar.make(rootView, "Current position: " + playbackService.getCurrentPosition(),
					Snackbar.LENGTH_LONG).show();
		}
	}

	public void testChangeVolume(final View v) {
		if (playbackServiceIsBound()) {
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
		if (playbackServiceIsBound()) {
			playbackService.reset();
		}
	}

	public void testToggleAutomaticStop(final View v) {
		if (playbackServiceIsBound()) {
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
		if (playbackServiceIsBound()) {
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

	private boolean playbackServiceIsBound() {
		return playbackService != null;
	}

	private void bindPlaybackService() {
		if (!playbackServiceIsBound()) {
			Intent launchMediaService = new Intent(this, PlaybackService.class);
			bindService(launchMediaService, mediaServiceConnection, BIND_AUTO_CREATE);
		}
	}

	private void unbindPlaybackService() {
		if (playbackServiceIsBound()) {
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