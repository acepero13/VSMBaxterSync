package de.dfki.vsm.players.stickman.animation.face;

import de.dfki.vsm.players.stickman.Stickman;
import de.dfki.vsm.players.stickman.animationlogic.Animation;
import de.dfki.vsm.players.stickman.animationlogic.AnimationContent;

import java.util.ArrayList;

/**
 * Created by alvaro on 3/28/16.
 */
public class Speak extends Animation {
    public Speak(Stickman sm, int duration, boolean block) {
        super(sm, duration, block);
    }
    @Override
    public void playAnimation() {
        // smile
        mAnimationPart = new ArrayList<>();
        mAnimationPart.add(new AnimationContent(mStickman.mMouth, "shape", "DEFAULT"));
        playAnimationPart(20);
    }
}
