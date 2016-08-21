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

import android.net.Uri;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;

/**
 * A PlayableMedia object stored on disk.
 */
public final class LocalPlayableMedia implements PlayableMedia {
	/**
	 * The file on disk containing this media.
	 */
	private final File file;

	/**
	 * Constructs a new LocalPlayableMedia instance. The provided File is not accessed during
	 * instantiation.
	 *
	 * @param file
	 * 		the File on disk containing this media
	 * @throws IllegalArgumentException
	 * 		if {@code file} is null
	 */
	private LocalPlayableMedia(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("file cannot be null");
		}

		this.file = file;
	}

	/**
	 * Creates a new LocalPlayableMedia from a file path. The File at {@code path} is not accessed
	 * during this method's execution.
	 *
	 * @param path
	 * 		the path to the File on disk containing this media, not null
	 * @return the new LocalPlayableMedia
	 * @throws IllegalArgumentException
	 * 		if {@code path} is null
	 */
	public static LocalPlayableMedia fromFilePath(String path) {
		if (path == null) {
			throw new IllegalArgumentException("path cannot be null");
		}

		return new LocalPlayableMedia(new File(path));
	}

	/**
	 * Creates a new LocalPlayableMedia from a File. The provided File is not accessed during this
	 * method's execution.
	 *
	 * @param file
	 * 		the File on disk containing this media, not null
	 * @return the new LocalPlayableMedia
	 * @throws IllegalArgumentException
	 * 		if {@code file} is null
	 */
	public static LocalPlayableMedia fromFile(final File file) {
		if (file == null) {
			throw new IllegalArgumentException("file cannot be null");
		}

		return new LocalPlayableMedia(file);
	}

	@Override
	public final Uri getUri() {
		return Uri.fromFile(file);
	}

	@Override
	public final boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o == null) {
			return false;
		} else if (!(o instanceof LocalPlayableMedia)) {
			return false;
		} else {
			final LocalPlayableMedia s = (LocalPlayableMedia) o;
			return s.getUri().equals(this.getUri());
		}
	}

	@Override
	public final int hashCode() {
		HashCodeBuilder b = new HashCodeBuilder();
		b.append(file);
		return b.toHashCode();
	}
}