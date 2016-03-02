package de.dfki.vsm.players.stickman.client;

import de.dfki.vsm.players.client.ClientConnectionPlayerHandler;
import de.dfki.vsm.players.stickman.StickmanStage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 *
 * @author Patrick Gebhard
 *
 */
public class ClientConnectionHandler extends ClientConnectionPlayerHandler {
	private static String sIDENTIFIER = "StickmanStage";
	public ClientConnectionHandler() {
		super();
	}

	public void connect() {
		StickmanStage.mLogger.info(sIDENTIFIER +  "tries to connect with control application...");
		try {
			super.connect();
		} catch (UnknownHostException e) {
			StickmanStage.mLogger.severe(mHost + " is unknown - aborting!");
		} catch (IOException e) {
			StickmanStage.mLogger.severe(mHost + " i/o exception - aborting!");
		}
	}

	@Override
	public void run() {
		String inputLine = "";

		while (mRunning) {
			try {
				inputLine = mIn.readLine();
				StickmanStage.parseStickmanMLCmd(inputLine);
			} catch (IOException ex) {
				StickmanStage.mLogger.severe(mHost + " i/o exception - aborting!");
			}
		}
	}
}
