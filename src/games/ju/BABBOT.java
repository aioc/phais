package games.ju;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import core.interfaces.PersistentPlayer;
import core.server.ClientConnection;

public class BABBOT extends Player {
	
	public static final String THE_NAME	= "BabBot";
	public static final int WON	= 0;
	public static final int DRAW = 1;
	public static final int LOSE = 2;

	private int roundVals[];
	private int curRound;
	private int myScore;
	private int theirScore;
	private boolean[] iHave;
	private boolean[] theyHave;
	private int prePlayed;
	private int outcome;
	
	public BABBOT (int PLAYERCONSUMED, ClientConnection CONNECTIONCONSUMED) {
		super(PLAYERCONSUMED, CONNECTIONCONSUMED);
	}
	
	public void GIVEMETHEROUNDS (int[] vals) {
		roundVals = vals;
		curRound = 0;
		myScore = 0;
		theirScore = 0;
		iHave = new boolean[vals.length];
		theyHave = new boolean[vals.length];
		for (int i = 0; i < vals.length; i++) {
		   iHave[i] = true;
		   theyHave[i] = true;
		}
	}
	
	public void GIVEMETHECARD (int player, int card) {
		prePlayed = card;
	}
	
	private int getCard() {
	   // First, pretend we lost (we will play the lowest card we have)
	   boolean copyIHave[] = new boolean[roundVals.length];
	   boolean copyTheyHave[] = new boolean[roundVals.length];
	   for (int i = 0; i < roundVals.length; i++) {
	      copyIHave[i] = iHave[i];
	      copyTheyHave[i] = theyHave[i];
	   }
	   copyTheyHave[prePlayed] = false;
	   for (int i = 0; i < roundVals.length; i++) {
	      if (copyIHave[i]) {
	         copyIHave[i] = false;
	         break;
	      }
	   }
	   int copyMyScore = myScore;
	   int copyTheirScore = theirScore + roundVals[curRound];
	   // Now, see how many we can win
	   int amoLose = 0;
	   for (int i = 0; i < roundVals.length; i++) {
	      if (copyTheyHave[i]) {
	         boolean found = false;
	         int val = 0;
	         for (int j = i + 1; !found && j < roundVals.length; j++) {
	            if (copyIHave[j]) {
	               found = true;
	               val = j;
	            }
	         }
	         if (!found) {
	            // I lose the rest
	            for (int j = 0; j < roundVals.length; j++) {
	               if (copyTheyHave[j]) {
	                  amoLose++;
	               }
	            }
	         } else {
	            // Cancel them out
	            copyIHave[val] = false;
	            copyTheyHave[i] = false;
	            
	         }
	      }
	   }
	   // We now know how many we can possibly lose. Sort the rest of the values
	   int restVals[] = new int[roundVals.length - curRound - 1];
	   for (int i = curRound + 1; i < roundVals.length; i++) {
	      restVals[i - curRound - 1] = roundVals[i];
	   }
	   for (int i = 0; i < restVals.length; i++) {
	      for (int j = i + 1; j < restVals.length; j++) {
	         if (restVals[i] < restVals[j]) {
	            int temp = restVals[i];
	            restVals[i] = restVals[j];
	            restVals[j] = temp;
	         }
	      }
	   }
	   // Now assume we lose the biggest (the front amoLose)
	   for (int i = 0; i < restVals.length; i++) {
	      if (i < amoLose) {
	         copyTheirScore += restVals[i];
	      } else {
	         copyMyScore += restVals[i];
	      }
	   }
	   // Did we lose?
	   int toPlay = 0;
      boolean found = false;
	   if (copyMyScore < copyTheirScore) {
	      // Play one above. Else play lowest
	      for (int i = prePlayed + 1; i < roundVals.length && !found; i++) {
	         if (iHave[i]) {
	            found = true;
	            toPlay = i;
	         }
	      }
	      if (!found) {
	         for (int i = 0; i < roundVals.length && !found; i++) {
	            if (iHave[i]) {
	               found = true;
	               toPlay = i;
	            }
	         }
	      }
	   } else {
	      // Play lowest
	      for (int i = 0; i < roundVals.length && !found; i++) {
	         if (iHave[i]) {
	            found = true;
	            toPlay = i;
	         }
	      }
	   }
	   return toPlay;
	}
	
	public int GETTHECARD () {
		int valToPlay = getCard();
		iHave[valToPlay] = false;
		theyHave[prePlayed] = false;
		if (valToPlay > prePlayed) {
			outcome = WON;
			myScore += roundVals[curRound];
		} else if (valToPlay == prePlayed) {
			outcome = DRAW;
		} else {
			outcome = LOSE;
			theirScore += roundVals[curRound];
		}
		curRound++;
		return valToPlay;
	}
	
	
	public void TAUNTPLAYER (PersistentPlayer p) {
		String winStrings[] = {
			"Check.",
			"Nice try.",
			"I can read you like a book.",
			"Your efforts are wasted.",
			"Give up and go home, %.",
			"I will decompose you into two handlebodies.",
			"I will Heegaard split you.",
			"I will smash an undifferentiable hole into your surface.",
			"Losing suits you well.",
			"You like that, %?",
			"You're epsilon and I'm a floor function.",
			"You're just a compact oriented 3-manifold that I'm going to turn into a Heegaard splitting.",
			"I'm adding you to my trophy wall with all my other students' heads.",
			"%? More like ~%()...",
			"You went array out of bounds the moment you stepped into the ring with me.",
			"Your intellect is so trivial that I could prove it's an unknot in P-time.",
			"Your failure is so complete, I could reduce 3-SAT to it.",
			"You're so lost, I bet you'd end up on the wrong side of a Mobius strip.",
			"Your strategy has more holes than a Menger sponge.",
			"LEMMA: % is homeomorphic to roadkill.",
			"You're a greedy algorithm and I'm a counterexample.",
			"I bet you can't even turn a sphere inside out without creating creases.",
			"I bet Charles Babbage doesn't pay *you* royalties for using your name.",
			"I've already proved I'll win this game. This demonstration is purely for the benefit of your feeble mind.",
			"Your canonical decomposition is combinatorially equivalent to a potato.",
			"You're a few bytes short of a char.",
			"I optimised your algorithm to O(1): printf(\"Lose\\n\");",
			"I could beat you with LOGSPACE memory.",
			"Beating you is so trivial that my algorithm got bored and decided to solve the Travelling Salesperson Problem.",
			"If you're pretending to suck, you just passed that Turing test.",
			"Your logic is so faulty, you just broke this machine's ALU.",
			"You're so predictable that I can represent you as a DFA with three states.",
			"Are you coded in Python, or do you actually think that slow?",
			"Your crushing defeat is more total than the natural order on the integers.",
			"You're more dense than the set of irrationals.",
			"With AI like this, you should be working for Altavista.",
			"#define % \"massive failure\"",
			"You're so irrelevant you were deprecated from MS Word.",
			"Your behaviour is so erratic it's uncomputable.",
			"I once wrote a Hello World program that played better than you.",
			"You could improve your algorithm by replacing it with 'cat /dev/urandom'.",
			"I drink my coffee from a torus. You're still drinking baby formula from a Klein bottle.",
			"If your code was any more buggy, it'd be a Microsoft date calculator.",
			"Suppose an optimal algorithm O exists which differs from yours. Guess what. It's mine.",
			"I once played laser tag with the students. Australia didn't go to the IOI that year because there was nobody left to choose a team from.",
			"I could binary search for your IQ, but a linear search from 0 would be faster.",
			"Your IQ is a single-digit number. In binary.",
			"I discovered a faster-growing function than Ackermann! It's your failure over time.",
			"Are you trying to get your score so far negative that you overflow?",
			"Assume you lose the first game. For each game i, you lose game i+1. Therefore, I win.",
			"I could represent the number of smart plays you've made with a one-bit integer, and still have room for more.",
			"There's more to psychological jujitsu than getting psychologically thrown to the ground again and again, but you clearly never got the memo.",
			"My black belt in psychological jujitsu beats your white belt in curling into a ball and crying.",
			"There aren't enough bits in my supercomputer cluster's collective memory to represent how hard you fail."
		};
		String drawStrings[] = {
			"Detente, my friend.",
			"It seems neither of us is the victor here.",
			"Our wits have matched."
		};
		String lostStrings[] = {
			"It seems you win this round.",
			"I concede the battle. But not the war.",
			"Smiling, %? You won't be for long.",
			"Well played, %. But your victory will be short-lived.",
			"Oh, you think that's enough to save you?",
			"Oh, you think you've won?",
			"Even a greedy algorithm is right sometimes."
		};
		String gotten = null;
		if (outcome == WON) {
			gotten = winStrings[new Random().nextInt(winStrings.length)];
		} else if (outcome == DRAW) {
			gotten = drawStrings[new Random().nextInt(drawStrings.length)];
		} else {
			gotten = lostStrings[new Random().nextInt(lostStrings.length)];
		}
		gotten = gotten.replaceAll("%", p.getName());
		p.getConnection().sendInfo("TAUNT " + gotten);
	}
	

	@Override
	public String getName() {
		return THE_NAME;
	}

	@Override
	public void generateNewName() {
		System.out.println ("BAB DOES NOT NEED A NAME");
	}
	
	@Override
	public int getColour() {
		return Color.RED.getRGB();
	}
	
	public static Clip playSweetBABMusic() {
		Clip clip = null;
		try {
			AudioInputStream soundIn = AudioSystem.getAudioInputStream(new BufferedInputStream(Main.class.getResourceAsStream("/resources/babmusic.wav")));
			AudioFormat format = soundIn.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			clip = (Clip)AudioSystem.getLine(info);
			clip.open(soundIn);
			clip.start();
		} catch (Exception e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		return clip;
	}
}
