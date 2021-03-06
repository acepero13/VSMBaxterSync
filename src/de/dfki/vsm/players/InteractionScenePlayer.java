package de.dfki.vsm.players;

import de.dfki.vsm.editor.event.SceneExecutedEvent;
import de.dfki.vsm.editor.event.TurnExecutedEvent;
import de.dfki.vsm.editor.event.UtteranceExecutedEvent;
import de.dfki.vsm.model.project.AgentConfig;
import de.dfki.vsm.model.project.PlayerConfig;
import de.dfki.vsm.players.action.TtsAction;
import de.dfki.vsm.players.action.sequence.Phoneme;
import de.dfki.vsm.players.baxter.action.BaxterAction;
import de.dfki.vsm.players.stickman.Stickman;
import de.dfki.vsm.players.stickman.StickmanStage;
import de.dfki.vsm.players.stickman.animation.face.Speak;
import de.dfki.vsm.runtime.project.RunTimeProject;
import de.dfki.vsm.model.scenescript.AbstractWord;
import de.dfki.vsm.model.scenescript.ActionFeature;
import de.dfki.vsm.model.scenescript.ActionObject;
import de.dfki.vsm.model.scenescript.SceneAbbrev;
import de.dfki.vsm.model.scenescript.SceneGroup;
import de.dfki.vsm.model.scenescript.SceneObject;
import de.dfki.vsm.model.scenescript.SceneParam;
import de.dfki.vsm.model.scenescript.SceneScript;
import de.dfki.vsm.model.scenescript.SceneTurn;
import de.dfki.vsm.model.scenescript.SceneUttr;
import de.dfki.vsm.model.scenescript.SceneWord;
import de.dfki.vsm.players.EventActionPlayer;
import de.dfki.vsm.players.action.Action;
import de.dfki.vsm.players.action.ActionListener;
import de.dfki.vsm.players.action.EventAction;
import de.dfki.vsm.players.action.sequence.TimeMark;
import de.dfki.vsm.players.action.sequence.Word;
import de.dfki.vsm.players.action.sequence.WordTimeMarkSequence;
import de.dfki.vsm.players.baxter.client.BaxterClientConnectionHandler;
import de.dfki.vsm.players.stickman.action.StickmanAction;
import de.dfki.vsm.players.stickman.action.StickmanEventAction;
import de.dfki.vsm.runtime.interpreter.Process;
import de.dfki.vsm.runtime.players.RunTimePlayer;
import de.dfki.vsm.runtime.values.AbstractValue;
import de.dfki.vsm.runtime.values.AbstractValue.Type;
import de.dfki.vsm.runtime.values.StringValue;
import de.dfki.vsm.runtime.values.StructValue;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.log.LOGDefaultLogger;
import de.dfki.vsm.util.tts.I4GMaryClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Patrick Gebhard (based on Default ScenePlayer)
 *
 */
public final class InteractionScenePlayer implements RunTimePlayer, ActionListener {

    // The singelton player instance
    public static InteractionScenePlayer sInstance = null;
    // The singelton logger instance
    private final LOGDefaultLogger mLogger = LOGDefaultLogger.getInstance();
    // The player's runtime project
    private RunTimeProject mProject;
    // The project specific config
    private PlayerConfig mPlayerConfig;
    // The project specific name
    private String mPlayerName;
    // OUTPUT MANAGER: The Actionplay
    // ScheduledActionPlayer mActionPlayer;
    private static EventActionPlayer mActionPlayer;
    // Synchronisation with the action player
    private Semaphore mAllActionSync;
    private Semaphore mAllEndSync;
    // RENDERER: The StickmanStage
    private static StickmanStage mStickmanStage;
    // Some memory about who belongs to what
    private static HashMap<String, String> mRelationAgentPlayer = new HashMap<>();

    // Construct the StickmanScene player
    private InteractionScenePlayer() {
    }

    // Get the InteractionScenePlayer instance
    public static synchronized InteractionScenePlayer getInstance() {
        if (sInstance == null) {
            sInstance = new InteractionScenePlayer();
        }
        return sInstance;
    }

    @Override
    public void update(Action a, ActionListener.STATE actionState) {
        if (actionState == ActionListener.STATE.ACTION_STARTED) {
            //mLogger.message(((StickmanAction) a).mName + " started");
        }

        if (actionState == ActionListener.STATE.ACTION_FINISHED) {
            //mLogger.message(((StickmanAction) a).mName + " finished");
        }
    }

    @Override
    public void update(ActionListener.STATE actionState) {
        if (actionState == ActionListener.STATE.ALL_ACTIONS_FINISHED) {
            mAllActionSync.release();
        }
    }

    // Launch the InteractionScenePlayer
    @Override
    public final boolean launch(final RunTimeProject project) {
        // Initialize the project
        mProject = project;
        // Initialize the name
        mPlayerName = project.getPlayerName(this);
        // Initialize the config
        mPlayerConfig = project.getPlayerConfig(mPlayerName);
        // Print some information
        mLogger.message("Launching the InteractionScenePlayer '" + this + "' with configuration:\n" + mPlayerConfig);

        // Start the Action Player
        mLogger.message("Starting Action Player ...");
        mActionPlayer = EventActionPlayer.getNetworkInstance(8000);
        // Tell the ActionPlayer that this ScenePlayer is interested in updates
        mActionPlayer.addListener(this);
        mActionPlayer.start();

        // Start the StickmanStage client application
        mLogger.message("Starting StickmanStage Client Application ...");
        mStickmanStage = StickmanStage.getNetworkInstance("127.0.0.1", 8000);

        // collect and prepare information which agent belongs to which player
        getCharacters(mProject.getSceneScript()).stream().forEach((c) -> {
            AgentConfig ac = mProject.getAgentConfig(c);
            if (ac != null) {
                mRelationAgentPlayer.put(c, ac.getClassName());
            }
        });

        // put all stickman characters on the stickman stage
        mRelationAgentPlayer.keySet().stream().forEach((c) -> {
            if (mRelationAgentPlayer.get(c).equalsIgnoreCase("stickmanstage")) {
                StickmanStage.addStickman(c);
            } else {
                mLogger.warning("No AgentConfig found for Agent " + c + "!");
            }
        });

        // configure blocking mechanism
        mAllActionSync = new Semaphore(0);
        // configure shutdown mechanism
        mAllEndSync = new Semaphore(0);
        return true;
    }

    // Unload the InteractionScenePlayer
    @Override
    public final boolean unload() {
        // Print some information
        mLogger.message("Unloading the InteractionScenePlayer '" + this + "' with configuration:\n" + mPlayerConfig);
        // clear the stage
        StickmanStage.clearStage();
        // remove action player
        mActionPlayer.removeListener(this);
        //BaxterClientConnectionHandler.getInstance().end();
        // stop the ActionPlayer
        mActionPlayer.end();
        // do not wait for the end of any actions
        mAllActionSync.release();
        // Return true at success
        return true;
    }

    private Set<String> getCharacters(SceneScript scenescript) {
        Set<String> speakersSet = new HashSet<>();

        for (SceneObject scene : scenescript.getSceneList()) {
            LinkedList<SceneTurn> sturns = scene.getTurnList();

            for (SceneTurn t : sturns) {
                if (!speakersSet.contains(t.getSpeaker())) {
                    speakersSet.add(t.getSpeaker());
                }

                LinkedList<SceneUttr> suttr = t.getUttrList();

                for (SceneUttr u : suttr) {
                    LinkedList<AbstractWord> words = u.getWordList();

                    for (AbstractWord word : words) {
                        if (word instanceof ActionObject) {
                            ActionObject ao = ((ActionObject) word);

                            String agent = ao.getAgentName();

                            if ((agent != null) && !agent.trim().isEmpty()) {
                                if (!speakersSet.contains(agent)) {
                                    speakersSet.add(agent);
                                }
                            }
                        }
                    }
                }
            }
        }
        return speakersSet;
    }

    // Play some scene with the player
    @Override
    public final void play(final String name,
                           final LinkedList<AbstractValue> args
    ) {
        final Process process = ((Process) Thread.currentThread());
        final HashMap<String, String> mSceneParamMap = new HashMap<>();
        // Process The Arguments
        if ((args != null) && !args.isEmpty()) {
            // Get The First Argument
            final AbstractValue value = args.getFirst();
            // Check The Argument Type
            if (value.getType().equals(Type.STRUCT)) {
                // Cast The Argument Type
                final StructValue struct = (StructValue) value;
                // Process Scene Arguments
                for (final Entry<String, AbstractValue> entry : struct.getValueMap().entrySet()) {
                    if (entry.getValue().getType() == Type.STRING) {
                        mSceneParamMap.put(entry.getKey(), ((StringValue) entry.getValue()).getValue());
                    } else {
                        // Process Other Argument Types
                    }
                }
            }
        }

        // Create The Player Task
        Task task = new Task(process.getName() + name + " StickManScenePlayer") {
            @Override
            public void run() {
                // Select The Scene
                I4GMaryClient mary = I4GMaryClient.instance();
                final SceneScript script = mProject.getSceneScript();
                final SceneGroup group = script.getSceneGroup(name);
                final SceneObject scene = group.select();

                // Scene Visualization
                EventDispatcher.getInstance().convey(new SceneExecutedEvent(this, scene));
                // Process The Turns
                for (SceneTurn turn : scene.getTurnList()) {
                    // Turn Visualization
                    TtsAction va  = new TtsAction();
                    mLogger.message("Executing turn:" + turn.getText());
                    EventDispatcher.getInstance().convey(new TurnExecutedEvent(this, turn));

                    // Get The Turn Speaker
                    final String speaker = turn.getSpeaker();

                    if (speaker == null && mRelationAgentPlayer.get(speaker) != null) {
                        // Get The Default Speaker
                    }
                    else{
                        if( mRelationAgentPlayer.get(speaker).equalsIgnoreCase("stickmanstage")){
                            String gender = StickmanStage.getStickman(speaker).mType.name();
                            va.setGender(gender);
                        }
                        else if (speaker.equalsIgnoreCase("baxter")){
                            va.setGender(Stickman.TYPE.MALE.name());
                        }
                    }

                    // Count The Word Number
                    int wordCount = 0;

                    // Process Utterance
                    for (SceneUttr utt : turn.getUttrList()) {
                        mLogger.message("Executing utterance:" + utt.getText());
                        EventDispatcher.getInstance().convey(new UtteranceExecutedEvent(this, utt));

                        // create a new word time mark sequence based on the current utterance
                        WordTimeMarkSequence wts = new WordTimeMarkSequence(utt.getCleanText());

                        if (!(utt.getCleanText().length() == 0) && !utt.getCleanText().equalsIgnoreCase(utt.getPunct()) || ( speaker!= null && speaker.equals("Baxter"))) {
                            if (mRelationAgentPlayer.get(speaker).equalsIgnoreCase("stickmanstage")) {
                                // Create and add the master event action that controlas all other actions
                                mActionPlayer.addMasterEventAction(new StickmanEventAction(StickmanStage.getStickman(speaker), 0, "Speaking", 3000, wts, false));
                                // add mouth open
                               // mActionPlayer.addAction(new StickmanAction(StickmanStage.getStickman(speaker), 0, "Mouth_O", 201, "", true));

                               // mActionPlayer.addAction(new StickmanAction(StickmanStage.getStickman(speaker), 0, "Mouth_O", 203, "", true));

                               // mActionPlayer.addAction(new StickmanAction(StickmanStage.getStickman(speaker), 0, "Mouth_O", 202, "", true));
                                // add mounth closed
                              // mActionPlayer.addAction(new StickmanAction(StickmanStage.getStickman(speaker), 190, "Mouth_Default", 20, "", false));
                            }
                        }

                        // Process the words of this utterance
                        // remember timemark
                        String tm = mActionPlayer.getTimeMark();
                        LinkedList<Phoneme> phonemes = new LinkedList<>();
                        int index = 0;

                        try {
                            phonemes = mary.getWordPhonemeList(utt.getCleanText());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        for (AbstractWord word : utt.getWordList()) {
                            if (word instanceof SceneWord) {
                                String phonetics = "";
                                long wordDuration = 0;

                                String w = ((SceneWord) word).getText();
                                // add word to the word time mark sequence
                                wts.add(new Word(w));
                                // generate a new time mark after each word
                                wordCount = w.length();
                                try {
                                    wordDuration = mary.getWordDuration(w);
                                    LinkedList<Phoneme> phonemes2 = mary.getWordPhonemeList(w);
                                    int i = 0;
                                    for(; i< phonemes2.size(); i++){
                                       if(!speaker.equals("Baxter")) {//TODO: Quitar luego
                                            Phoneme p = phonemes.get(index + i);
                                            StickmanAction phonemeSA = new StickmanAction(StickmanStage.getStickman(speaker), (int) p.getmStart(), "Mouth_" + p.getLipPosition(), (int) (p.getmEnd() - p.getmStart()), p.getLipPosition(), true);
                                            mActionPlayer.addAction(phonemeSA);
                                        }
                                        else {//TODO: Quitar luego
                                            Phoneme p = phonemes.get(index + i);
                                            BaxterAction phonemeBA = new BaxterAction((int) p.getmStart(), "Mouth_" + p.getLipPosition(), (int) (p.getmEnd() - p.getmStart()), p.getLipPosition(), true);
                                            mActionPlayer.addAction(phonemeBA);
                                        }
                                    }
                                    index += i;

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                va.addWord(word);
                                tm = mActionPlayer.getTimeMark();

                            } else if (word instanceof SceneParam) {
                                // Visualization
                                //mLogger.message("Executing param:" + ((SceneParam) word).getText());
                            } else if (word instanceof ActionObject) {
                                ActionObject ao = ((ActionObject) word);
                                String agent = ao.getAgentName();
                                agent = (agent == null || agent.trim().isEmpty()) ? speaker : agent;

                                if (mRelationAgentPlayer.get(agent).equalsIgnoreCase("stickmanstage")) {
                                    // if there is a master event action, let it decide when to play the action, else play it at timecode 0
                                    StickmanAction sa = new StickmanAction(StickmanStage.getStickman(agent), mActionPlayer.hasMasterEventAction() ? -1 : 0, ao.getName(), 1000, "", false);
                                    if (mActionPlayer.hasMasterEventAction()) {
                                        // give time mark to the master event action
                                        ((EventAction) mActionPlayer.getMasterEventAction()).addTimeMark(tm);
                                        // set time mark to action
                                        sa.setTimeMark(tm);
                                        // add time mark action to action player - to be played at the specfic timemark
                                        mActionPlayer.addTimeMarkAction(sa);
                                        // add time mark to word time mark sequence
                                        wts.add(new TimeMark(tm));
                                    } else {
                                        mActionPlayer.addAction(sa);
                                    }
                                }
                                else if(mRelationAgentPlayer.get(agent).equalsIgnoreCase("baxter")){
                                    BaxterAction ba = new BaxterAction(mActionPlayer.hasMasterEventAction() ? -1 : 0, ao.getName(), 1000, "", false );
                                    if (mActionPlayer.hasMasterEventAction()) {
                                        // give time mark to the master event action
                                        ((EventAction) mActionPlayer.getMasterEventAction()).addTimeMark(tm);
                                        // set time mark to action
                                        ba.setTimeMark(tm);
                                        // add time mark action to action player - to be played at the specfic timemark
                                        mActionPlayer.addTimeMarkAction(ba);
                                        // add time mark to word time mark sequence
                                        wts.add(new TimeMark(tm));
                                    } else {
                                        mActionPlayer.addAction(ba);
                                    }
                                }
                            } else if (word instanceof SceneAbbrev) {
                                // Visualization
                                //mLogger.message("Executing abbreviation:" + ((SceneAbbrev) word).getText());
                            }
                        }
                        mActionPlayer.addAction(va);
                        // play all actions of an utterance
                        //mLogger.message("go ...........");
                        mActionPlayer.play();

                        // wait for all actions to be played
                        mLogger.message("Wait for all actions ...");
                        try {
                            mAllActionSync.acquire();
                        } catch (InterruptedException ex) {
                            mLogger.warning("Execution of scene " + group.getName() + " got interrupted!");
                        }
                    }

                    // Exit if interrupted
                    if (isDone()) {
                        return;
                    }
                }
            }
        };

        // Start the player task
        task.start();
        // Wait for termination
        boolean finished = false;
        while (!finished) {
            try {
                // Join the player task
                task.join();
                // Stop waiting for task
                finished = true;
            } catch (final InterruptedException exc) {
                // Print some warning
                mLogger.warning("Warning: Interrupting process '" + process + "'");
                // Terminate the task
                task.abort();
                // Interrupt the task
                task.interrupt();

            }
        }
    }

    /*
     private boolean init() {
     // Initialize the delay
     try {
     // Check if the config is null
     if (mConfig != null) {
     mDelay = Integer.parseInt(
     mConfig.getProperty("delay"));
     // Print an error message in this case
     mLogger.message("Initializing the default scene player delay with '" + mDelay + "'");
     // Return failure if it does not exist
     return true;
     } else {
     // Print an error message in this case
     mLogger.warning("Warning: Cannot read bad default scene player configuration");
     // Return failure if it does not exist
     return false;
     }
     } catch (final NumberFormatException exc) {
     // Print an error message in this case
     mLogger.failure("Error: Cannot initialize the default scene player delay");
     // Return failure if it does not exist
     return false;
     }
     }
     */
}
