package network;

/**
 * The user of a cluster should implement this interface to receive events about
 * any exception that occures during receving objects on the network.
 */
public interface FailureListener {

	/**
	 * Called by the cluster if there is an exception during receiving objects.
	 * 
	 * @param throwable
	 *            The exception that occured.
	 */
	void fail(Throwable throwable);
}
