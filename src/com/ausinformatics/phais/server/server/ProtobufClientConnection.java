package com.ausinformatics.phais.server.server;

import java.io.IOException;
import java.net.Socket;

import com.ausinformatics.client.GameClient.ProtocolRequest;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;

/**
 * An implementation of a proto2-backed ClientConnection.
 */
public class ProtobufClientConnection implements ClientConnection {
	private Socket socket;
	private CodedInputStream inStream;
	private CodedOutputStream outStream;

	/**
	 * @param socket
	 *            Underlying transport socket
	 * @param timeout
	 *            Socket read timeout in milliseconds
	 */
	public ProtobufClientConnection(Socket socket, int timeout) {
		try {
			socket.setSoTimeout(timeout);
			inStream = CodedInputStream.newInstance(socket.getInputStream());
			outStream = CodedOutputStream.newInstance(socket.getOutputStream());
			this.socket = socket;
		} catch (IOException e) {
			disconnect();
		}
	}

	@Override
	public void sendMessage(Message message) {
		if (!isConnected())
			return;

		System.out.println("< " + TextFormat.shortDebugString(message));
		byte[] serialized = message.toByteArray();
		try {
			outStream.writeFixed32NoTag(serialized.length);
			outStream.writeRawBytes(serialized);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
		}
	}

	@Override
	public void recvMessage(Message.Builder builder) throws DisconnectedException {
		if (!isConnected())
			throw new DisconnectedException(this);

		try {
			byte[] serialized = inStream.readRawBytes(inStream.readFixed32());
			builder.mergeFrom(serialized);
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
			throw new DisconnectedException(this);
		}

		System.out.println("> " + TextFormat.shortDebugString(builder));
	}

	@Override
	public boolean isConnected() {
		return socket != null && inStream != null && outStream != null;
	}

	@Override
	public void disconnect() {
		if (isConnected()) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			socket = null;
			inStream = null;
			outStream = null;
		}
	}

	@Override
	public void sendInfo(String s) {
		throw new UnsupportedOperationException("POD data transport not implemented in "
				+ this.getClass().getCanonicalName());
	}

	@Override
	public void sendInfo(int i) {
		throw new UnsupportedOperationException("POD data transport not implemented in "
				+ this.getClass().getCanonicalName());
	}

	@Override
	public String getStrInput() throws DisconnectedException {
		throw new UnsupportedOperationException("POD data transport not implemented in "
				+ this.getClass().getCanonicalName());
	}

	@Override
	public int getIntInput() throws DisconnectedException {
		throw new UnsupportedOperationException("POD data transport not implemented in "
				+ this.getClass().getCanonicalName());
	}

	@Override
	public String getAsync() {
		throw new UnsupportedOperationException("POD data transport not implemented in "
				+ this.getClass().getCanonicalName());
	}

	@Override
	public void sendFatal(String s) {
		sendMessage(ProtocolRequest.newBuilder().setCommand(ProtocolRequest.Command.ERROR).setErrorDetail(s).build());
		disconnect();
	}
}