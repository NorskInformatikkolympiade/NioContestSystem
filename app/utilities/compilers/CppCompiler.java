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

public class CppCompiler extends BaseCompiler implements Compiler {
	// For testing
	CppCompiler(long timeout, ICommandLineExecutor commandLineExecutor) {
		super(timeout, commandLineExecutor);
	}

	public CppCompiler(long timeout) {
		this(timeout, new CommandLineExecutor());
	}

	@Override
	public CompileResult compile(String source, String folder, String fileName) throws IOException, InterruptedException, TimeoutException {
		String srcFileName = fileName + ".cpp";
		writeToFile(source, folder, srcFileName);

		File dir = new File(folder);
		File srcFile = new File(dir, srcFileName);
		File execFile = new File(dir, fileName);
		String[] commandLine = { "g++", srcFile.getAbsolutePath(), "-o", execFile.getAbsolutePath() };

		CompileStatus status;
		CommandLineResult result = commandLineExecutor.execute(commandLine, new byte[0], true, true, timeout);
		if (result.exitCode == 0) {
			status = CompileStatus.OK;
		} else {
			status = CompileStatus.CODE_ERROR;
		}
		return new CompileResult(status, result.stdOut, result.stdErr, result.duration);
	}

	@Override
	public Language getLanguage() {
		return Language.CPP;
	}
}
