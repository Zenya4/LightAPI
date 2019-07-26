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

import java.lang.reflect.Method;

/**
 * Simple solution to stop the main thread being blocked
 *
 * @author Connor Spencer Harries
 */
public class UpdaterRunnable implements Runnable {

	/**
	 * Store the parent {@link Updater} instance.
	 */
	private final Updater updater;

	/**
	 * Create a new {@link ru.beykerykt.lightapi.updater.UpdaterRunnable} with an {@link Updater} as the parent.
	 *
	 * @param parent instace of {@link Updater}
	 */
	UpdaterRunnable(Updater parent) {
		this.updater = parent;
	}

	/**
	 * Use reflection to invoke the run method on our {@link Updater}
	 */
	@Override
	public void run() {
		try {
			Method method = updater.getClass().getDeclaredMethod("run");
			method.setAccessible(true);

			method.invoke(updater);

			method.setAccessible(false);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
