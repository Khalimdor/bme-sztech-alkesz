package network.internal;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import network.Cluster;
import network.FailureListener;

/**
 * Sends multicast ping messages that should be received by other node. After
 * the other node received the ping message, the two nodes will connect to each
 * other. Sending ping messages must happen in a new thread as the main thread
 * should wait for connection from the other node, who received the packet sent
 * by this sender thread.
 */
public class PingSender implements Runnable, Closeable {

	private AtomicBoolean closed = new AtomicBoolean(false);

	private InetAddress group;

	private byte[] serverPortBA;

	/**
	 * Constructor.
	 *
	 * @param group
	 *            The group address of the multicast messages.
	 * @param serverPortBA
	 *            The opened port of this server that the other node should
	 *            connect to after received the ping message sent by this
	 *            sender. The port is represented by a byte array as UDP packets
	 *            can be byte arrays.
	 * @param failureListener
	 *            Failure listener is called if there is an issue with the
	 *            network.
	 */
	public PingSender(final InetAddress group, final byte[] serverPortBA, final FailureListener failureListener) {
		this.group = group;
		this.serverPortBA = serverPortBA;
	}

	@Override
	public void close() {
		closed.set(true);
	}

	private void fail(final IOException e) {
		e.printStackTrace();
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		try (DatagramSocket socket = new DatagramSocket()) {
			while (!closed.get()) {
				DatagramPacket packet = new DatagramPacket(serverPortBA, serverPortBA.length, group,
						Cluster.MULTICAST_PORT);
				socket.send(packet);
				Thread.sleep(100);
			}
		} catch (IOException e) {
			fail(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
