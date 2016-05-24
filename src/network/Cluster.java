package network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import network.internal.PingSender;
import network.internal.ServerSocketThread;

/**
 * Cluster is the main class of the network lazer. Cluster must be instantiated
 * to let two nodes talk to each other.
 */
public class Cluster {

	private static final InetAddress GROUP;

	/**
	 * The multicast IP address where the two nodes will start pinging each
	 * other.
	 */
	public static final String MULTICAST_IP = "230.0.113.0";

	/**
	 * The multicast port where the two nodes will start pinging each other.
	 */
	public static final int MULTICAST_PORT = 4446;

	static {
		try {
			GROUP = InetAddress.getByName(MULTICAST_IP);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Connects to the cluster. This function returns only if
	 * <ul>
	 * <li>This node is connected to other node in a cluster and they can start
	 * talking.</li>
	 * <li>The thread that called this function is interrupted.
	 * <li>
	 * </ul>
	 * If programmers want to interrupt this function for a reason (e.g.:
	 * pushing a cancel button), this function should be called on a new thread
	 * and the thread should be interrupted in case of pushing the cancel
	 * button.
	 *
	 * @param objectReceiver
	 *            The programmer, who needs two nodes in a cluster, should
	 *            implement ObjectReceiver. The objects sent by the other nodes
	 *            will be passed to {@link ObjectReceiver#receive(Object)}.
	 * @param failureListener
	 *            In case a network failure occures during waiting for more
	 *            objects, the {@link Cluster} instance will call
	 *            {@link FailureListener#fail(Throwable)} function. The
	 *            programmer, who needs the cluster should implement this
	 *            interface and handle unexpected exceptions.
	 * @return The {@link Cluster} instance. After this method is returned, the
	 *         programmer should {@link #send(Object)} to send objects to the
	 *         other node in the cluster.
	 * @throws IOException
	 *             if there is any problem during connecting to the other node.
	 */
	public synchronized ConnectedNode connect(final ObjectReceiver objectReceiver,
			final FailureListener failureListener) throws IOException {
		ServerSocket serverSocket = new ServerSocket(0);
		int serverPort = serverSocket.getLocalPort();

		byte[] serverPortBA = convertIntToByteArray(serverPort);
		PingSender pingSender = new PingSender(GROUP, serverPortBA, failureListener);

		ServerSocketThread serverSocketThread = new ServerSocketThread(serverSocket, objectReceiver, failureListener,
				pingSender);
		serverSocketThread.start();

		new Thread(pingSender).start();

		InetSocketAddress remoteSocketAddress = waitForPing(serverPortBA);

		Socket clientSocket = new Socket(remoteSocketAddress.getAddress(), remoteSocketAddress.getPort());

		ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
		return new ConnectedNode(serverSocketThread, clientSocket, objectOutputStream);
	}

	private int convertByteArrayToInt(final byte[] bytes) {
		return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

	private byte[] convertIntToByteArray(final int data) {
		byte[] result = new byte[Integer.BYTES];

		result[0] = (byte) ((data & 0xFF000000) >> 24);
		result[1] = (byte) ((data & 0x00FF0000) >> 16);
		result[2] = (byte) ((data & 0x0000FF00) >> 8);
		result[3] = (byte) ((data & 0x000000FF) >> 0);

		return result;
	}

	private InetSocketAddress waitForPing(final byte[] serverPortBA) {
		try (MulticastSocket socket = new MulticastSocket(Cluster.MULTICAST_PORT)) {
			System.out.println(GROUP.getHostAddress());
			socket.joinGroup(GROUP);

			InetSocketAddress remoteSocketAddress = null;
			while (remoteSocketAddress == null) {
				byte[] buf = new byte[Integer.BYTES];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				if (!Arrays.equals(buf, serverPortBA)) {
					int remotePort = convertByteArrayToInt(buf);
					remoteSocketAddress = new InetSocketAddress(packet.getAddress(), remotePort);
				}
			}
			socket.leaveGroup(GROUP);
			return remoteSocketAddress;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
