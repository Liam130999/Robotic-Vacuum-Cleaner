package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * retrieving the result once it is available.
 * <p>
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 *
 * @param <T> The type of the result held by this Future object.
 */
public class Future<T> {

	private T result;
	private boolean done;

	/**
	 * Constructs an empty Future object.
	 * <p>
	 * Initializes the result to {@code null} and sets the status to not done.
	 *
	 * @pre None
	 * @post this.done == false && this.result == null
	 */
	public Future() {
		this.done = false;
		this.result = null;
	}

	/**
	 * Retrieves the result the Future object holds if it has been resolved.
	 * This is a blocking method that waits until the result is available.
	 *
	 * @return The result of type {@code T} if it is available; otherwise, waits until it is available.
	 * @throws InterruptedException If the thread is interrupted while waiting.
	 * @pre None
	 * @post result is returned if done == true.
	 */
	public synchronized T get() {
		while (!this.done) {
			try {
				wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Preserve the interrupt status
				return null;
			}
		}
		return result;
	}

	/**
	 * Resolves the result of this Future object.
	 * Once resolved, all waiting threads are notified.
	 *
	 * @param result The result to be set in this Future object.
	 * @pre this.done == false
	 * @post this.result == result && this.done == true
	 */
	public synchronized void resolve(T result) {
		if (!this.done) {
			this.result = result;
			this.done = true;
			notifyAll();
		}
	}

	/**
	 * Checks if this Future object has been resolved.
	 *
	 * @return {@code true} if this object has been resolved; {@code false} otherwise.
	 * @pre None
	 * @post Returns true if done == true, otherwise false.
	 */
	public synchronized boolean isDone() {
		return this.done;
	}

	/**
	 * Retrieves the result the Future object holds if it has been resolved.
	 * This method is non-blocking and waits for the specified timeout.
	 *
	 * @param timeout The maximum amount of time to wait for the result.
	 * @param unit    The {@link TimeUnit} defining the unit of time for the timeout.
	 * @return The result of type {@code T} if available within the specified time, otherwise {@code null}.
	 * @throws InterruptedException If the thread is interrupted while waiting.
	 * @pre timeout > 0 && unit != null
	 * @post If done == true within timeout, result is returned. Otherwise, null is returned.
	 */
	public synchronized T get(long timeout, TimeUnit unit) {
		if (this.done) {
			return result;
		}

		long millisTimeout = unit.toMillis(timeout);
		long endTime = System.currentTimeMillis() + millisTimeout;

		while (!done && millisTimeout > 0) {
			try {
				wait(millisTimeout); // Wait with a timeout
				millisTimeout = endTime - System.currentTimeMillis(); // Update remaining time
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Preserve interrupt status
				return null; // Graceful exit on interruption
			}
		}

		if (done) {
			return result;
		} else {
			return null;
		}
	}
}
