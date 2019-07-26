/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vladimir Mikhailov <beykerykt@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.beykerykt.lightapi.updater;

/**
 * Enumeration of possible responses from the updater.
 *
 * @author Connor Spencer Harries
 */
public enum Response {

	/**
	 * GitHub could not find the repository.
	 */
	REPO_NOT_FOUND,

	/**
	 * The latest release on GitHub isn't semver compliant.
	 */
	REPO_NOT_SEMVER,

	/**
	 * No releases have been made on the repository.
	 */
	REPO_NO_RELEASES,

	/**
	 * An update has been found.
	 */
	SUCCESS,

	/**
	 * An error occured whilst trying to find updates.
	 */
	FAILED,

	/**
	 * GitHub denied the connection. This is most likely due to too many connections being opened to the API
	 * within a small period of time.
	 */
	GITHUB_DENY,

	/**
	 * Used to indicate a server error such as HTTP status code 500.
	 */
	GITHUB_ERROR,

	/**
	 * The specified version is already the latest version
	 */
	NO_UPDATE;

	@Override
	public String toString() {
		return this.name();
	}
}
