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

/**
 * An Immutable implementation of the {@link VolumeProfile} interface.
 */
public final class ImmutableVolumeProfile implements VolumeProfile {
	/**
	 * The volume to use during normal playback.
	 */
	private final float normalVolume;

	/**
	 * The volume to use during audio focus ducking.
	 */
	private final float duckingVolume;

	/**
	 * The duration for transitioning to {@code duckingVolume} when an audio focus ducking request
	 * is received from the system, measured in milliseconds.
	 */
	private final int duckingTransitionDuration;

	/**
	 * The duration for transitioning to {@code normalVolume} after audio ducking, measured in
	 * milliseconds.
	 */
	private final int audioFocusRegainedTransitionDuration;

	/**
	 * Constructs a new ImmutableVolumeProfile instance.
	 *
	 * @param normalVolume
	 * 		the volume to use during normal playback, as a decimal between 0 (silent) and 1 (maximum)
	 * @param duckingVolume
	 * 		the volume to use during audio focus ducking, as a decimal between 0 (silent) and 1
	 * 		(maximum)
	 * @param duckingTransitionDuration
	 * 		the duration for transitioning to {@code duckingVolume} when an audio focus ducking request
	 * 		is received from the system
	 * @param audioFocusRegainedTransitionDuration
	 * 		duration for transitioning to {@code normalVolume} after audio ducking
	 */
	private ImmutableVolumeProfile(final float normalVolume, final float duckingVolume,
			final int duckingTransitionDuration,
			final int audioFocusRegainedTransitionDuration) {
		this.normalVolume = normalVolume;
		this.duckingVolume = duckingVolume;
		this.duckingTransitionDuration = duckingTransitionDuration;
		this.audioFocusRegainedTransitionDuration = audioFocusRegainedTransitionDuration;
	}

	/**
	 * Returns a new ImmutableVolumeProfile with the following default values: <ul> <li>Normal
	 * volume: 1</li> <li>Ducking volume: 0</li>  <li>Ducking transition duration: 0</li> <li>Audio
	 * focus regained transition duration: 0</li></ul>
	 *
	 * @return a new ImmutableVolumeProfile with the default values
	 */
	public static ImmutableVolumeProfile newInstance() {
		return new ImmutableVolumeProfile(1f, 0f, 0, 0);
	}

	/**
	 * Returns a new ImmutableVolumeProfile with the same volumes and durations as this
	 * ImmutableVolumeProfile, except with the normal volume changed to the provided value.
	 *
	 * @param normalVolume
	 * 		the volume to use during normal playback, as a decimal between 0 (silent) and 1 (maximum)
	 * @return a new ImmutableVolumeProfile with the normal volume changed
	 * @throws IllegalArgumentException
	 * 		if {@code normalVolume} is less than 0 or greater than 1
	 */
	public ImmutableVolumeProfile withNormalVolume(final float normalVolume) {
		if (normalVolume < 0 || normalVolume > 1) {
			throw new IllegalArgumentException("normalVolume must be between 0 and 1 inclusive");
		}

		return new ImmutableVolumeProfile(normalVolume, duckingVolume, duckingTransitionDuration,
				audioFocusRegainedTransitionDuration);
	}

	/**
	 * Returns a new ImmutableVolumeProfile with the same volumes and durations as this
	 * ImmutableVolumeProfile, except with the ducking volume changed to the provided value.
	 *
	 * @param duckingVolume
	 * 		the volume to use during audio focus ducking, as a decimal between 0 (silent) and 1
	 * 		(maximum)
	 * @return a new ImmutableVolumeProfile with the ducking volume changed
	 * @throws IllegalArgumentException
	 * 		if {@code duckingVolume} is less than 0 or greater than 1
	 */
	public ImmutableVolumeProfile withDuckingVolume(final float duckingVolume) {
		if (duckingVolume < 0 || duckingVolume > 1) {
			throw new IllegalArgumentException("duckingVolume must be between 0 and 1 inclusive");
		}

		return new ImmutableVolumeProfile(normalVolume, duckingVolume, duckingTransitionDuration,
				audioFocusRegainedTransitionDuration);
	}

	/**
	 * Returns a new ImmutableVolumeProfile with the same volumes and durations as this
	 * ImmutableVolumeProfile, except with the ducking transition duration changed to the provided
	 * value.
	 *
	 * @param duckingTransitionDuration
	 * 		the duration for transitioning to {@code duckingVolume} when an audio focus ducking request
	 * 		is received from the system, not less than 0
	 * @return a new ImmutableVolumeProfile with the ducking transition duration changed
	 * @throws IllegalArgumentException
	 * 		if {@code duckingTransitionDuration} is less than 0
	 */
	public ImmutableVolumeProfile withDuckingTransitionDuration(final int
			duckingTransitionDuration) {
		if (duckingTransitionDuration < 0) {
			throw new IllegalArgumentException("duckingTransitionDuration must be greater than or" +
					" equal to 0");
		}

		return new ImmutableVolumeProfile(normalVolume, duckingVolume, duckingTransitionDuration,
				audioFocusRegainedTransitionDuration);
	}

	/**
	 * Returns a new ImmutableVolumeProfile with the same volumes and durations as this
	 * ImmutableVolumeProfile, except with the audio focus regained transition duration changed to
	 * the provided value.
	 *
	 * @param audioFocusRegainedTransitionDuration
	 * 		duration for transitioning to {@code normalVolume} after audio ducking, not less than 0
	 * @return a new ImmutableVolumeProfile with the audio focus regained transition duration
	 * changed
	 * @throws IllegalArgumentException
	 * 		if {@code audioFocusRegainedTransitionDuration} is less than 0
	 */
	public ImmutableVolumeProfile withAudioFocusRegainedTransitionDuration(
			final int audioFocusRegainedTransitionDuration) {
		if (audioFocusRegainedTransitionDuration < 0) {
			throw new IllegalArgumentException(
					"audioFocusRegainedTransitionDuration must be greater than or equal to 0");
		}

		return new ImmutableVolumeProfile(normalVolume, duckingVolume, duckingTransitionDuration,
				audioFocusRegainedTransitionDuration);
	}

	@Override
	public float getNormalVolume() {
		return normalVolume;
	}

	@Override
	public float getDuckingVolume() {
		return duckingVolume;
	}

	@Override
	public int getDuckingTransitionDuration() {
		return duckingTransitionDuration;
	}

	@Override
	public int getAudioFocusRegainedTransitionDuration() {
		return audioFocusRegainedTransitionDuration;
	}
}
