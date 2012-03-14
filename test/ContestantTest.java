import org.junit.*;
import java.util.*;

import play.db.jpa.Transactional;
import play.test.*;
import viewmodels.ScoreboardEntry;
import models.*;

public class ContestantTest extends UnitTest {
	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}
	
    @Test
    public void getScoreForTaskShouldReturnZeroWhenUserHasNoSubmissionsForThatTask() {
        Task task = new Task(1, "Banana Republic", 100, "").save();
        Task otherTask = new Task(2, "Foo", 200, "").save();
        Contestant contestant = new Contestant("Ola", "Nordmann", false).save();
        Contestant otherContestant = new Contestant("Per", "Nilsen", false).save();
        new Submission(contestant, otherTask, "", Language.CPP, new Date(), SubmissionStatus.COMPLETED, 100).save();
        new Submission(otherContestant, task, "", Language.CPP, new Date(), SubmissionStatus.COMPLETED, 100).save();
        
        int score = contestant.getScoreForTask(task.id);
        
        assertEquals(0, score);
    }
    
    @Test
    public void getScoreForTaskShouldReturnZeroWhenAllTheUsersSubmissionsForThatTaskHaveZeroPoints() {
        Task task = new Task(1, "Banana Republic", 100, "").save();
        Contestant user = new Contestant("Ola", "Nordmann", false).save();
        new Submission(user, task, "", Language.CPP, new Date(), SubmissionStatus.COMPLETED, 0).save();
        new Submission(user, task, "", Language.CPP, new Date(), SubmissionStatus.QUEUED, 0).save();
        new Submission(user, task, "", Language.CPP, new Date(), SubmissionStatus.RUNNING, 0).save();
        
        int score = user.getScoreForTask(task.id);
        
        assertEquals(0, score);
    }

    @Test
    public void getScoreForTaskShouldReturnTheSubmissionScoreWhenThereIsOnlyOneSubmission() {
        Task task = new Task(1, "Banana Republic", 100, "").save();
        Contestant contestant = new Contestant("Ola", "Nordmann", false).save();
        new Submission(contestant, task, "", Language.CPP, new Date(), SubmissionStatus.COMPLETED, 43).save();
        
        int score = contestant.getScoreForTask(task.id);
        
        assertEquals(43, score);
    }
    
    @Test
    public void getScoreForTaskShouldReturnTheGreatestSubmissionScoreWhenThereAreManySubmissions() {
        Task task = new Task(1, "Banana Republic", 100, "").save();
        Contestant contestant = new Contestant("Ola", "Nordmann", false).save();
        new Submission(contestant, task, "", Language.CPP, new Date(2012, 2, 5, 12, 5, 9), SubmissionStatus.COMPLETED, 0).save();
        new Submission(contestant, task, "", Language.CPP, new Date(2012, 2, 5, 12, 15, 0), SubmissionStatus.COMPLETED, 10).save();
        new Submission(contestant, task, "", Language.CPP, new Date(2012, 2, 5, 12, 23, 18), SubmissionStatus.COMPLETED, 58).save();
        new Submission(contestant, task, "", Language.CPP, new Date(2012, 2, 5, 13, 59, 59), SubmissionStatus.COMPLETED, 29).save();
        new Submission(contestant, task, "", Language.CPP, new Date(2012, 2, 5, 14, 25, 0), SubmissionStatus.RUNNING, 0).save();
        new Submission(contestant, task, "", Language.CPP, new Date(2012, 2, 5, 14, 25, 9), SubmissionStatus.QUEUED, 0).save();
        
        int score = contestant.getScoreForTask(task.id);
        
        assertEquals(58, score);
    }
    
    @Test
    public void getTotalScoreShouldReturnZeroWhenThereAreNoSubmissions() {
        new Task(1, "Banana Republic", 100, "").save();
        new Task(2, "Banana Republic", 50, "").save();
        new Task(3, "Banana Republic", 200, "").save();
        Contestant contestant = new Contestant("Ola", "Nordmann", false).save();
        
        int score = contestant.getTotalScore();
        
        assertEquals(0, score);
    }
    
    @Test
    public void getTotalScoreShouldReturnTheSumOfTheGreatestSubmissionScoresForEachTask() {
        Task taskOne = new Task(1, "Banana Republic", 100, "").save();
        Task taskTwo = new Task(2, "Banana Republic", 50, "").save();
        Task taskThree = new Task(3, "Banana Republic", 200, "").save();
        Contestant contestant = new Contestant("Ola", "Nordmann", false).save();
        Contestant otherContestant = new Contestant("Per", "Hansen", false).save();
        new Submission(contestant, taskOne, "", Language.CPP, new Date(2012, 2, 5, 12, 5, 9), SubmissionStatus.COMPLETED, 0).save();
        new Submission(contestant, taskOne, "", Language.CPP, new Date(2012, 2, 5, 12, 15, 0), SubmissionStatus.COMPLETED, 10).save();
        new Submission(contestant, taskTwo, "", Language.CPP, new Date(2012, 2, 5, 12, 23, 18), SubmissionStatus.COMPLETED, 58).save();
        new Submission(contestant, taskTwo, "", Language.CPP, new Date(2012, 2, 5, 13, 59, 59), SubmissionStatus.COMPLETED, 29).save();
        new Submission(contestant, taskThree, "", Language.CPP, new Date(2012, 2, 5, 14, 25, 0), SubmissionStatus.COMPLETED, 47).save();
        new Submission(contestant, taskThree, "", Language.CPP, new Date(2012, 2, 5, 14, 25, 0), SubmissionStatus.COMPLETED, 23).save();
        new Submission(contestant, taskThree, "", Language.CPP, new Date(2012, 2, 5, 14, 25, 0), SubmissionStatus.RUNNING, 1).save();
        new Submission(contestant, taskOne, "", Language.CPP, new Date(2012, 2, 5, 14, 25, 9), SubmissionStatus.RUNNING, 0).save();
        new Submission(contestant, taskOne, "", Language.CPP, new Date(2012, 2, 5, 14, 25, 9), SubmissionStatus.QUEUED, 0).save();
        new Submission(otherContestant, taskOne, "", Language.CPP, new Date(2012, 2, 5, 12, 5, 9), SubmissionStatus.COMPLETED, 50).save();
        
        int score = contestant.getTotalScore();
        
        assertEquals(115, score);
    }
    
    @Test
    public void getScoreboardShouldReturnAllContestantsOrderedByTotalScore() {
    	Task taskOne = new Task(1, "Banana Republic", 100, "").save();
        Task taskTwo = new Task(2, "Banana Republic", 50, "").save();
        Contestant contestantOne = new Contestant("Ola", "Nordmann", false).save();
        Contestant contestantTwo = new Contestant("Per", "Hansen", false).save();
        Contestant contestantThree = new Contestant("Lars", "Nilsen", false).save();
        TestData.AddSubmissions(contestantOne, taskOne, 2, 29, 28);
        TestData.AddSubmissions(contestantOne, taskTwo, 50, 23, 0);
        TestData.AddSubmissions(contestantTwo, taskOne, 20, 99);
        TestData.AddSubmissions(contestantTwo, taskTwo, 0, 0, 0);
        TestData.AddSubmissions(contestantThree, taskOne, 1, 2, 3, 4, 0, 5, 0);
        
        List<ScoreboardEntry> scoreboard = Contestant.getScoreboard();
        
        assertEquals(3, scoreboard.size());
        assertScoreboardEntry(scoreboard.get(0), 1, contestantTwo.id, "Per Hansen", 99);
        assertScoreboardEntry(scoreboard.get(1), 2, contestantOne.id, "Ola Nordmann", 79);
        assertScoreboardEntry(scoreboard.get(2), 3, contestantThree.id, "Lars Nilsen", 5);
    }
    
    @Test
    public void getScoreboardShouldGiveEqualPositionNumbersToTiedContestantsAndOrderThemByNameAndSkipPositionNumbers() {
    	Task task = new Task(1, "Banana Republic", 100, "").save();
        Contestant contestantOne = new Contestant("Ola", "Nordmann", false).save();
        Contestant contestantTwo = new Contestant("Per", "Hansen", false).save();
        Contestant contestantThree = new Contestant("Lars", "Nilsen", false).save();
        Contestant contestantFour = new Contestant("Henrik", "Olsen", false).save();
        Contestant contestantFive = new Contestant("Andreas", "Larsen", false).save();
        TestData.AddSubmissions(contestantOne, task, 42);
        TestData.AddSubmissions(contestantTwo, task, 10);
        TestData.AddSubmissions(contestantThree, task, 42);
        TestData.AddSubmissions(contestantFour, task, 98);
        TestData.AddSubmissions(contestantFive, task, 42);
        
        List<ScoreboardEntry> scoreboard = Contestant.getScoreboard();
        
        assertEquals(5, scoreboard.size());
        assertScoreboardEntry(scoreboard.get(0), 1, contestantFour.id, "Henrik Olsen", 98);
        assertScoreboardEntry(scoreboard.get(1), 2, contestantFive.id, "Andreas Larsen", 42);
        assertScoreboardEntry(scoreboard.get(2), 2, contestantThree.id, "Lars Nilsen", 42);
        assertScoreboardEntry(scoreboard.get(3), 2, contestantOne.id, "Ola Nordmann", 42);
        assertScoreboardEntry(scoreboard.get(4), 5, contestantTwo.id, "Per Hansen", 10);
    }
    
    @Test
    public void getScoreboardShouldGiveFirstPlaceToEverybodyWhenAllScoresAreEqual() {
    	Task task = new Task(1, "Banana Republic", 100, "").save();
        Contestant contestantOne = new Contestant("Ola", "Nordmann", false).save();
        Contestant contestantTwo = new Contestant("Per", "Hansen", false).save();
        Contestant contestantThree = new Contestant("Lars", "Nilsen", false).save();
        TestData.AddSubmissions(contestantOne, task, 8);
        TestData.AddSubmissions(contestantTwo, task, 8);
        TestData.AddSubmissions(contestantThree, task, 8);
        
        List<ScoreboardEntry> scoreboard = Contestant.getScoreboard();
        
        assertEquals(3, scoreboard.size());
        assertScoreboardEntry(scoreboard.get(0), 1, contestantThree.id, "Lars Nilsen", 8);
        assertScoreboardEntry(scoreboard.get(1), 1, contestantOne.id, "Ola Nordmann", 8);
        assertScoreboardEntry(scoreboard.get(2), 1, contestantTwo.id, "Per Hansen", 8);
    }
    
    @Test
    public void getScoreboardShouldExcludeAdmins() {
    	Contestant contestantOne = new Contestant("Arne", "Nordmann", false).save();
        Contestant contestantTwo = new Contestant("Fredrik", "Hansen", false).save();
        Contestant contestantThree = new Contestant("Lars", "Nilsen", false).save();
        new Contestant("Karl", "Andersen", true).save();
        new Contestant("Ole", "Larsen", true).save();
        
        List<ScoreboardEntry> scoreboard = Contestant.getScoreboard();
        
        assertEquals(3, scoreboard.size());
        assertEquals(contestantOne.id.longValue(), scoreboard.get(0).contestantId);
        assertEquals(contestantTwo.id.longValue(), scoreboard.get(1).contestantId);
        assertEquals(contestantThree.id.longValue(), scoreboard.get(2).contestantId);
    }
    
    private void assertScoreboardEntry(ScoreboardEntry actual, long expectedPosition, long expectedContestantId, String expectedName, int expectedScore) {
    	assertEquals(expectedPosition, actual.position);
        assertEquals(expectedContestantId, actual.contestantId);
        assertEquals(expectedName, actual.name);
        assertEquals(expectedScore, actual.score);
    }
}
