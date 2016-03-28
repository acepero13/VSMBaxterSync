package de.dfki.vsm.util.tts.events;

//~--- non-JDK imports --------------------------------------------------------

//import de.dfki.embots.output.scenePlayer.scenes.Scene;
import de.dfki.vsm.util.evt.EventObject;

/**
 * @author Sergio Soto
 */
public class LineStop extends EventObject {
    public LineStop(Object source) {
        super(source);
    }

    public String getEventDescription() {
        return "Line Stopped";
    }
}
