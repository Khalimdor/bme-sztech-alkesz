package network.test;

import network.FailureListener;

/**
 * Simplest implementation of {@link FailureListener} that writes every received exception to the
 * standard error stream.
 */
public final class TestFailureListener implements FailureListener {
  @Override
  public void fail(final Throwable e) {
    e.printStackTrace();
  }
}
