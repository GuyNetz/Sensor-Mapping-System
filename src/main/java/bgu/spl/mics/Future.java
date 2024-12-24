package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	//Fields
	private T resolvedResult = null;
	private boolean isResolved = false;
	
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {

	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public synchronized T get() {
		while (!isResolved) {
			try {
				wait(); // waits until another thread notify/interrupt it
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Restore the interrupted status of the thread,
			}										// as it gets cleared when InterruptedException is thrown. 										
		}
		return resolvedResult;
	}
	
	/**
     * Resolves the result of this Future object.
     */
	public synchronized void resolve(T result) {
		if (!isResolved) {
			this.resolvedResult = result;
			this.isResolved = true;
			notifyAll(); 
		}
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public synchronized boolean isDone() {
		return isResolved;
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public synchronized T get(long timeout, TimeUnit unit) {
        if (!isResolved) {
            try {
                unit.timedWait(this, timeout); //waits the limited time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); //handles interruption like before
            }
        }
        return isResolved ? resolvedResult : null; // if the limited time has passed returns null
    }

}
