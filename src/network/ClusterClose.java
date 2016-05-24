package network;

import java.io.Serializable;

/**
 * This object is sent for the other node when the cluster is closed. That
 * means, that the {@link ObjectReceiver} instance will get this object always
 * when the other node closes the game.
 */
public final class ClusterClose implements Serializable {

	public static final ClusterClose INSTANCE = new ClusterClose();

	private static final long serialVersionUID = 1L;

	private ClusterClose() {
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof ClusterClose;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
