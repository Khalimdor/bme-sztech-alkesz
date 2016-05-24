package network.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import network.ClusterClose;
import network.FailureListener;
import network.ObjectReceiver;

/**
 * New thread that waits for the other node to connect. As soon as the connection is made, objects
 * can be sent to this node and the {@link PingSender} of this node is closed.
 */
public class ServerSocketThread extends Thread implements Closeable {

  private AtomicBoolean closed = new AtomicBoolean(false);

  private FailureListener failureListener;

  private final ObjectReceiver objectReceiver;

  private PingSender pingSender;

  private final ServerSocket serverSocket;

  public ServerSocketThread(final ServerSocket serverSocket, final ObjectReceiver objectReceiver,
      final FailureListener failureListener, final PingSender pingSender) {
    this.serverSocket = serverSocket;
    this.objectReceiver = objectReceiver;
    this.failureListener = failureListener;
    this.pingSender = pingSender;
  }

  @Override
  public void close() throws IOException {
    if (closed.getAndSet(true)) {
      serverSocket.close();
    }
  }

  @Override
  public void run() {
    try (Socket socket = serverSocket.accept()) {
      pingSender.close();
      InputStream inputStream = socket.getInputStream();
      ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
      boolean endObjectReceived = false;
      while (!closed.get() && !endObjectReceived) {
        Object object = objectInputStream.readObject();
        objectReceiver.receive(object);
        if (object instanceof ClusterClose) {
          endObjectReceived = true;
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      pingSender.close();
      failureListener.fail(e);
    }
  }

}
