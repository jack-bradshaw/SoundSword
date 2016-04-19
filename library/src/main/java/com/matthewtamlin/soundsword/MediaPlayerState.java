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

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * An external representation of the internal state of {@link android.media.MediaPlayer}. The
 * methods of this class indicate which methods of MediaPlayer can be called from a particular
 * state. See the MediaPlayer documentation for more information about its internal state affect its
 * behaviour.
 */
public enum MediaPlayerState {
	/**
	 * The idle state of MediaPlayer.
	 */
	IDLE(false, false, false, false, false, true, true, false, true, true, true),

	/**
	 * The initialised state of MediaPlayer.
	 */
	INITIALISED(true, false, false, false, false, false, true, true, true, true, true),

	/**
	 * The prepared state of MediaPlayer.
	 */
	PREPARED(false, true, false, true, true, false, true, false, true, true, true),

	/**
	 * The started state of MediaPlayer.
	 */
	STARTED(false, true, true, true, true, false, true, false, true, true, true),

	/**
	 * The paused state of MediaPlayer.
	 */
	PAUSED(false, true, true, true, true, false, true, false, true, true, true),

	/**
	 * The playback complete state of MediaPlayer.
	 */
	PLAYBACK_COMPLETE(false, true, true, true, true, false, true, false, true, true, true),

	/**
	 * The stopped state of MediaPlayer.
	 */
	STOPPED(true, false, false, true, false, false, true, true, true, true, true),

	/**
	 * The preparing state of MediaPlayer.
	 */
	PREPARING(false, false, false, false, false, false, true, false, true, false, false),

	/**
	 * The error state of MediaPlayer.
	 */
	ERROR(false, false, false, false, false, false, true, false, true, false, false),

	/**
	 * The end state of MediaPlayer.
	 */
	END(false, false, false, false, false, false, true, false, true, false, false);

	/**
	 * Whether or not the {@link MediaPlayer#prepare()} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown.
	 */
	private final boolean canPrepare;

	/**
	 * Whether or not the {@link MediaPlayer#start()} method can be called in this MediaPlayerState
	 * without an IllegalStateException being thrown.
	 */
	private final boolean canStart;

	/**
	 * Whether or not the {@link MediaPlayer#pause()} method can be called in this MediaPlayerState
	 * without an IllegalStateException being thrown.
	 */
	private final boolean canPause;

	/**
	 * Whether or not the {@link MediaPlayer#stop()} method can be called in this MediaPlayerState
	 * without an IllegalStateException being thrown.
	 */
	private final boolean canStop;

	/**
	 * Whether or not the {@link MediaPlayer#seekTo(int)} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown.
	 */
	private final boolean canSeekTo;

	/**
	 * Whether or not the {@link MediaPlayer#setDataSource(Context, Uri)} method (or the overloaded
	 * variants) can be called in this MediaPlayerState without an IllegalStateException being
	 * thrown.
	 */
	private final boolean canSetDataSource;

	/**
	 * Whether or not the {@link MediaPlayer#reset()} method can be called in this MediaPlayerState
	 * without an IllegalStateException being thrown.
	 */
	private final boolean canReset;

	/**
	 * Whether or not the {@link MediaPlayer#prepareAsync()} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown.
	 */
	private final boolean canPrepareAsync;

	/**
	 * Whether or not the {@link MediaPlayer#release()} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown.
	 */
	private final boolean canRelease;

	/**
	 * Whether or not the {@link MediaPlayer#getCurrentPosition()} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown.
	 */
	private final boolean canGetCurrentPosition;

	/**
	 * Whether or not the {@link MediaPlayer#getVideoHeight()} and {@link
	 * MediaPlayer#getVideoWidth()} methods can be called in this MediaPlayerState without an
	 * IllegalStateException being thrown.
	 */
	private final boolean canGetVideoHeightAndWidth;

	/**
	 * Used to initialise the MediaPlayerState enum constants.
	 *
	 * @param canPrepare
	 * 		whether or not the {@link MediaPlayer#prepare()} method can be called in this
	 * 		MediaPlayerState without an IllegalStateException being thrown
	 * @param canStart
	 * 		whether or not the {@link MediaPlayer#start()} method can be called in this
	 * 		MediaPlayerState without an IllegalStateException being thrown
	 * @param canPause
	 * 		whether or not the {@link MediaPlayer#pause()} method can be called in this
	 * 		MediaPlayerState without an IllegalStateException being thrown
	 * @param canStop
	 * 		whether or not the {@link MediaPlayer#stop()} method can be called in this MediaPlayerState
	 * 		without an IllegalStateException being thrown
	 * @param canSeekTo
	 * 		whether or not the {@link MediaPlayer#seekTo(int)} method can be called in this
	 * 		MediaPlayerState without an IllegalStateException being thrown
	 * @param canSetDataSource
	 * 		whether or not the {@link MediaPlayer#setDataSource(Context, Uri)} method (or the
	 * 		overloaded variants) can be called in this MediaPlayerState without an
	 * 		IllegalStateException being thrown
	 * @param canReset
	 * 		whether or not the {@link MediaPlayer#reset()} method can be called in this
	 * 		MediaPlayerState without an IllegalStateException being thrown
	 * @param canPrepareAsync
	 * 		whether or not the {@link MediaPlayer#prepareAsync()} method can be called in this
	 * 		MediaPlayerState without an IllegalStateException being thrown
	 * @param canRelease
	 * 		whether or not the {@link MediaPlayer#release()} method can be called in this
	 * 		MediaPlayerState without an IllegalStateException being thrown
	 * @param canGetCurrentPosition
	 * 		whether or not the {@link MediaPlayer#getCurrentPosition()} method can be called in this
	 * 		MediaPlayerState without an IllegalStateException being thrown
	 * @param canGetVideoHeightAndWidth
	 * 		whether or not the {@link MediaPlayer#getVideoHeight()} and {@link
	 * 		MediaPlayer#getVideoWidth()} methods can be called in this MediaPlayerState without an
	 * 		IllegalStateException being thrown
	 */
	MediaPlayerState(final boolean canPrepare, final boolean canStart, final boolean canPause,
			final boolean canStop, final boolean canSeekTo, final boolean canSetDataSource,
			final boolean canReset, final boolean canPrepareAsync, final boolean canRelease,
			final boolean canGetCurrentPosition, final boolean canGetVideoHeightAndWidth) {
		this.canPrepare = canPrepare;
		this.canStart = canStart;
		this.canPause = canPause;
		this.canStop = canStop;
		this.canSeekTo = canSeekTo;
		this.canSetDataSource = canSetDataSource;
		this.canReset = canReset;
		this.canPrepareAsync = canPrepareAsync;
		this.canRelease = canRelease;
		this.canGetCurrentPosition = canGetCurrentPosition;
		this.canGetVideoHeightAndWidth = canGetVideoHeightAndWidth;
	}

	/**
	 * @return true if the {@link MediaPlayer#prepare()} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown, false otherwise
	 */
	public boolean canPrepare() {
		return canPrepare;
	}

	/**
	 * @return true if the {@link MediaPlayer#start()} method can be called in this MediaPlayerState
	 * without an IllegalStateException being thrown, false otherwise
	 */
	public boolean canStart() {
		return canStart;
	}

	/**
	 * @return true if the {@link MediaPlayer#pause()} method can be called in this MediaPlayerState
	 * without an IllegalStateException being thrown, false otherwise
	 */
	public boolean canPause() {
		return canPause;
	}

	/**
	 * @return true if the {@link MediaPlayer#stop()} method can be called in this MediaPlayerState
	 * without an IllegalStateException being thrown, false otherwise
	 */
	public boolean canStop() {
		return canStop;
	}

	/**
	 * @return true if the {@link MediaPlayer#seekTo(int)} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown, false otherwise
	 */
	public boolean canSeekTo() {
		return canSeekTo;
	}

	/**
	 * @return true if the {@link MediaPlayer#setDataSource(Context, Uri)} method (or the overloaded
	 * variants) can be called in this MediaPlayerState without an IllegalStateException being
	 * thrown, false otherwise
	 */
	public boolean canSetDataSource() {
		return canSetDataSource;
	}

	/**
	 * @return true if the {@link MediaPlayer#reset()} method can be called in this MediaPlayerState
	 * without an IllegalStateException being thrown, false otherwise
	 */
	public boolean canReset() {
		return canReset;
	}

	/**
	 * @return true if the {@link MediaPlayer#prepareAsync()} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown, false otherwise
	 */
	public boolean canPrepareAsync() {
		return canPrepareAsync;
	}

	/**
	 * @return true if the {@link MediaPlayer#release()} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown, false otherwise
	 */
	public boolean canRelease() {
		return canRelease;
	}

	/**
	 * @return true if the {@link MediaPlayer#getCurrentPosition()} method can be called in this
	 * MediaPlayerState without an IllegalStateException being thrown, false otherwise
	 */
	public boolean canGetCurrentPosition() {
		return canGetCurrentPosition;
	}

	/**
	 * @return true if the {@link MediaPlayer#getVideoHeight()} and {@link
	 * MediaPlayer#getVideoWidth()} methods can be called in this MediaPlayerState without an
	 * IllegalStateException being thrown, false otherwise
	 */
	public boolean canGetVideoHeightAndWidth() {
		return canGetVideoHeightAndWidth;
	}
}