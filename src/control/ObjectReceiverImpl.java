package control;

import network.ObjectReceiver;

public class ObjectReceiverImpl implements ObjectReceiver {

	private Control control;

	public ObjectReceiverImpl(final Control control) {
		this.control = control;
	}

	@Override
	public void receive(final Object object) {
		// Fogadja a GameState objektumokat, az eger kattintas infokat, ...,
		// hivogatja a kontrol
		// fuggvenyeit

	}

}
