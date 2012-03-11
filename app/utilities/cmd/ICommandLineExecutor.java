package utilities.cmd;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import models.cmd.CommandLineResult;

public interface ICommandLineExecutor {
	CommandLineResult execute(String[] commandLine, boolean captureOutput, boolean captureError, long timeout) throws IOException, InterruptedException, TimeoutException;
}
