package de.dfki.vsm.players.baxter.client;

import de.dfki.vsm.api.VSMScenePlayer;
import de.dfki.vsm.api.VSMTCPSockClient;
import de.dfki.vsm.players.client.ClientConnectionPlayerHandler;
import de.dfki.vsm.players.stickman.StickmanStage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * Created by alvaro on 2/21/16.
 */
public class ClientConnectionHandler extends ClientConnectionPlayerHandler {
    public static final Logger mLogger = Logger.getAnonymousLogger();
    private static ClientConnectionHandler sInstance = null;

    public ClientConnectionHandler() {
        super();
        mHost = "192.168.178.86";
        mPort = 1313;
        sIDENTIFIER = "Baxter";

    }

    public static ClientConnectionHandler getInstance(){
        if(sInstance == null){
            sInstance =  new ClientConnectionHandler();
            sInstance.connect();
        }
        return sInstance;
    }

    public void connect() {
        mLogger.info(sIDENTIFIER +  "tries to connect with control application...");
        try {
            super.connect();
        } catch (UnknownHostException e) {
            mLogger.severe(mHost + " is unknown - aborting!");
        } catch (IOException e) {
            mLogger.severe(mHost + " i/o exception - aborting!");
        }
    }

    @Override
    public void run() {
        String inputLine = "";
        while (mRunning) {
            try {
                inputLine = mIn.readLine();
                System.out.println(inputLine);

                //TODO: Do something
            } catch (IOException ex) {
                mLogger.severe(mHost + " i/o exception - aborting!");
            }
        }
    }
}
