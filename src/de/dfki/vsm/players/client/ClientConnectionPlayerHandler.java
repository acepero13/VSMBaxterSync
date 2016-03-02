package de.dfki.vsm.players.client;

import de.dfki.vsm.players.stickman.StickmanStage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

/**
 *
 * @author Patrick Gebhard
 *
 */
public class ClientConnectionPlayerHandler extends Thread {

	protected Socket mSocket;
	protected String mHost = "127.0.0.1";
	protected int mPort = 7777;
	protected PrintWriter mOut;
	protected BufferedReader mIn;
	protected static String sIDENTIFIER = "StickmanStage";

	protected boolean mRunning = true;
	public boolean mConnected = false;

	public ClientConnectionPlayerHandler() {
		super.setName(ClientConnectionPlayerHandler.sIDENTIFIER + " Socket Connection Handler");
	}

	public void end() {
		try {
			mSocket.shutdownInput();
			mSocket.shutdownOutput();
			mSocket.close();
			mRunning = false;
			mConnected = false;
			System.out.println("Ending connection");
		} catch (IOException ex) {
			StickmanStage.mLogger.severe("Error closing socket to " + mHost + ", " + mPort);
		}
	}

	public void sendToServer(String message) {
		if (mSocket.isConnected()) {
			mOut.println(message);
			mOut.flush();
		}
	}

	public void connect(String host, int port) throws IOException {
		mHost = host;
		mPort = port;
		connect();
	}
	
	public void connect() throws IOException {
		try {
			InetAddress inteAddress = InetAddress.getByName(mHost);
			SocketAddress socketAddress = new InetSocketAddress(inteAddress, mPort);

			mSocket = new Socket();
			mSocket.connect(socketAddress, 2000); // wait max. 2000ms

			mOut = new PrintWriter(mSocket.getOutputStream(), true);
			mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
		} catch (UnknownHostException e) {
			throw new UnknownHostException();
		} catch (IOException e) {
			throw new IOException();

		}
		mConnected = true;
		sendToServer("CLIENTID#" + sIDENTIFIER);
		start();
	}

	@Override
	public  void run() {
		System.out.println("Method not implemented");
	}
}
