package core.server;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class InputPoller implements Runnable {

	private boolean running;
	private boolean exceptionOccured;
	private Queue<String> availableInput;
	private SocketTransport inputter;

	public InputPoller(SocketTransport inputter) {
		availableInput = new LinkedList<String>();
		running = false;
		exceptionOccured = false;
		this.inputter = inputter;
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				String input = inputter.read();
				synchronized (availableInput) {
					availableInput.add(input);
				}
				Thread.sleep(10);
			} catch (Exception e) {
				if (!(e instanceof NoSuchElementException)) {
					exceptionOccured = true;
					running = false;
				}
			}
		}
	}

	public void stop() {
		running = false;
	}

	public boolean inputAvailable() {
		return availableInput.size() != 0;
	}

	public boolean errorOccured() {
		return exceptionOccured;
	}

	public String getInput() {
		String toRet = null;
		try {
			synchronized (availableInput) {
				toRet = availableInput.remove();
			}
		} catch (Exception e) {
			exceptionOccured = true;
			e.printStackTrace();
		}
		return toRet;
	}

}
