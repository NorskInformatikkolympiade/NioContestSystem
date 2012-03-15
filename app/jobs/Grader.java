package jobs;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import models.DataSet;
import models.Submission;
import models.SubmissionStatus;
import models.cmd.CommandLineResult;
import models.compilers.CompileResult;
import models.compilers.CompileStatus;

import org.joda.time.DateTime;

import play.db.jpa.JPA;
import play.jobs.Job;
import utilities.cmd.CommandLineExecutor;
import utilities.cmd.FileHelper;
import utilities.cmd.ICommandLineExecutor;
import utilities.cmd.IFileHelper;
import utilities.compilers.Compilers;
import utilities.compilers.ICompilers;

public class Grader extends Job implements Runnable {
	private static final Grader instance = new Grader();
	private final ICompilers compilers;
	private final ICommandLineExecutor commandLineExecutor;
	private final IFileHelper fileHelper;
	private boolean shouldStop;
	private boolean isRunning;
	static boolean disableGradingDuringTesting;
	
	private Grader() {
		this(Compilers.instance(), new CommandLineExecutor(), new FileHelper());
	}
	
	// For testing only
	Grader(ICompilers compilers, ICommandLineExecutor commandLineExecutor, IFileHelper fileReader) {
		this.compilers = compilers;
		this.commandLineExecutor = commandLineExecutor;
		this.fileHelper = fileReader;
	}
	
	public static Grader getInstance() {
		return instance;
	}
	
	public void doJob() {
		if (disableGradingDuringTesting)
			return;
		System.out.println("Starting grader");
		JPA.em().getTransaction().rollback(); // Abort the transaction Play! has started for the job; we'll control our transactions ourselves
		int count = 0;
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
				if (++count % 100 == 0)
					System.out.println("Grader is alive (" + new DateTime().toString("hh:mm:ss") + ")");
				Thread.sleep(1000);
			}
			catch (Exception e) {
				System.out.println("Exception thrown while grading: " + e);
				e.printStackTrace();
			}
		}
		isRunning = false;
		shouldStop = false;
	}
	
	private Submission getNextQueuedSubmission() {
		return Submission.find("status = ? order by submittedAt asc", SubmissionStatus.QUEUED).first();
	}
	
	// Default visibility for testing
	void grade(Submission submission) {
		System.out.println("Grading the following submission:\n" +
				"  User:         " + submission.contestant.getFullName() + "\n" +
				"  Task:         " + submission.task.title + "\n" +
				"  Submitted at: " + submission.submittedAt);
		
		try {
			JPA.em().getTransaction().begin();
			submission.status = SubmissionStatus.RUNNING;
			submission.save();
			JPA.em().flush();
			JPA.em().getTransaction().commit();
		}
		catch (Exception e) {
			System.out.println("Error occurred when trying to set task status to " + SubmissionStatus.RUNNING + ": " + e);
			e.printStackTrace();
			JPA.em().getTransaction().rollback();
			return;
		}
		
		//TODO: Can we customize paths for each developer?
		CompileResult compileResult = compilers.compile(submission.language, submission.sourceCode, "E:\\Private\\eclipse-workspace\\NioContestSystem\\work", "Program.exe");
		System.out.println("Compilation done in " + compileResult.duration + " ms. Result: " + compileResult.status);
		
		if (compileResult.status == CompileStatus.OK) {
			submission.score = runCompiledProgramOnDataSets("E:\\Private\\eclipse-workspace\\NioContestSystem\\work\\Program.exe", submission);
		}
		
		try {
			JPA.em().getTransaction().begin();
			submission.status = SubmissionStatus.COMPLETED;
			submission.save();
			JPA.em().flush();
			JPA.em().getTransaction().commit();
		}
		catch (Exception e) {
			System.out.println("Error occurred when trying to set task status to " + SubmissionStatus.COMPLETED + ": " + e);
			e.printStackTrace();
			JPA.em().getTransaction().rollback();
			return;
		}
		
		System.out.println("Done grading the following submission:\n" +
				"  User:         " + submission.contestant.getFullName() + "\n" +
				"  Task:         " + submission.task.title + "\n" +
				"  Submitted at: " + submission.submittedAt + "\n" + 
				"  Score:        " + submission.score);
	}
	
	private int runCompiledProgramOnDataSets(String programPath, Submission submission) {
		System.out.println("Running compiled program...");
		int weightSum = 0, successfulWeightSum = 0;
		for (DataSet dataSet : submission.task.dataSets) {
			weightSum += dataSet.weight;
			System.out.print("  Set #" + dataSet.number + " (" + dataSet.weight + ")... ");
			if (runCompiledProgramOnDataSet(programPath, dataSet)) {
				successfulWeightSum += dataSet.weight;
				System.out.println("correct");
			}
			else {
				System.out.println("incorrect");
			}
		}
		if (weightSum == 0)
			return 0;
		int score = submission.task.weight * successfulWeightSum / weightSum;
		System.out.println("  Score: " + score + " (of " + submission.task.weight + ")");
		return score;
	}
	
	private boolean runCompiledProgramOnDataSet(String programPath, DataSet dataSet) {
		try {
			byte[] inputData = fileHelper.readAllBytes(dataSet.getInputFileName());
			CommandLineResult runResult = commandLineExecutor.execute(
					new String[] {"e:\\Private\\eclipse-workspace\\NioContestSystem\\ProperRunAs\\bin\\Release\\ProperRunAs.exe", "e:\\Private\\eclipse-workspace\\NioContestSystem\\work", " ", programPath}, 
					inputData, true, true, 3000);
			if (runResult.exitCode != 0)
				return false;
			String expectedOutput = fileHelper.readAllAsString(dataSet.getOutputFileName());
			return runResult.stdOut != null && runResult.stdOut.equals(expectedOutput);
		}
		catch (TimeoutException e) {
			e.printStackTrace();
			return false;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
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
