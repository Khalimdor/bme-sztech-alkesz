package network;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import network.internal.ServerSocketThread;

/**
 * The representation of the other node that is connected within the cluster.
 */
public class ConnectedNode implements Closeable {

  private Socket clientSocket = null;

  private AtomicBoolean closed = new AtomicBoolean(false);

  private ObjectOutputStream objectOutputStream;

  private ServerSocket serverSocket;

  private ServerSocketThread serverThread;

  ConnectedNode(final ServerSocketThread serverThread, final Socket clientSocket,
      final ObjectOutputStream objectOutputStream) {
    this.serverThread = serverThread;
    this.clientSocket = clientSocket;
    this.objectOutputStream = objectOutputStream;
  }

  /**
   * Closing all of the resources this {@link Cluster} instance has opened.
   */
  @Override
  public synchronized void close() throws IOException {
    if (!closed.getAndSet(true)) {
      if (serverThread != null) {
        serverThread.close();
      }

      if (serverSocket != null) {
        serverSocket.close();
      }
      if (objectOutputStream != null) {
        trySendingCloseToOtherNode();
        objectOutputStream.close();
      }

      if (clientSocket != null) {
        clientSocket.close();
      }
    }
  }

  /**
   * Sends the object to the other node in the cluster.
   *
   * @param object
   *          The object to send to.
   * @throws IOException
   *           if there is an issue with the connection during sending the object.
   * @throws IllegalStateException
   *           if the cluster is not built, the other node is not connected.
   */
  public synchronized void send(final Object object) throws IOException {
    if (objectOutputStream == null) {
      throw new IllegalStateException("There is no connected node");
    }
    objectOutputStream.writeObject(object);
    objectOutputStream.flush();
  }

  private void trySendingCloseToOtherNode() {
    try {
      send(ClusterClose.INSTANCE);
    } catch (IOException e) {
      // We do not care if there is an exception on the channel at this
      // point
    }
  }
}
