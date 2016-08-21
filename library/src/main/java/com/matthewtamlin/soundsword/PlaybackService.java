/*
 * Copyright 2016 Matthew Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matthewtamlin.soundsword;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;

import com.matthewtamlin.android_utilities_library.helpers.AudioFocusHelper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Service which enables background media playback. Binding to the service allows clients to
 * control playback, and registering for callbacks allows clients to receive feedback. Since
 * services operate independently of the activity lifecycle, instances of PlaybackService can be
 * used to play media as the user navigates between activities or when the app is in the background.
 * The service provides a means for self-terminating once playback has completed (if desired). The
 * service is controlled with two kinds of methods: Request operation methods and standard methods.
 * <p/>
 * Request operation methods return almost immediately as they simply queue operations for
 * asynchronous processing. Operations are executed one at a time on the 'playback thread' to avoid
 * blocking the UI. Operations are executed in the order in which they are requested, with the
 * exception of the change media source operation. Requesting this operation terminates the current
 * operation and cancels all pending operations. Whenever an operation finishes, the next operation
 * in the queue is immediately executed until there are no more operations to execute. The
 * operations which can be requested are: <ul><li>Change media source ({@link
 * #requestChangeMediaSourceOperation(PlayableMedia, Map)}).</li> <li>Start/resume playback (see
 * {@link #requestPlayMediaOperation()}).</li> <li>Pause playback ({@link
 * #requestPauseMediaOperation()}).</li> <li>Stop playback ({@link #requestStopMediaOperation()}).</li>
 * <li>Seek to a particular position ({@link #requestSeekToOperation(int)}).</li></ul>See the
 * Javadoc of the request operation methods for a more in depth explanation of what each operation
 * does.
 * <p/>
 * The standard methods allow the client to configure the service or query its properties. The
 * standard methods do not perform any asynchronous operations and will run entirely on the calling
 * thread. The standard methods allow the client to:<ul><li>Change the volume profile ({@link
 * #setVolumeProfile(VolumeProfile)}).</li><li>Get the current playback position ({@link
 * #getCurrentPosition()}).</li><li>Determine if the media is currently playing ({@link
 * #isPlaying()}).</li><li>Determine if an operation can be performed at the current time ({@link
 * #isValidOperation(Operation)}).</li><li>Enable/disable looping ({@link
 * #enableLooping(boolean)}).</li><li>Configure the video output ({@link
 * #setDisplay(SurfaceHolder)}).</li><li>Register for callbacks.</li><li>Set the Service to stop
 * itself when playback completes ({@link #stopServiceAutomatically(boolean)}).</li><li>Terminate
 * the current operation, clear pending operations, and terminate playback ({@link
 * #reset()}).</li></ul>See the Javadoc of the standard methods for a more in depth explanation of
 * what each method does.
 * <p/>
 * A PlaybackService can pass information back to the client by means of four separate callbacks.
 * Callbacks are delivered one at a time on the 'callback thread', therefore it is important that
 * the callback listeners do not perform UI related tasks without using the {@link
 * android.app.Activity#runOnUiThread(Runnable)} method. The callbacks are:<ul><li>{@link
 * OnOperationStartedListener}. This callback is invoked by the PlaybackService whenever an
 * operation is started. The callback is passed a reference to the service that issued the callback,
 * as well as {@link Operation} constant which identifies the operation.</li><li>{@link
 * OnOperationFinishedListener}. This callback is invoked by the PlaybackService whenever an
 * operation finishes, either by successful completion or failure. The callback is passed a
 * reference to the service that issued the callback, as well as an {@link Operation} constant which
 * identifies the operation and a {@link FailureMode} constant which identifies the failure mode.
 * The failure mode is null if the operation completed successfully.</li><li>{@link
 * OnPendingOperationsCancelledListener}. This callback is invoked by the PlaybackService whenever
 * the pending operation queue is cleared, regardless of whether or not the queue was empty. The
 * callback is passed a reference to the PlaybackService which issued the callback.</li><li>{@link
 * OnPlaybackCompleteListener}. This callback is invoked by the PlaybackService whenever the end of
 * the current media is reached. The callback may be called multiple times if looping has been
 * enabled. The callback is passed a reference to the PlaybackService which issued the
 * callback.</li></ul>See the Javadoc of the callbacks for a more in depth explanation.
 * <p/>
 * This Service conforms to the standard Android guidelines regarding media playback. Playback will
 * stop whenever audio focus is lost, and playback will pause whenever the system indicates that
 * playback is "becoming noisy" (e.g. headphones are being unplugged). Requests for transient audio
 * ducking result in the volume changing to the ducking volume specified by the volume profile (see
 * {@link #setVolumeProfile(VolumeProfile)}.
 */
public class PlaybackService extends Service implements AudioManager.OnAudioFocusChangeListener {
	/**
	 * Used to identify this class during debugging.
	 */
	private final String TAG = "[MusicService]";

	/**
	 * Whether or not the service will stop itself when an unrecoverable error occurs or playback
	 * completes.
	 */
	private boolean stopServiceAutomatically = false;

	/**
	 * Facilitates media playback to the user.
	 */
	private final MediaPlayer mediaPlayer = new MediaPlayer();

	/**
	 * Handles the callbacks from {@code mediaPlayer} rather than passing them directly to
	 * PlaybackService. This avoids exposing the internal structure of PlaybackService in the API
	 * signature which is undesirable.
	 */
	private final MediaPlayerCallbackDelegate callbackDelegate = new MediaPlayerCallbackDelegate();

	/**
	 * An external representation of the internal state of {@code mediaPlayer}.
	 */
	private MediaPlayerState mediaPlayerState = MediaPlayerState.IDLE;

	/**
	 * The source for media playback.
	 */
	private PlayableMedia mediaSource = null;

	/**
	 * The VolumeProfile to use during playback.
	 */
	private VolumeProfile volumeProfile;

	/**
	 * Whether or not to loop back to the start when playback completes. Although {@code
	 * mediaPlayer} supports looping, it sometimes ignores the request so the status is manually
	 * tracked.
	 */
	private boolean loopingEnabled = false;

	/**
	 * Manages the current operation, and delivers callbacks to the {@code
	 * onOperationStartedListener} and the {@code onOperationFinishedListener}.
	 */
	private final OperationManager operationManager = new OperationManager();

	/**
	 * Playback operations are executed on this thread to avoid blocking the main thread.
	 */
	private ExecutorService playbackExecutor = Executors.newSingleThreadExecutor();

	/**
	 * Callbacks are executed on this thread to avoid blocking playback.
	 */
	private final ExecutorService callbackExecutor = Executors.newSingleThreadExecutor();

	/**
	 * The listener to receive callbacks when playback completes.
	 */
	private final Set<OnPlaybackCompleteListener> onPlaybackCompleteListeners = new HashSet<>();

	/**
	 * The listener to receive callbacks when playbackExecutor is shutdown.
	 */
	private final Set<OnPendingOperationsCancelledListener>
			onPendingOperationsCancelledListeners = new HashSet<>();

	/**
	 * The listener to receive callbacks when a queued operation is started.
	 */
	private final Set<OnOperationStartedListener> onOperationStartedListeners = new HashSet<>();

	/**
	 * The listener to receive callbacks when the active operation completes.
	 */
	private final Set<OnOperationFinishedListener> onOperationFinishedListeners = new HashSet<>();

	/**
	 * Default value for the volume during audio focus ducking.
	 */
	private static final float DEFAULT_DUCKING_VOLUME = 0.1f;

	/**
	 * Default value for the volume during normal playback.
	 */
	private static final float DEFAULT_NORMAL_VOLUME = 1f;

	/**
	 * Default value for the transitioning to ducking duration.
	 */
	private static final int DEFAULT_DUCKING_TRANSITION_DURATION = 50;

	/**
	 * Default value for the transitioning to normal playback duration.
	 */
	private static final int DEFAULT_AUDIO_FOCUS_REGAINED_TRANSITION_DURATION = 500;

	/**
	 * A broadcast receiver used to pause playback when audio is becoming noisy.
	 */
	private final BroadcastReceiver audioBecomingNoisyReceiver =
			new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					Log.d(TAG, "[Becoming noisy intent intercepted]");

					if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
						if (mediaPlayerState == MediaPlayerState.STARTED) {
							pausePlayback();
						}
					}
				}
			};

	@Override
	public void onCreate() {
		super.onCreate();

		mediaPlayer.setOnCompletionListener(callbackDelegate);
		mediaPlayer.setOnErrorListener(callbackDelegate);

		volumeProfile = ImmutableVolumeProfile.newInstance()
				.withDuckingVolume(DEFAULT_DUCKING_VOLUME)
				.withNormalVolume(DEFAULT_NORMAL_VOLUME)
				.withDuckingTransitionDuration(DEFAULT_DUCKING_TRANSITION_DURATION)
				.withAudioFocusRegainedTransitionDuration
						(DEFAULT_AUDIO_FOCUS_REGAINED_TRANSITION_DURATION);

		registerReceiver(audioBecomingNoisyReceiver,
				new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mediaPlayer.release();
		unregisterReceiver(audioBecomingNoisyReceiver);
	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		// This method is designed to handle the ducking behaviour
		switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN: {
				final float volume = volumeProfile.getNormalVolume();
				final int duration = volumeProfile.getAudioFocusRegainedTransitionDuration();
				transitionPlaybackVolume(volume, volume, duration);
				break;
			}

			case AudioManager.AUDIOFOCUS_LOSS: {
				// Cancel any operation which may be running
				operationManager.declareCurrentOperationFinished(FailureMode.AUDIO_FOCUS_ERROR);

				// Pending operations should be cancelled since audio focus may not be returned
				restartPlaybackExecutor();

				if (mediaPlayerState == MediaPlayerState.STARTED) {
					FailureMode operationFailureMode = pauseMediaOperation();

					// If for any reason the pause operation failed, playback must still be
					// stopped to avoid ruining the user experience
					if (operationFailureMode != null) {
						resetMediaPlayer();
					}
				}

				break;
			}

			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
				final float volume = volumeProfile.getDuckingVolume();
				final int duration = volumeProfile.getDuckingTransitionDuration();
				transitionPlaybackVolume(volume, volume, duration);
				break;
			}

			default:
				Log.d(TAG, "[Unhandled audio focus type] [Type id: " + focusChange + "]");
		}
	}

	/**
	 * By default, this Service will run indefinitely when it is started by a call to {@link
	 * Context#startService(Intent)}. Passing true to this method sets the Service to automatically
	 * stop itself when playback completes (and looping is disabled), or when an unrecoverable error
	 * occurs. This Service still follows the standard lifecycle for Android Services, and will not
	 * shutdown if there are still bound clients (assuming BIND_AUTO_CREATE is used in {@link
	 * Context#bindService(Intent, ServiceConnection, int)}).
	 *
	 * @param stopServiceAutomatically
	 * 		true to set the service to automatically stop itself, false to disable this feature
	 */
	public void stopServiceAutomatically(boolean stopServiceAutomatically) {
		this.stopServiceAutomatically = stopServiceAutomatically;
	}

	/**
	 * Returns whether or not this service will automatically stop itself when playback completes or
	 * when an unrecoverable error occurs. See {@link #stopServiceAutomatically(boolean)}.
	 *
	 * @return true if this service will automatically stop itself, false otherwise
	 */
	public boolean serviceWillStopAutomatically() {
		return stopServiceAutomatically;
	}

	/**
	 * Terminates the current operation, clears pending operations, and terminates playback.
	 */
	public void reset() {
		// Cancels the current operation (if any), clears pending operations and calls the relevant
		// listeners
		postOperationProcedures(FailureMode.RESET_BY_CLIENT);
	}

	/**
	 * Queues an operation to change the current media source. The supplied media will be loaded
	 * into memory so that it can be played upon a request from {@link
	 * #requestPlayMediaOperation()}.
	 * <p/>
	 * This operation is not guaranteed to start as it may be cancelled prior to being executed.
	 * Successful completion is also not guaranteed; the operation will finish successfully when the
	 * media source has been loaded into memory, but it may finish unsuccessfully before then. If
	 * the operation fails, the reason for the failure will be provided in the
	 * OnOperationFinishedListener callback. Failure will result in all pending operations being
	 * cancelled.
	 *
	 * @param mediaSource
	 * 		the media source to use, not null
	 * @param headers
	 * 		the headers to be sent together with the request for the data, null to ignore this
	 * @throws IllegalArgumentException
	 * 		if {@code mediaSource} is null
	 */
	public synchronized void requestChangeMediaSourceOperation(final PlayableMedia mediaSource,
			final Map<String, String> headers) {
		if (mediaSource == null) {
			throw new IllegalArgumentException("mediaSource cannot be null");
		}

		playbackExecutor.execute(new Runnable() {
			@Override
			public void run() {
				operationManager.declareCurrentOperation(Operation.CHANGE_MEDIA_SOURCE);
				final FailureMode operationFailureMode = changeMediaSourceOperation(mediaSource,
						headers);
				postOperationProcedures(operationFailureMode);
			}
		});
	}

	/**
	 * Queues an operation to play the current media source. This operation will fail if the media
	 * has not been set with {@link #requestChangeMediaSourceOperation (PlayableMedia, Map)}.
	 * <p/>
	 * This operation is not guaranteed to start as it may be cancelled prior to being executed.
	 * Successful completion is also not guaranteed; the operation will finish successfully when
	 * playback of the current media starts, but it may finish unsuccessfully before then. If the
	 * operation fails, the failure mode will be provided in the OnOperationFinishedListener
	 * callback. If the failure mode is not recoverable, all pending operations will be cancelled
	 * (see {@link FailureMode#isRecoverable()}).
	 */
	public synchronized void requestPlayMediaOperation() {
		playbackExecutor.execute(new Runnable() {
			@Override
			public void run() {
				operationManager.declareCurrentOperation(Operation.PLAY);
				FailureMode operationFailureMode = playMediaOperation(); // null for success
				postOperationProcedures(operationFailureMode);
			}
		});
	}

	/**
	 * Queues an operation to pause playback of the current media source. This operation will fail
	 * if the media source is not currently playing or paused.
	 * <p/>
	 * This operation is not guaranteed to start as it may be cancelled prior to being executed.
	 * Successful completion is also not guaranteed; the operation will finish successfully when
	 * playback is paused, but it may finish unsuccessfully before then. If the operation fails, the
	 * failure mode will be provided in the OnOperationFinishedListener callback. If the failure
	 * mode is not recoverable, all pending operations will be cancelled (see {@link
	 * FailureMode#isRecoverable()}).
	 */
	public synchronized void requestPauseMediaOperation() {
		playbackExecutor.execute(new Runnable() {
			@Override
			public void run() {
				operationManager.declareCurrentOperation(Operation.PAUSE);
				FailureMode operationFailureMode = pauseMediaOperation(); // null for success
				postOperationProcedures(operationFailureMode);
			}
		});
	}

	/**
	 * Queues an operation to stop playback of the current media source. This operation will fail if
	 * media playback has not been started (paused is still valid).
	 * <p/>
	 * Stopping playback involves clearing the current media from memory. This operation is not
	 * guaranteed to start as it may be cancelled prior to being executed. Successful completion is
	 * also not guaranteed; the operation will finish successfully when playback is stopped, but it
	 * may finish unsuccessfully before then. If the operation fails, the failure mode will be
	 * provided in the OnOperationFinishedListener callback. If the failure mode is not recoverable,
	 * all pending operations will be cancelled (see {@link FailureMode#isRecoverable()}).
	 */
	public synchronized void requestStopMediaOperation() {
		playbackExecutor.execute(new Runnable() {
			@Override
			public void run() {
				operationManager.declareCurrentOperation(Operation.STOP);
				FailureMode operationFailureMode = stopMediaOperation();
				postOperationProcedures(operationFailureMode);
			}
		});
	}

	/**
	 * Queues an operation to seek to a particular position in the current playback. This method
	 * will fail if the media source has not been set or if playback has been stopped.
	 * <p/>
	 * This operation is not guaranteed to start as it may be cancelled prior to being executed.
	 * Successful completion is also not guaranteed; the operation will finish successfully when
	 * playback seeks, but it may finish unsuccessfully before then. If the operation fails, the
	 * failure mode will be provided in the OnOperationFinishedListener callback. If the failure
	 * mode is not recoverable, all pending operations will be cancelled (see {@link
	 * FailureMode#isRecoverable()}).
	 */
	public synchronized void requestSeekToOperation(final int offsetMs) {
		playbackExecutor.execute(new Runnable() {
			@Override
			public void run() {
				operationManager.declareCurrentOperation(Operation.SEEK_TO);
				FailureMode operationFailureMode = seekToOperation(offsetMs);
				postOperationProcedures(operationFailureMode);
			}
		});
	}

	/**
	 * Returns an enum specifying the operation which is currently being executed. If no operation
	 * is being executed at the time of invocation, then null is returned.
	 *
	 * @return the current operation, null if none is occurring
	 */
	public Operation getCurrentOperation() {
		return operationManager.getCurrentOperation();
	}

	/**
	 * Returns whether or not an operation could be run successfully at the current time. This
	 * method does not take into account any queued operations, and it cannot anticipate the effects
	 * of RuntimeExceptions.
	 *
	 * @param operation
	 * 		the operation to test
	 * @return true if {@code operation} could run right now, false otherwise
	 */
	public boolean isValidOperation(final Operation operation) {
		switch (operation) {
			case CHANGE_MEDIA_SOURCE: {
				// The state is always reset at the start of a change media operation
				return true;
			}

			case PLAY: {
				return (mediaPlayerState.canPrepare() || mediaPlayerState.canStart());
			}

			case PAUSE: {
				return mediaPlayerState.canPause();
			}

			case STOP: {
				return mediaPlayerState.canStop();
			}

			case SEEK_TO: {
				return mediaPlayerState.canSeekTo();
			}

			default: {
				return false;
			}
		}
	}

	/**
	 * @return the current media source, null if there is none
	 */
	public PlayableMedia getMediaSource() {
		return mediaSource;
	}

	/**
	 * Sets the volume profile to use during playback.
	 *
	 * @param volumeProfile
	 * 		the profile to use, not null
	 * @throws IllegalArgumentException
	 * 		if {@code profile} is null
	 */
	public void setVolumeProfile(VolumeProfile volumeProfile) {
		final float oldVolume = this.volumeProfile.getNormalVolume();

		if (volumeProfile == null) {
			throw new IllegalArgumentException("profile cannot be null");
		}

		this.volumeProfile = volumeProfile;

		// Make change instantaneous
		transitionPlaybackVolume(oldVolume, volumeProfile.getNormalVolume(), 0);
	}

	/**
	 * @return the volume profile currently in use, not null
	 */
	public VolumeProfile getVolumeProfile() {
		return this.volumeProfile;
	}

	/**
	 * Returns the current playback position. This method will only return meaningful data if the
	 * media has been started, paused, stopped, prepared or finished.
	 *
	 * @return the current playback position measured in milliseconds, or -1 if the query is not
	 * valid
	 */
	public int getCurrentPosition() {
		if (mediaPlayerState.canGetCurrentPosition()) {
			return mediaPlayer.getCurrentPosition();
		} else {
			return -1;
		}
	}

	/**
	 * Sets whether or not the media should loop once the end has been reached.
	 *
	 * @param loopingEnabled
	 * 		whether or not the media should loop
	 */
	public void enableLooping(boolean loopingEnabled) {
		this.loopingEnabled = loopingEnabled;
	}

	/**
	 * @return true if the media will play again when it finishes, false otherwise
	 */
	public boolean loopingIsEnabled() {
		return loopingEnabled;
	}

	/**
	 * @return true if media is currently playing, false otherwise
	 */
	public boolean isPlaying() {
		return (mediaPlayerState == MediaPlayerState.STARTED);
	}

	/**
	 * Sets the {@link SurfaceHolder} to use for displaying the video portion of the media. Not
	 * calling this method or supplying null will result in only audio being played.
	 *
	 * @param surfaceHolder
	 * 		the SurfaceHolder to use for video display
	 */
	public void setDisplay(SurfaceHolder surfaceHolder) {
		mediaPlayer.setDisplay(surfaceHolder);
	}

	/**
	 * Sets whether or not to keep the screen on while video playback is occurring. This method does
	 * not require the low-level wake lock permission.
	 *
	 * @param keepScreenOn
	 * 		true to keep the screen on during playback, false to allow it to turn off
	 */
	public void keepScreenAwakeWhilePlaying(boolean keepScreenOn) {
		mediaPlayer.setScreenOnWhilePlaying(keepScreenOn);
	}

	/**
	 * Returns the width of the video portion of the media. If there is no video, no display surface
	 * as been set or the width has not yet been determined, then 0 is returned.
	 *
	 * @return the width of the video
	 */
	public int getVideoWidth() {
		return mediaPlayerState.canGetVideoHeightAndWidth() ? mediaPlayer.getVideoWidth() : 0;
	}

	/**
	 * Returns the height of the video portion of the media. If there is no video, no display
	 * surface as been set or the height has not yet been determined, then 0 is returned.
	 *
	 * @return the height of the video
	 */
	public int getVideoHeight() {
		return mediaPlayerState.canGetVideoHeightAndWidth() ? mediaPlayer.getVideoHeight() : 0;
	}

	/**
	 * Registers an OnPlaybackCompleteListener to receive callbacks from this PlaybackService.
	 * Callbacks are not delivered on the main thread or the playback thread, instead they are
	 * delivered on a separate callback thread. Only one callback thread exists for each
	 * PlaybackService, so only one callback event can execute at a time.
	 *
	 * @param listener
	 * 		the listener to register
	 */
	public void addOnPlaybackCompleteListener(OnPlaybackCompleteListener listener) {
		if (listener != null) {
			onPlaybackCompleteListeners.add(listener);
		}
	}

	/**
	 * Removes an OnPlaybackCompleteListener from this PlaybackService. The method returns normally
	 * if the supplied listener is not registered.
	 *
	 * @param listener
	 * 		the listener to unregister
	 */
	public void removeOnPlaybackCompleteListener(OnPlaybackCompleteListener listener) {
		onPlaybackCompleteListeners.remove(listener);
	}

	/**
	 * Registers an OnPendingOperationsCancelledListener to receive callbacks from this
	 * PlaybackService. Callbacks are not delivered on the main thread or the playback thread,
	 * instead they are delivered on a separate callback thread. Only one callback thread exists for
	 * each PlaybackService, so only one callback event can execute at a time.
	 *
	 * @param listener
	 * 		the listener to register
	 */
	public void addOnPendingOperationsCancelledListener(OnPendingOperationsCancelledListener
			listener) {
		if (listener != null) {
			onPendingOperationsCancelledListeners.add(listener);
		}
	}

	/**
	 * Removes an OnPendingOperationsClearedListener from this PlaybackService. The method returns
	 * normally if the supplied listener is not registered.
	 *
	 * @param listener
	 * 		the listener to unregister
	 */
	public void removeOnPendingOperationsCancelledListener(OnPendingOperationsCancelledListener
			listener) {
		onPendingOperationsCancelledListeners.remove(listener);
	}

	/**
	 * Registers an OnOperationStartedListener to receive callbacks from this PlaybackService.
	 * Callbacks are not delivered on the main thread or the playback thread, instead they are
	 * delivered on a separate callback thread. Only one callback thread exists for each
	 * PlaybackService, so only one callback event can execute at a time.
	 *
	 * @param listener
	 * 		the listener to register
	 */
	public void addOnOperationStartedListener(OnOperationStartedListener listener) {
		if (listener != null) {
			onOperationStartedListeners.add(listener);
		}
	}

	/**
	 * Removes an OnOperationStartedListener from this PlaybackService. The method returns normally
	 * if the supplied listener is not registered.
	 *
	 * @param listener
	 * 		the listener to unregister
	 */
	public void removeOnOperationStartedListener(OnOperationStartedListener listener) {
		onOperationStartedListeners.remove(listener);
	}

	/**
	 * Registers an OnOperationFinishedListener to receive callbacks from this PlaybackService.
	 * Callbacks are not delivered on the main thread or the playback thread, instead they are
	 * delivered on a separate callback thread. Only one callback thread exists for each
	 * PlaybackService, so only one callback event can execute at a time.
	 *
	 * @param listener
	 * 		the listener to receive the callbacks
	 */
	public void addOnOperationFinishedListener(OnOperationFinishedListener listener) {
		if (listener != null) {
			onOperationFinishedListeners.add(listener);
		}
	}

	/**
	 * Removes an OnOperationFinishedListener from this PlaybackService. The method returns normally
	 * if the supplied listener is not registered.
	 *
	 * @param listener
	 * 		the listener to unregister
	 */
	public void removeOnOperationFinishedListener(OnOperationFinishedListener listener) {
		onOperationFinishedListeners.remove(listener);
	}

	/**
	 * Operation to change the media source of this PlaybackService. This method should not be
	 * called on the main thread.
	 *
	 * @param mediaSource
	 * 		the source to change to
	 * @param headers
	 * 		the headers to be sent together with the request for the data, null to ignore this
	 * @return the failure mode of the operation, null if the operation finished without error
	 */
	private synchronized FailureMode changeMediaSourceOperation(final PlayableMedia mediaSource,
			final
			Map<String, String> headers) {
		try {
			resetMediaPlayer();

			if (mediaPlayerState.canSetDataSource()) {
				try {
					setMediaSource(mediaSource, headers);
				} catch (IOException e) {
					return FailureMode.MEDIA_ACCESS_ERROR;
				}
			} else {
				Log.e(TAG,
						"[changeMediaSourceOperation] [state error: state should always allow " +
								"setting media source after reset]");

				// Not a usual invalid state error, so don't return FailureMode.INVALID_STATE
				return FailureMode.UNDEFINED;
			}

			if (mediaPlayerState.canPrepare()) {
				try {
					prepareMedia();
					PlaybackService.this.mediaSource =
							mediaSource; // Only set after prepare succeeds
				} catch (IOException e) {
					return FailureMode.MEDIA_ACCESS_ERROR;
				}
			} else {
				Log.e(TAG,
						"[changeMediaSourceOperation] [state error: state should always allow " +
								"preparing after setting media]");

				// Not a usual invalid state error, so don't return FailureMode.INVALID_STATE
				return FailureMode.UNDEFINED;
			}
		} catch (Exception e) {
			return FailureMode.UNDEFINED;
		}

		// Return null at the end, to ensure that the entire method runs before 'no-error' is returned
		return null;
	}

	/**
	 * Operation to start/resume playback if possible. This method should not be called on the main
	 * thread.
	 *
	 * @return the failure mode of the operation, null if the operation finished without error
	 */
	private synchronized FailureMode playMediaOperation() {
		try {
			// Media will need to be prepared again if previously stopped or completed, but an
			// invalid state error shouldn't be thrown because this step isn't always necessary
			if (mediaPlayerState.canPrepare()) {
				try {
					prepareMedia();
				} catch (IOException e) {
					return FailureMode.MEDIA_ACCESS_ERROR;
				}
			}

			if (mediaPlayerState.canStart()) {
				final boolean playbackStartedSuccessfully = startPlayback();

				if (!playbackStartedSuccessfully) {
					return FailureMode.AUDIO_FOCUS_ERROR;
				}
			} else {
				return FailureMode.INVALID_STATE;
			}
		} catch (Exception e) {
			return FailureMode.UNDEFINED;
		}

		// Return null at the end, to ensure that the entire method runs before 'no-error' is returned
		return null;
	}

	/**
	 * Operation to pause playback if possible. This method should not be called on the main
	 * thread.
	 *
	 * @return the failure mode of the operation, null if the operation finished without error
	 */
	private synchronized FailureMode pauseMediaOperation() {
		try {
			if (mediaPlayerState.canPause()) {
				pausePlayback();
			} else {
				return FailureMode.INVALID_STATE;
			}
		} catch (Exception e) {
			return FailureMode.UNDEFINED;
		}

		// Return null at the end, to ensure that the entire method runs before 'no-error' is returned
		return null;
	}

	/**
	 * Operation to stop playback if possible. This method should not be called on the main thread.
	 *
	 * @return the failure mode of the operation, null if the operation finished without error
	 */
	private synchronized FailureMode stopMediaOperation() {
		try {
			if (mediaPlayerState.canStop()) {
				stopPlayback();
			} else {
				return FailureMode.INVALID_STATE;
			}
		} catch (Exception e) {
			return FailureMode.UNDEFINED;
		}

		// Return null at the end, to ensure that the entire method runs before 'no-error' is returned
		return null;
	}

	/**
	 * Operation to seek to the specified position in the current media source.
	 *
	 * @param offsetMs
	 * 		the offset to seek to from the start of the media source, measured in milliseconds
	 * @return the failure mode of the operation, null if the operation finished without error
	 */
	private synchronized FailureMode seekToOperation(final int offsetMs) {
		try {
			if (mediaPlayerState.canSeekTo()) {
				if (offsetMs > mediaPlayer.getDuration() || offsetMs < 0) {
					return FailureMode.INVALID_SEEKING;
				} else {
					mediaPlayer.seekTo(offsetMs);
				}
			} else {
				return FailureMode.INVALID_STATE;
			}
		} catch (Exception e) {
			return FailureMode.UNDEFINED;
		}

		// Return null at the end, to ensure that the entire method runs before 'no-error' is returned
		return null;
	}

	/**
	 * Changes the media source and updates the media player state. Take care to only call this
	 * method if the internal state of mediaPlayer allows {@link MediaPlayer#setDataSource(Context,
	 * Uri)} to be called at this time.
	 *
	 * @param mediaSource
	 * 		the media source to change to
	 * @param headers
	 * 		the headers to be sent together with the request for the data, null to ignore this
	 * @throws IOException
	 * 		if some IO error occurs when accessing the media source
	 */
	private synchronized void setMediaSource(final PlayableMedia mediaSource, final Map<String,
			String> headers) throws
			IOException {

		if (headers == null) {
			mediaPlayer.setDataSource(getApplicationContext(), mediaSource.getUri());
		} else {
			mediaPlayer.setDataSource(getApplicationContext(), mediaSource.getUri(), headers);
		}

		mediaPlayerState = MediaPlayerState.INITIALISED;
	}

	/**
	 * Loads the current media source into memory and updated the media player state. Take care to
	 * only call this method if the internal state of mediaPlayer allows {@link
	 * MediaPlayer#prepare()} to be called at this time.
	 *
	 * @throws IOException
	 * 		if some IO error occurs when accessing the media source
	 */
	private synchronized void prepareMedia() throws IOException {
		mediaPlayer.prepare();
		mediaPlayerState = MediaPlayerState.PREPARED;
	}

	/**
	 * Start/resumes playback if audio focus can be obtained. Take care to only call this method if
	 * the internal state of mediaPlayer allows {@link MediaPlayer#start()} to be called at this
	 * time.
	 *
	 * @return true if audio focus was granted and playback started, false otherwise
	 */
	private synchronized boolean startPlayback() {
		final boolean audioFocusGranted = AudioFocusHelper
				.requestStreamMusicFocus(getApplicationContext(), PlaybackService.this);

		if (audioFocusGranted) {
			mediaPlayer.start();
			mediaPlayerState = MediaPlayerState.STARTED;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Pauses playback.
	 */
	private synchronized void pausePlayback() {
		mediaPlayer.pause();
		mediaPlayerState = MediaPlayerState.PAUSED;
		AudioFocusHelper.abandonFocus(getApplicationContext(), PlaybackService.this);
	}

	/**
	 * Stops playback and abandons audio focus. Take care to only call this method if mediaPlayer's
	 * internal state allows.
	 */
	private synchronized void stopPlayback() {
		mediaPlayer.stop();
		mediaPlayerState = MediaPlayerState.STOPPED;
		AudioFocusHelper.abandonFocus(getApplicationContext(), PlaybackService.this);
	}

	/**
	 * Common procedures to run after each operation finishes.
	 *
	 * @param operationFailureMode
	 * 		the FailureMode returned by the operation upon finishing
	 */
	private synchronized void postOperationProcedures(final FailureMode operationFailureMode) {
		operationManager.declareCurrentOperationFinished(operationFailureMode);

		if (operationFailureMode != null) {
			if (!operationFailureMode.isRecoverable()) {
				mediaPlayerState = MediaPlayerState.ERROR;
				restartPlaybackExecutor();
				resetMediaPlayer();
				stopSelf();
			}
		}
	}

	/**
	 * Clears the queue of operations in the playback executor and calls the registered
	 * OnPendingOperationsCancelledListener.
	 */
	private synchronized void restartPlaybackExecutor() {
		callOnPendingOperationsCancelledListener();
		playbackExecutor.shutdownNow();
		playbackExecutor = Executors.newSingleThreadExecutor();
	}

	/**
	 * Resets the media player and updates the state accordingly.
	 */
	private synchronized void resetMediaPlayer() {
		mediaPlayer.reset();
		mediaPlayerState = MediaPlayerState.IDLE;
	}

	/**
	 * Transitions the playback volume over a period of time.
	 *
	 * @param startVolume
	 * 		the volume at the start of the transition, as a decimal between 0 (silent) and 1 (maximum)
	 * @param endVolume
	 * 		the volume at the end of the transition, as a decimal between 0 (silent) and 1
	 * @param duration
	 * 		the duration to use for the transition
	 */
	private synchronized void transitionPlaybackVolume(float startVolume, final float endVolume,
			long duration) {
		ValueAnimator changeVolume = ValueAnimator.ofFloat(startVolume, endVolume);
		changeVolume.setDuration(duration);
		changeVolume.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				final float volume = (float) animation.getAnimatedValue();
				mediaPlayer.setVolume(volume, volume);
			}
		});
		changeVolume.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				// Make sure volume changed
				mediaPlayer.setVolume(endVolume, endVolume);
			}
		});
		changeVolume.start();
	}

	/**
	 * Invokes the callback method in the each registered OnPlaybackCompleteListener of this
	 * PlaybackService.
	 */
	private void callOnPlaybackCompleteListener() {
		callbackExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for (OnPlaybackCompleteListener l : onPlaybackCompleteListeners) {
					l.onPlaybackComplete(PlaybackService.this, mediaSource);
				}
			}
		});
	}

	/**
	 * Invokes the callback method in the each registered OnPendingOperationsCancelledListener of
	 * this PlaybackService.
	 */
	private void callOnPendingOperationsCancelledListener() {
		callbackExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for (OnPendingOperationsCancelledListener l :
						onPendingOperationsCancelledListeners) {
					l.onPendingOperationsCancelled(PlaybackService.this);
				}
			}
		});
	}

	/**
	 * Invokes the callback method in each registered OnOperationStartedListener of this
	 * PlaybackService. No callbacks will be issued if {@code operation} is null.
	 *
	 * @param operation
	 * 		he operation which started
	 */
	private void callOnOperationStartedListener(final Operation operation) {
		callbackExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (operation != null) {
					for (OnOperationStartedListener l : onOperationStartedListeners) {
						l.OnOperationStarted(PlaybackService.this, operation);
					}
				}
			}
		});
	}

	/**
	 * Invokes the callback method in each registered OnOperationFinishedListener of this
	 * PlaybackService. No callbacks will be issued if {@code operation} is null.
	 *
	 * @param operation
	 * 		the operation which finished
	 * @param failureMode
	 * 		the failure mode returned by the operation, null if the operation finished without errors
	 */
	private void callOnOperationFinishedListener(final Operation operation,
			final FailureMode failureMode) {
		callbackExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (operation != null) {
					for (OnOperationFinishedListener l : onOperationFinishedListeners) {
						l.onOperationFinished(PlaybackService.this,
								operation, failureMode);
					}
				}
			}
		});
	}

	/**
	 * Interface definition for a callback to be invoked whenever a {@link PlaybackService} finishes
	 * playing media. If looping is enabled, the callback will be delivered each time the media
	 * finishes.
	 */
	public interface OnPlaybackCompleteListener {
		void onPlaybackComplete(PlaybackService service, PlayableMedia completedMedia);
	}

	/**
	 * Interface definition for a callback to be invoked whenever the pending operations of a {@link
	 * PlaybackService} are cancelled.
	 */
	public interface OnPendingOperationsCancelledListener {
		/**
		 * Called whenever a PlaybackService clears pending playback operations. Note that this
		 * callback could be invoked even if there are no operations to clear.
		 */
		void onPendingOperationsCancelled(PlaybackService service);
	}

	/**
	 * Interface definition for a callback to be invoked whenever a {@link PlaybackService} starts
	 * an operation.
	 */
	public interface OnOperationStartedListener {
		/**
		 * Called whenever a PlaybackService starts a new operation.
		 *
		 * @param service
		 * 		the service which started the operation, not null
		 * @param operation
		 * 		the operation which started, not null
		 */
		void OnOperationStarted(PlaybackService service, Operation operation);
	}

	/**
	 * Interface definition for a callback to be invoked whenever a {@link PlaybackService} finishes
	 * an operation, either by completion or failure.
	 */
	public interface OnOperationFinishedListener {
		/**
		 * Called whenever a PlaybackService finishes an operation.
		 *
		 * @param service
		 * 		the service which started the operation, not null
		 * @param operation
		 * 		the operation which completed, not null
		 * @param failureMode
		 * 		the failure mode returned by the operation, null if the operation finished without
		 * 		errors
		 */
		void onOperationFinished(PlaybackService service, Operation operation, FailureMode
				failureMode);
	}

	/**
	 * The operations which a PlaybackService can perform.
	 */
	public enum Operation {
		/**
		 * Cancels all pending operations, stops playback and sets the media source.
		 */
		CHANGE_MEDIA_SOURCE,

		/**
		 * Starts/resumes playback.
		 */
		PLAY,

		/**
		 * Pauses playback.
		 */
		PAUSE,

		/**
		 * Terminates playback and unloads the current media from memory.
		 */
		STOP,

		/**
		 * Moves the playback position.
		 */
		SEEK_TO
	}

	/**
	 * The ways in which a PlaybackService operation can fail. A FailureMode can either be
	 * recoverable or unrecoverable. When a recoverable error occurs, the PlaybackService will
	 * terminate the current operation but queued operations will not be cleared. When an
	 * unrecoverable error occurs, the PlaybackService will terminate the current operation and all
	 * pending operations will be cancelled. Additionally, unrecoverable errors will cause the
	 * service to call {@link Service#stopSelf()}.
	 */
	public enum FailureMode {
		/**
		 * Indicates that some error has occurred in relation to accessing the media provided to
		 * {@link PlaybackService#requestChangeMediaSourceOperation(PlayableMedia, Map)}.
		 */
		MEDIA_ACCESS_ERROR(false),

		/**
		 * Indicates that audio focus could not be obtained or was revoked.
		 */
		AUDIO_FOCUS_ERROR(true),

		/**
		 * Indicates that the requested operation could not be performed given the current state.
		 * For example, this error will occur if a play media request is made before a change media
		 * source request.
		 */
		INVALID_STATE(true),

		/**
		 * Specific to the seeking operation. Indicates that the requested seeking position was
		 * outside the time-limits of the media.
		 */
		INVALID_SEEKING(true),

		/**
		 * Indicates that some unknown/unforeseen error has occurred.
		 */
		UNDEFINED(false),

		/**
		 * Indicates that the operation was finished by the client calling {@link
		 * PlaybackService#reset()}.
		 */
		RESET_BY_CLIENT(false);

		/**
		 * Whether or not queued operations can still be processed.
		 */
		final boolean recoverable;

		/**
		 * Used to construct the FailureMode constants.
		 *
		 * @param recoverable
		 * 		whether or not the PlaybackService can continue processing operations
		 */
		FailureMode(boolean recoverable) {
			this.recoverable = recoverable;
		}

		/**
		 * Whether or not the PlaybackService can continue processing operations after this error
		 * occurs. When a recoverable error occurs, the PlaybackService disregards the failed
		 * operation and continues to the next in the queue. When an unrecoverable error occurs, all
		 * pending operations are cancelled and the relevant callbacks are delivered.
		 *
		 * @return whether or not the PlaybackService can continue processing operations
		 */
		public boolean isRecoverable() {
			return recoverable;
		}
	}

	/**
	 * Manages the operation which is currently occurring and delivers callbacks to the
	 * OnOperationStartedListener and the OnOperationFinishedListener when appropriate.
	 */
	private class OperationManager {
		/**
		 * The operation which is currently occurring, null if there is none
		 */
		Operation currentOperation;

		/**
		 * Constructs a new OperationManager instance.
		 */
		OperationManager() {
			this.currentOperation = null;
		}

		/**
		 * @return the operation which is currently occurring
		 */
		public Operation getCurrentOperation() {
			return currentOperation;
		}

		/**
		 * Declares that a new operation has started. The OnOperationStartedListener of the
		 * PlaybackService running the operation is called when this method is invoked. To clear the
		 * current operation, invoke {@link #declareCurrentOperationFinished(FailureMode)}.
		 *
		 * @param currentOperation
		 * 		the operation which has started
		 * @throws IllegalArgumentException
		 * 		if {@code currentOperation} is null
		 */
		public void declareCurrentOperation(final Operation currentOperation) {
			if (currentOperation == null) {
				throw new IllegalArgumentException("currentOperation cannot be null");
			}

			this.currentOperation = currentOperation;
			callOnOperationStartedListener(currentOperation);
		}

		/**
		 * Declares that the current operation has finished, either by completion or failure. The
		 * OnOperationFinishedListener of the PlaybackService which was running the operation is
		 * called when this method is invoked.
		 *
		 * @param failureMode
		 * 		the failure mode returned by the operation, null if the operation completed
		 * 		successfully
		 */
		public void declareCurrentOperationFinished(final FailureMode failureMode) {
			callOnOperationFinishedListener(currentOperation, failureMode);
			this.currentOperation = null;
		}
	}

	/**
	 * Provides the binding client to get a reference to the bound service.
	 */
	public final class LocalBinder extends Binder {
		/**
		 * @return the bound service
		 */
		public final PlaybackService getService() {
			return PlaybackService.this;
		}
	}

	/**
	 * Handles the callbacks from {@code mediaPlayer} rather than passing them directly to
	 * PlaybackService. This avoids exposing the internal structure of PlaybackService in the API
	 * signature which is undesirable.
	 */
	private final class MediaPlayerCallbackDelegate implements MediaPlayer.OnCompletionListener,
			MediaPlayer.OnErrorListener {
		@Override
		public void onCompletion(MediaPlayer mp) {
			mediaPlayerState = MediaPlayerState.PLAYBACK_COMPLETE;
			callOnPlaybackCompleteListener();

			if (loopingEnabled) {
				playMediaOperation();
			} else if (stopServiceAutomatically) {
				stopSelf();
			}
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			// Cancels the current operation (if any), clears pending operations, and calls
			// the relevant listeners
			postOperationProcedures(FailureMode.UNDEFINED);
			return true;
		}
	}
}