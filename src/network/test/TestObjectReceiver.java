package network.test;

import network.ObjectReceiver;

/**
 * Simplest implementation of the {@link ObjectReceiver} that writes every
 * object to the standard output that were sent by the other node.
 */
public final class TestObjectReceiver implements ObjectReceiver {
	private final String nodeId;

	TestObjectReceiver(final String nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public void receive(final Object object) {
		System.out.println(nodeId + " node received Object: " + object);
	}
}
