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
 * A VolumeProfile defines the volumes to use for different audio focus modes, as well as the
 * durations to use when transitioning between modes. The audio focus modes are during audio focus
 * ducking and normal media playback
 */
public interface VolumeProfile {
	/**
	 * Returns the volume to use during normal media playback, as a decimal between 0 (silent) and 1
	 * (maximum).
	 *
	 * @return the normal volume
	 */
	float getNormalVolume();

	/**
	 * Returns the volume to use during audio focus ducking, as a decimal between 0 (silent) and 1
	 * (maximum).
	 *
	 * @return the ducking volume
	 */
	float getDuckingVolume();

	/**
	 * When audio focus ducking is requested by the system, the playback volume should be
	 * transitioned to {@link #getDuckingVolume()}. This method returns the duration of time to use
	 * for the transition.
	 *
	 * @return the transition duration, measured in milliseconds
	 */
	int getDuckingTransitionDuration();

	/**
	 * When audio focus has been regained after audio focus ducking, the playback volume should be
	 * transitioned to {@link #getNormalVolume()}. This method returns the duration of time to use
	 * for the transition.
	 *
	 * @return the transition duration, measured in milliseconds
	 */
	int getAudioFocusRegainedTransitionDuration();
}