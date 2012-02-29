import javax.persistence.EntityManager;

import models.Submission;
import models.SubmissionStatus;
import play.db.jpa.JPA;
import play.jobs.Job;

//TODO: Not sure about what the best way to do this is, since an @OnApplicationStart job 
//		is required to finish before the webapp starts processing requests. We might want 
//		to use regular threads instead and kick it off from Bootstrap.
//@Every("1s")
public class Grader extends Job implements Runnable {
	private static Grader instance = new Grader();
	private boolean shouldStop;
	private boolean isRunning;
	
	private Grader() {
	}
	
	public static Grader getInstance() {
		return instance;
	}
	
	public void doJob() {
		System.out.println("Starting grader");
		JPA.em().getTransaction().rollback(); // Abort the transaction Play! has started for the job; we'll control our transactions ourselves
		isRunning = true;
		shouldStop = false;
		while (!shouldStop) {
			try {
				while (!shouldStop) {
					Submission submission = getNextQueuedSubmission();
					if (submission == null)
						break;
					grade(submission);
				}
				System.out.println("Found nothing to grade; waiting for one second");
				Thread.sleep(1000);
			}
			catch (Exception e) {
				System.out.println("Exception thrown while grading: " + e);
			}
		}
		isRunning = false;
		shouldStop = false;
	}
	
	private Submission getNextQueuedSubmission() {
		return Submission.find("status = ? order by submittedAt asc", SubmissionStatus.QUEUED).first();
	}
	
	private void grade(Submission submission) {
		System.out.println("Grading the following submission:\n" +
				"  User:         " + submission.contestant.getFullName() + "\n" +
				"  Task:         " + submission.task.title + "\n" +
				"  Submitted at: " + submission.submittedAt);
		JPA.em().getTransaction().begin();
		submission.status = SubmissionStatus.RUNNING;
		submission.save();
		JPA.em().flush();
		JPA.em().getTransaction().commit();
		try {
			Thread.sleep(5000);
		}
		catch (InterruptedException e) {}
		JPA.em().getTransaction().begin();
		submission.status = SubmissionStatus.COMPLETED;
		submission.score = (int)(Math.random() * 101);
		submission.save();
		JPA.em().flush();
		JPA.em().getTransaction().commit();
		
		System.out.println("Done grading the following submission:\n" +
				"  User:         " + submission.contestant.getFullName() + "\n" +
				"  Task:         " + submission.task.title + "\n" +
				"  Submitted at: " + submission.submittedAt + "\n" + 
				"  Score:        " + submission.score);
	}
	
	public void stop() {
		System.out.println("Shutting down grader; waiting for at most 30 seconds...");
		shouldStop = true;
		long start = System.currentTimeMillis();
		while (isRunning) {
			if (System.currentTimeMillis() - start > 30000) {
				System.out.println("Grader failed to shut down in 30 seconds");
				return;
			}
			try {
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
			}
		}
		System.out.println("Grader shutdown complete");
	}
}
