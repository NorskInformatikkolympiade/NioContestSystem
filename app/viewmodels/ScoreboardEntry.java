package viewmodels;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardEntry {
	public int position;
	public long contestantId;
	public String name;
	public int score;
	public List<Integer> taskScores;
	
	public ScoreboardEntry() {
		taskScores = new ArrayList<Integer>();
	}
}
