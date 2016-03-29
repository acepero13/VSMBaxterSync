package de.dfki.vsm.players.action.sequence;

import de.dfki.vsm.players.stickman.body.Mouth;

import java.util.HashMap;

/**
 * Created by alvaro on 3/28/16.
 */
public class Phoneme {
    private String mValue;
    private long mStart;
    private long mEnd;
    private HashMap<String, String> lipMap = new HashMap<>();


    public Phoneme(String value, long start, long end){
        mValue = value;
        mStart = start;
        mEnd = end;
        lipMap.put("w", "SEVEN");
        lipMap.put("u", "SEVEN");
        lipMap.put("E", "FOUR");
        lipMap.put("l", "ONE"); //FOURTEEN
        lipMap.put("k", "THREE"); //TWENTY
        lipMap.put("@", "ONE");
        lipMap.put("m", "Default");
        lipMap.put("n", "ONE"); //NINETEEN
        lipMap.put("t", "ONE"); //NINETEEN
        lipMap.put("AI", "NINE");
        lipMap.put("EI", "FOUR");
        lipMap.put("@U", "FIVE");
        lipMap.put("I", "ONE");//SIX
        lipMap.put("i", "ONE");//SIX
        lipMap.put("z", "ONE");//FIFTEEN
        lipMap.put("s", "ONE");//FIFTEEN
        lipMap.put("A", "TWO");
        lipMap.put("r", "FIVE");//THIRTEEN
        lipMap.put("j", "FIVE");

       // lipMap.put("s", Mouth.SHAPE.SIX);//TODO: Make this face

    }
    public String getmValue() {
        return mValue;
    }

    public void setmValue(String mValue) {
        this.mValue = mValue;
    }

    public long getmStart() {
        return mStart;
    }

    public void setmStart(long mStart) {
        this.mStart = mStart;
    }

    public long getmEnd() {
        return mEnd;
    }

    public void setmEnd(long mEnd) {
        this.mEnd = mEnd;
    }

    public String getLipPosition(String phoneme){
        if(lipMap.containsKey(phoneme)){
            return lipMap.get(phoneme);
        }
        else{
            return "Default";
        }
    }

    public String getLipPosition(){
        if(lipMap.containsKey(mValue)){
            return lipMap.get(mValue);
        }
        else{
            return "Default";
        }
    }
}
