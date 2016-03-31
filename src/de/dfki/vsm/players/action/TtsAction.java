package de.dfki.vsm.players.action;

import de.dfki.vsm.model.scenescript.AbstractWord;
import de.dfki.vsm.model.scenescript.SceneWord;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;
import de.dfki.vsm.util.tts.I4GMaryClient;
import de.dfki.vsm.util.tts.VoiceName;
import de.dfki.vsm.util.tts.events.LineStart;
import de.dfki.vsm.util.tts.events.LineStop;

import java.util.Iterator;
import java.util.LinkedList;

import static de.dfki.vsm.players.ActionPlayer.notifyListenersAboutAction;

/**
 * Created by alvaro on 3/26/16.
 */
public class TtsAction extends Action implements EventListener{
    private LinkedList<AbstractWord> listWords = new LinkedList<AbstractWord>();
    private final EventDispatcher mEventDispatcher = EventDispatcher.getInstance();
    private final I4GMaryClient mary = I4GMaryClient.instance();
    public TtsAction(){
        mEventDispatcher.register(this);

    }



    public void addWord(AbstractWord word){
        listWords.add(word);
    }

    public void run(){

        Iterator it = listWords.iterator();
        VoiceName speakerVoice = I4GMaryClient.OBADIAH;
        while (it.hasNext()) {
            AbstractWord iterAction = (AbstractWord) it.next();
            if (iterAction instanceof SceneWord) {
                mary.addWord(((SceneWord) iterAction).getText());
            }
        }
        String phrase = mary.getText();

        if(phrase.length() > 0) {
            try {
                mary.speak();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            mActionPlayer.actionEnded(this);
        }

        listWords.clear();

    }

    @Override
    public void update(EventObject event) {
        if(event instanceof LineStart){
            notifyListenersAboutAction(this, ActionListener.STATE.ACTION_STARTED);
        }
        if(event instanceof LineStop){
            mActionPlayer.actionEnded(this);
        }
    }
}
