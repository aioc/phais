package core.visualisation;

import games.ttd.Track;

import java.util.ArrayList;
import java.util.List;

public class VisualGameEvent {

	int totalFrames;
	int curFrame;
	public List<Track> tracks;
	
	public VisualGameEvent() {
		tracks = new ArrayList<Track>();
	}
	
	public void addTrack(Track track) {
		tracks.add(track);
	}
}
