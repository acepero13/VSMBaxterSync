/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.vsm.players.stickman.animation.face;

import de.dfki.vsm.players.stickman.Stickman;
import de.dfki.vsm.players.stickman.animationlogic.Animation;
import de.dfki.vsm.players.stickman.animationlogic.AnimationContent;
import java.util.ArrayList;

/**
 *
 * @author Patrick Gebhard
 *
 */
public class Mouth_O extends Animation {

	public Mouth_O(Stickman sm, int duration, boolean block) {
		super(sm, duration, block);
	}

	@Override
	public void playAnimation() {
		// smile
		mAnimationPart = new ArrayList<>();
		String shape = "O";
		if(mDuration == 201){
			shape = "ONE";
		}
		if(mDuration == 202){
			shape = "TWO";
		}
		if(mDuration == 203){
			shape = "THREE";
		}
		if(mDuration == 204){
			shape = "FOUR";
		}
		if(mDuration == 205){
			shape = "FIVE";
		}
		if(mDuration == 206){
			shape = "SIX";
		}
		if(mDuration == 207){
			shape = "SEVEN";
		}
		if(mDuration == 208){
			shape = "EIGHT";
		}
		if(mDuration == 209){
			shape = "NINE";
		}

		if(mDuration == 210){
			shape = "TEN";
		}
		mAnimationPart.add(new AnimationContent(mStickman.mMouth, "shape", shape));
		playAnimationPart(100);
	}
}
