package control;

import network.FailureListener;

public class FailureListenerImpl implements FailureListener {

	@Override
	public void fail(final Throwable throwable) {
		// Pl.: Gui-n feldobok egy ablakot, hogy gebasz van OK gombbal. Ha OK/ra
		// kattintott, akkor
		// kilepunk (vagy visszalepunk a menure)
	}

}
