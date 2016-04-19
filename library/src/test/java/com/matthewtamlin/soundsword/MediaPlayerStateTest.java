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


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link MediaPlayerState}. These tests primarily check that the documentation for
 * {@link android.media.MediaPlayer} has been accurately represented in code.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 18, constants = BuildConfig.class)
public class MediaPlayerStateTest {
	@Test
	public void testConstant_IDLE_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.IDLE;

		assertThat("constant IDLE has not been defined properly", !s.canPrepare());
		assertThat("constant IDLE has not been defined properly", !s.canStart());
		assertThat("constant IDLE has not been defined properly", !s.canPause());
		assertThat("constant IDLE has not been defined properly", !s.canStop());
		assertThat("constant IDLE has not been defined properly", !s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", !s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", s.canGetVideoHeightAndWidth());
	}

	@Test
	public void testConstant_INITIALISED_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.INITIALISED;

		assertThat("constant IDLE has not been defined properly", s.canPrepare());
		assertThat("constant IDLE has not been defined properly", !s.canStart());
		assertThat("constant IDLE has not been defined properly", !s.canPause());
		assertThat("constant IDLE has not been defined properly", !s.canStop());
		assertThat("constant IDLE has not been defined properly", !s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", !s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", s.canGetVideoHeightAndWidth());
	}

	@Test
	public void testConstant_PREPARED_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.PREPARED;

		assertThat("constant IDLE has not been defined properly", !s.canPrepare());
		assertThat("constant IDLE has not been defined properly", s.canStart());
		assertThat("constant IDLE has not been defined properly", !s.canPause());
		assertThat("constant IDLE has not been defined properly", s.canStop());
		assertThat("constant IDLE has not been defined properly", s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", !s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", !s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", s.canGetVideoHeightAndWidth());
	}

	@Test
	public void testConstant_STARTED_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.STARTED;

		assertThat("constant IDLE has not been defined properly", !s.canPrepare());
		assertThat("constant IDLE has not been defined properly", s.canStart());
		assertThat("constant IDLE has not been defined properly", s.canPause());
		assertThat("constant IDLE has not been defined properly", s.canStop());
		assertThat("constant IDLE has not been defined properly", s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", !s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", !s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", s.canGetVideoHeightAndWidth());
	}

	@Test
	public void testConstant_PAUSED_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.PAUSED;

		assertThat("constant IDLE has not been defined properly", !s.canPrepare());
		assertThat("constant IDLE has not been defined properly", s.canStart());
		assertThat("constant IDLE has not been defined properly", s.canPause());
		assertThat("constant IDLE has not been defined properly", s.canStop());
		assertThat("constant IDLE has not been defined properly", s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", !s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", !s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", s.canGetVideoHeightAndWidth());
	}

	@Test
	public void testConstant_PLAYBACK_COMPLETE_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.PLAYBACK_COMPLETE;

		assertThat("constant IDLE has not been defined properly", !s.canPrepare());
		assertThat("constant IDLE has not been defined properly", s.canStart());
		assertThat("constant IDLE has not been defined properly", s.canPause());
		assertThat("constant IDLE has not been defined properly", s.canStop());
		assertThat("constant IDLE has not been defined properly", s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", !s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", !s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", s.canGetVideoHeightAndWidth());
	}

	@Test
	public void testConstant_STOPPED_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.STOPPED;

		assertThat("constant IDLE has not been defined properly", s.canPrepare());
		assertThat("constant IDLE has not been defined properly", !s.canStart());
		assertThat("constant IDLE has not been defined properly", !s.canPause());
		assertThat("constant IDLE has not been defined properly", s.canStop());
		assertThat("constant IDLE has not been defined properly", !s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", !s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", s.canGetVideoHeightAndWidth());
	}

	@Test
	public void testConstant_PREPARING_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.PREPARING;

		assertThat("constant IDLE has not been defined properly", !s.canPrepare());
		assertThat("constant IDLE has not been defined properly", !s.canStart());
		assertThat("constant IDLE has not been defined properly", !s.canPause());
		assertThat("constant IDLE has not been defined properly", !s.canStop());
		assertThat("constant IDLE has not been defined properly", !s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", !s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", !s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", !s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", !s.canGetVideoHeightAndWidth());
	}

	@Test
	public void testConstant_ERROR_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.ERROR;

		assertThat("constant IDLE has not been defined properly", !s.canPrepare());
		assertThat("constant IDLE has not been defined properly", !s.canStart());
		assertThat("constant IDLE has not been defined properly", !s.canPause());
		assertThat("constant IDLE has not been defined properly", !s.canStop());
		assertThat("constant IDLE has not been defined properly", !s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", !s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", !s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", !s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", !s.canGetVideoHeightAndWidth());
	}

	@Test
	public void testConstant_END_shouldPass() {
		final MediaPlayerState s = MediaPlayerState.END;

		assertThat("constant IDLE has not been defined properly", !s.canPrepare());
		assertThat("constant IDLE has not been defined properly", !s.canStart());
		assertThat("constant IDLE has not been defined properly", !s.canPause());
		assertThat("constant IDLE has not been defined properly", !s.canStop());
		assertThat("constant IDLE has not been defined properly", !s.canSeekTo());
		assertThat("constant IDLE has not been defined properly", !s.canSetDataSource());
		assertThat("constant IDLE has not been defined properly", s.canReset());
		assertThat("constant IDLE has not been defined properly", !s.canPrepareAsync());
		assertThat("constant IDLE has not been defined properly", s.canRelease());
		assertThat("constant IDLE has not been defined properly", !s.canGetCurrentPosition());
		assertThat("constant IDLE has not been defined properly", !s.canGetVideoHeightAndWidth());
	}
}
