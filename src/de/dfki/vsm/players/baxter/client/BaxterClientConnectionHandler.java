package de.dfki.vsm.players.baxter.client;

import de.dfki.vsm.players.client.ClientConnectionPlayerHandler;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * Created by alvaro on 2/21/16.
 */
public class BaxterClientConnectionHandler extends ClientConnectionPlayerHandler {
    public static final Logger mLogger = Logger.getAnonymousLogger();
    private static BaxterClientConnectionHandler sInstance = null;

    public BaxterClientConnectionHandler() {
        super();
        mHost = "127.0.0.1";
        mPort = 1313;
        sIDENTIFIER = "Baxter";

    }

    public static BaxterClientConnectionHandler getInstance(){
        if(sInstance == null){
            sInstance =  new BaxterClientConnectionHandler();
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
