package games.spies;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import core.interfaces.PersistentPlayer;
import core.server.ClientConnection;
import core.server.DisconnectedException;

public class MINARWIN implements ClientConnection {

	private GameState state;
	private String mustSend = "";
	private String preString = "";
	private List<PersistentPlayer> players;
	private int curMove = 0;
	private boolean connected;
	
	public MINARWIN() {
		connected = true;
	}
	
	@Override
	public void sendInfo(String s) {
		// No need for their info
		preString = s;
		if (s.contains("GAMEOVER")) {
			//connected = false;
		}
	}

	@Override
	public void sendInfo(int i) {
		// See above
	}

	@Override
	public String getStrInput() throws DisconnectedException {
		if (mustSend.length() > 0) {
			return "ACTION " + mustSend;
		}
		if (preString.length() > 0) {
			String[] strs = preString.split("\\s");
			if (strs[0].equals("NEWGAME")) {
				curMove = 0;
				preString = "";
				mustSend = "";
				return "READY " + strs[5];
			}
		}
		// Compute move, then send
		GamePerson gp[] = new GamePerson[state.getNumberPlayers()];
		for (int i = 0; i < state.getNumberPlayers(); i++) {
			gp[i] = state.getPerson(i);
		}
		return "ACTION " + getMove(gp);
	}

	@Override
	public int getIntInput() throws DisconnectedException {
		return 0;
	}

	@Override
	public void disconnect() {
		// IN SOVIET RUSSIA, CLIENT DISCONNECTS YOU
		//System.exit(1);
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean checkConnected() {
		return true;
	}

	public void giveMeAllYourStates(GameState s, List<PersistentPlayer> p) {
		state = s;
		players = p;
	}

	private String getMove(GamePerson gp[]) {
		// Compute what happens for each move
		// Evaluate goodness
		long timeS = System.currentTimeMillis();
		String moves = "^>v<()S-";
		int best = -1000000000;
		List<String> bestMoves = new ArrayList<String>();
		for (int i = 0; i < 1 << (3 * 5); i++) {
			String myMove = "";
			for (int j = 0; j < 5; j++) {
				myMove += moves.charAt(((i & (7 << (3 * j))) >> (3 * j)));
			}
			GameState gs = getPredictedState(myMove, gp);
			int r = getRating(gs, myMove);
			if (r > best) {
				best = r;
				bestMoves = new ArrayList<String>();
			}
			if (r == best) {
				bestMoves.add(myMove);
			}
		}
		mustSend = "";
		String bestMove = bestMoves.get(new Random(System.currentTimeMillis()).nextInt(bestMoves.size()));
		curMove++;
		return bestMove;
	}

	private GameState getPredictedState(String move, GamePerson gp[]) {
		GameVisualiser gv = new GameVisualiser(players, 0, 0);
		GameState gs = new GameState(gp.length, state.getBoardSize(), state.getMaxHealth(), gv);
		for (int i = 0; i < gp.length; i++) {
			gs.getPerson(i).fromGamePerson(gp[i]);
		}
		try {
			mustSend = move;
			gs.getPerson(gp.length - 1).action = Action.getAction(this);
		} catch (Exception e) {
			System.out.println("DAMN IT");
		}
		gs.implementMoves();
		return gs;
	}
	
	public static Clip getMusic() {
		Clip clip = null;
		try {
			AudioInputStream soundIn = AudioSystem.getAudioInputStream(new BufferedInputStream(SpiesMain.class.getResourceAsStream("/resources/kremlinmusic.wav")));
			AudioFormat format = soundIn.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			clip = (Clip)AudioSystem.getLine(info);
			clip.open(soundIn);
		} catch (Exception e) {
			System.err.println(e);
			System.err.println(e.getMessage());
		}
		return clip;
	}
	

	private int getRating(GameState gs, String move) {
		// Basically, 100000 * myHealth, - 1000 * sum of every one else's health
		// - closest's alive person distance
		// If we shoot more than once, minuses 50000
		int total = 100000 * gs.getPerson(gs.getNumberPlayers() - 1).health;
		Position myP = gs.getPerson(gs.getNumberPlayers() - 1).position;
		if (curMove >= 10) {
			for (int i = 0; i < gs.getNumberPlayers() - 1; i++) {
				total -= 1000 * gs.getPerson(i).health;
				if (gs.getPerson(i).health > 0) {
					int dis = -(Math.abs(myP.c - gs.getPerson(i).position.c) + Math.abs(myP.r
							- gs.getPerson(i).position.r));
					total += dis;
				}
			}
			int count = 0;
			for (int i = 0; i < 5; i++) {
				if (move.charAt(i) == 'S') {
					count++;
				}
			}
			if (count > 5) {
				total -= 50000;
			}
		} else {
			// Want to spin around, dodging bullets
			for (int i = 0; i < 5; i++) {
				if (move.charAt(i) == 'S') {
					total -= 1000;
				}
				if (move.charAt(i) == '(' || move.charAt(i) == ')' || move.charAt(i) == '-') {
					total -= 100;
				}
			}
		}

		return total;
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
		int outcome = 0;
		int WON = 1;
		int DRAW = 2;
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
}
