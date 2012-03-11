package utilities.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.concurrent.TimeoutException;

import models.cmd.CommandLineResult;

public class CommandLineExecutor implements ICommandLineExecutor {
	public CommandLineResult execute(final String[] commandLine,
									 final boolean captureOutput, 
									 final boolean captureError,
									 final long timeout) 
										throws IOException, InterruptedException, TimeoutException {
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(commandLine);
		String stdErr = null, stdOut = null;
		
		StringBuffer stdOutCapture = new StringBuffer();
		StringBuffer stdErrCapture = new StringBuffer();
		if (captureOutput)
			new InputStreamHandler(stdOutCapture, process.getInputStream());
		if (captureError)
			new InputStreamHandler(stdErrCapture, process.getErrorStream());
		
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout); // Wait <= this many ms for worker thread to finish
			if (worker.exit == null) {
				throw new TimeoutException();
			}
		}
		catch (InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		}
		finally {
			process.destroy(); // Clean up the running command line process
		}
		stdOut = stdOutCapture.toString();
		stdErr = stdErrCapture.toString();
		return new CommandLineResult(worker.exit, stdOut, stdErr, worker.duration);
	}
	
	private static class Worker extends Thread {
		private final Process process;
		private Integer exit;
		private Long duration;
		
		private Worker(Process process) {
			this.process = process;
		}
		
		public void run() {
			long startTime = System.nanoTime();
			try {
				exit = process.waitFor();
				duration = (System.nanoTime() - startTime) / 1000000; // Realistically, we don't have nanosecond accuracy.
			}
			catch (InterruptedException ignore) {
				return;
			}
		}
	}

	private static class InputStreamHandler extends Thread {
		private InputStream stream;
		private StringBuffer capture;
		
		public InputStreamHandler(StringBuffer capture, InputStream stream) {
			this.capture = capture;
			this.stream = stream;
			start();
		}
		
		public void run() {
			try {
				BufferedReader input = new BufferedReader(new InputStreamReader(stream));
				String line = null;
				while ((line = input.readLine()) != null) {
					capture.append(line);
					capture.append("\n");
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
}
