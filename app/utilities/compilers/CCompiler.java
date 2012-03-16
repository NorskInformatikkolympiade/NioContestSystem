package utilities.compilers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

import utilities.cmd.CommandLineExecutor;
import utilities.cmd.ICommandLineExecutor;

import models.Language;
import models.cmd.CommandLineResult;
import models.compilers.CompileResult;
import models.compilers.CompileStatus;

public class CCompiler extends BaseCompiler implements Compiler {
	// For testing
	CCompiler(long timeout, ICommandLineExecutor commandLineExecutor) {
		super(timeout, commandLineExecutor);
	}

	public CCompiler(long timeout) {
		this(timeout, new CommandLineExecutor());
	}

	@Override
	public CompileResult compile(String source, String folder, String fileName) throws IOException, InterruptedException, TimeoutException {
		String srcFileName = fileName + ".c";
		writeToFile(source, folder, srcFileName);

		File dir = new File(folder);
		File srcFile = new File(dir, srcFileName);
		File execFile = new File(dir, "Program.exe");
		String[] commandLine = { "gcc", srcFile.getAbsolutePath(), "-o", execFile.getAbsolutePath() };

		CompileStatus status;
		CommandLineResult result = commandLineExecutor.execute(commandLine, new byte[0], true, true, timeout);
		if (result.exitCode == 0) {
			status = CompileStatus.OK;
		} else {
			status = CompileStatus.CODE_ERROR;
		}
		return new CompileResult(status, result.stdOut, result.stdErr, result.duration, execFile.getName());
	}

	@Override
	public Language getLanguage() {
		return Language.C;
	}
}
