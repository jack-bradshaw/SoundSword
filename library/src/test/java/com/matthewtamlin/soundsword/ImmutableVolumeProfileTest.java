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


import com.matthewtamlin.soundsword.BuildConfig;
import com.matthewtamlin.soundsword.ImmutableVolumeProfile;
import com.matthewtamlin.soundsword.VolumeProfile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link ImmutableVolumeProfile}.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 18, constants = BuildConfig.class)
public class ImmutableVolumeProfileTest {
	/**
	 * Default value for the volume during audio focus ducking.
	 */
	private static final float DEFAULT_DUCKING_VOLUME = 0f;

	/**
	 * Default value for the volume during normal playback.
	 */
	private static final float DEFAULT_NORMAL_VOLUME = 1f;

	/**
	 * Default value for the transitioning to ducking duration.
	 */
	private static final int DEFAULT_DUCKING_TRANSITION_DURATION = 0;

	/**
	 * Default value for the transitioning to normal playback duration.
	 */
	private static final int DEFAULT_AUDIO_FOCUS_REGAINED_TRANSITION_DURATION = 0;

	/**
	 * Value for the volume during audio focus ducking.
	 */
	private static final float DUCKING_VOLUME = 0.1f;

	/**
	 * Value for the volume during normal playback.
	 */
	private static final float NORMAL_VOLUME = 0.5f;

	/**
	 * Value for the transitioning to ducking duration.
	 */
	private static final int DUCKING_TRANSITION_DURATION = 50;

	/**
	 * Value for the transitioning to normal playback duration.
	 */
	private static final int AUDIO_FOCUS_REGAINED_TRANSITION_DURATION = 500;

	@Test
	public void newInstance_shouldHaveDefaultValues() {
		final VolumeProfile vp = ImmutableVolumeProfile.newInstance();

		assertThat("default ducking volume not set/return correctly", vp.getDuckingVolume() ==
				DEFAULT_DUCKING_VOLUME);
		assertThat("default normal volume not set/return correctly", vp.getNormalVolume() ==
				DEFAULT_NORMAL_VOLUME);
		assertThat("default ducking transition duration not set/return correctly",
				vp.getDuckingTransitionDuration() == DEFAULT_DUCKING_TRANSITION_DURATION);
		assertThat("default audio focus regained transition duration not set/return correctly",
				vp.getAudioFocusRegainedTransitionDuration() ==
						DEFAULT_AUDIO_FOCUS_REGAINED_TRANSITION_DURATION);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withNormalVolume_illegalArgument1_shouldThrowException() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp.withNormalVolume(-1f); // Should throw exception
	}

	@Test(expected = IllegalArgumentException.class)
	public void withNormalVolume_illegalArgument2_shouldThrowException() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp.withNormalVolume(1.1f); // Should throw exception
	}

	@Test
	public void withNormalVolume_boundaryCaseArgument1_shouldSetNormalVolume() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withNormalVolume(1f);

		assertThat("normal volume did not set/return properly",
				vp.getNormalVolume() == 1f);
	}

	@Test
	public void withNormalVolume_boundaryCaseArgument2_shouldSetNormalVolume() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withNormalVolume(0f);

		assertThat("normal volume did not set/return properly",
				vp.getNormalVolume() == 0f);
	}

	@Test
	public void withNormalVolume_legalArgument_shouldSetNormalVolume() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withNormalVolume(NORMAL_VOLUME);

		assertThat("normal volume did not set/return properly",
				vp.getNormalVolume() == NORMAL_VOLUME);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withDuckingVolume_illegalArgument1_shouldThrowException() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp.withDuckingVolume(-1f); // Should throw exception
	}

	@Test(expected = IllegalArgumentException.class)
	public void withDuckingVolume_illegalArgument2_shouldThrowException() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp.withDuckingVolume(1.1f); // Should throw exception
	}

	@Test
	public void withDuckingVolume_boundaryCaseArgument1_shouldSetDuckingVolume() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withDuckingVolume(1f);

		assertThat("ducking volume did not set/return properly",
				vp.getDuckingVolume() == 1f);
	}

	@Test
	public void withDuckingVolume_boundaryCaseArgument2_shouldSetDuckingVolume() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withDuckingVolume(0f);

		assertThat("ducking volume did not set/return properly",
				vp.getDuckingVolume() == 0f);
	}

	@Test
	public void withDuckingVolume_legalArgument_shouldSetDuckingVolume() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withDuckingVolume(DUCKING_VOLUME);

		assertThat("ducking volume did not set/return properly",
				vp.getDuckingVolume() == DUCKING_VOLUME);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withDuckingTransitionDuration_illegalArgument_shouldThrowException() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp.withDuckingTransitionDuration(-5); // Should throw exception
	}

	@Test
	public void
	withDuckingTransitionDuration_boundaryCaseArgument_shouldSetDuckingTransitionDuration() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withDuckingTransitionDuration(0); // Should not throw exception

		assertThat("ducking transition duration did not set/return properly",
				vp.getDuckingTransitionDuration() == 0);
	}

	@Test
	public void withDuckingTransitionDuration_legalArgument_shouldSetDuckingTransitionDuration() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withDuckingTransitionDuration(DUCKING_TRANSITION_DURATION);

		assertThat("ducking transition duration did not set/return properly",
				vp.getDuckingTransitionDuration() == DUCKING_TRANSITION_DURATION);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withAudioFocusRegainedTransitionDuration_illegalArgument_shouldThrowException() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp.withAudioFocusRegainedTransitionDuration(-5); // Should throw exception
	}

	@Test
	public void
	withAudioFocusRegainedTransitionDuration_boundaryCaseArgument_shouldSetAudioFocusRegainedTransitionDuration
			() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withAudioFocusRegainedTransitionDuration(0); // Should not throw exception

		assertThat("audio focus regained transition duration did not set/return properly",
				vp.getAudioFocusRegainedTransitionDuration() == 0);
	}

	@Test
	public void
	withAudioFocusRegainedTransitionDuration_legalArgument_shouldSetAudioFocusRegainedTransitionDuration() {
		ImmutableVolumeProfile vp = ImmutableVolumeProfile.newInstance();

		vp = vp.withAudioFocusRegainedTransitionDuration(AUDIO_FOCUS_REGAINED_TRANSITION_DURATION);

		assertThat("audio focus regained transition duration did not set/return properly",
				vp.getAudioFocusRegainedTransitionDuration() ==
						AUDIO_FOCUS_REGAINED_TRANSITION_DURATION);
	}
}
