package de.dfki.vsm.players.baxter.action;

import de.dfki.vsm.players.action.Action;
import de.dfki.vsm.players.action.ActionListener;
import de.dfki.vsm.players.baxter.client.BaxterClientConnectionHandler;
import de.dfki.vsm.players.baxter.command.BaxterCommand;
import de.dfki.vsm.players.server.TCPActionServer;
import de.dfki.vsm.players.stickman.Stickman;
import de.dfki.vsm.players.stickman.animationlogic.AnimationLoader;
import de.dfki.vsm.players.stickman.animationlogic.listener.AnimationListener;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLUtilities;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static de.dfki.vsm.players.ActionPlayer.notifyListenersAboutAction;

/**
 * Created by alvaro on 2/14/16.
 */
public class BaxterAction extends Action implements AnimationListener {
    int mDuration;
    Object mParameter;
    boolean mBlocking;
    BufferedImage head;
    String mAnimationAction;
    BaxterClientConnectionHandler connection ;

    public BaxterAction(int starttime, String name, int dur, Object param, boolean block){
        mParameter = param;
        mBlocking = block;
        mStartTime = starttime;
        mName = name;
        mDuration = dur;
        Stickman mBaxterStickman = new Stickman("Baxter", Stickman.TYPE.MALE, 2.0f);
        mBaxterStickman.setApplyTransform(false);
        AnimationLoader.getInstance().loadAnimation(mBaxterStickman, mName, dur, mBlocking);
        mBaxterStickman.doAnimation(mName, 500, false);

        BufferedImage image = new BufferedImage(
                640,
                480,
                BufferedImage.TYPE_INT_RGB
        );
        mBaxterStickman.paint( image.getGraphics() );
        head = cropHead(image, (int)mBaxterStickman.mHead.getWidth(), (int)mBaxterStickman.mHead.getHeight(), 2.0f);
        mBaxterStickman.setApplyTransform(true);


    }
    @Override
    public void update(String animationId) {
        System.out.println("UPDATED");
        //connection.end();
        mActionEndSync.release();

    }



    private BufferedImage cropHead(BufferedImage src, int width, int height, float scale ){
        BufferedImage dest = src.getSubimage(0, 0, width * (int) scale +70, height * (int) scale -50);
        return dest;
    }

    @Override
    public void run() {
        notifyListenersAboutAction(this, ActionListener.STATE.ACTION_STARTED);

         Dimension screenDimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

        // call the Component's paint method, using
        // the Graphics object of the image.


        ArrayList<String> params = new ArrayList<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ImageIO.write(head, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            //TODO: Delete BASEEnconde
            BASE64Encoder encoder = new BASE64Encoder();
            String imageString = encoder.encode(imageBytes);
            baos.close();
            params.add(imageString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BaxterCommand command = new BaxterCommand("paint", "testId", params);

        TCPActionServer.getInstance().addListener(this);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOSIndentWriter iosw = new IOSIndentWriter(out);
        boolean r = XMLUtilities.writeToXMLWriter(command, iosw);
        String toSend = new String(out.toByteArray());
        toSend+= "#END\n";
        BaxterClientConnectionHandler.getInstance().sendToServer(toSend);
        try {
            mActionEndSync.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("SENT!");
        TCPActionServer.getInstance().removeListener(this);
        mActionPlayer.actionEnded(this);
        mActionEndSync.release();
    }
}
