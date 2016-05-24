package network.test;

import java.io.IOException;
import java.io.UncheckedIOException;

import network.Cluster;
import network.ConnectedNode;

/**
 * Represents the usage of the network layer. The main function takes one parameter, a node id that
 * is logged to the console.
 */
public class ClusterTest {

  public static void main(final String[] args) {
    final String nodeId = args[0];

    // Instantiate cluster. Pass the ObjectReceiver implementation that will
    // receive any object that was sent from the other node and a failure
    // listener that will be called if there is an issue in the network
    // channel.
    Cluster cluster = new Cluster();

    // Call connect to cluster. The connect function will return if the
    // other node is connected
    try (ConnectedNode connectedNode = cluster.connect(new TestObjectReceiver(nodeId),
        new TestFailureListener())) {

      // Send any object to the other node. The object will be received
      // with the ObjectReceiver implementation.
      connectedNode.send("Szeva. En " + nodeId + " vagyok");

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      connectedNode.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
