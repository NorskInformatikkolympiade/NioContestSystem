import org.junit.*;
import java.util.*;

import play.db.jpa.Transactional;
import play.test.*;
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
        Contestant contestant = new Contestant("Ola", "Nordmann", true, false).save();
        Contestant otherContestant = new Contestant("Per", "Nilsen", true, false).save();
        new Submission(contestant, otherTask, "", Language.CPP, new Date(), SubmissionStatus.COMPLETED, 100).save();
        new Submission(otherContestant, task, "", Language.CPP, new Date(), SubmissionStatus.COMPLETED, 100).save();
        
        int score = contestant.getScoreForTask(task.id);
        
        assertEquals(0, score);
    }
    
    @Test
    public void getScoreForTaskShouldReturnZeroWhenAllTheUsersSubmissionsForThatTaskHaveZeroPoints() {
        Task task = new Task(1, "Banana Republic", 100, "").save();
        Contestant user = new Contestant("Ola", "Nordmann", true, false).save();
        new Submission(user, task, "", Language.CPP, new Date(), SubmissionStatus.COMPLETED, 0).save();
        new Submission(user, task, "", Language.CPP, new Date(), SubmissionStatus.QUEUED, 0).save();
        new Submission(user, task, "", Language.CPP, new Date(), SubmissionStatus.RUNNING, 0).save();
        
        int score = user.getScoreForTask(task.id);
        
        assertEquals(0, score);
    }

    @Test
    public void getScoreForTaskShouldReturnTheSubmissionScoreWhenThereIsOnlyOneSubmission() {
        Task task = new Task(1, "Banana Republic", 100, "").save();
        Contestant contestant = new Contestant("Ola", "Nordmann", true, false).save();
        new Submission(contestant, task, "", Language.CPP, new Date(), SubmissionStatus.COMPLETED, 43).save();
        
        int score = contestant.getScoreForTask(task.id);
        
        assertEquals(43, score);
    }
    
    @Test
    public void getScoreForTaskShouldReturnTheGreatestSubmissionScoreWhenThereAreManySubmissions() {
        Task task = new Task(1, "Banana Republic", 100, "").save();
        Contestant contestant = new Contestant("Ola", "Nordmann", true, false).save();
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
        Contestant contestant = new Contestant("Ola", "Nordmann", true, false).save();
        
        int score = contestant.getTotalScore();
        
        assertEquals(0, score);
    }
    
    @Test
    public void getTotalScoreShouldReturnTheSumOfTheGreatestSubmissionScoresForEachTask() {
        Task taskOne = new Task(1, "Banana Republic", 100, "").save();
        Task taskTwo = new Task(2, "Banana Republic", 50, "").save();
        Task taskThree = new Task(3, "Banana Republic", 200, "").save();
        Contestant contestant = new Contestant("Ola", "Nordmann", true, false).save();
        Contestant otherContestant = new Contestant("Per", "Hansen", true, false).save();
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
}
