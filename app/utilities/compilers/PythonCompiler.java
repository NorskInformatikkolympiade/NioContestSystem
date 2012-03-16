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

public class PythonCompiler extends BaseCompiler implements Compiler {
	// For testing
	PythonCompiler(long timeout, ICommandLineExecutor commandLineExecutor) {
		super(timeout, commandLineExecutor);
	}

	public PythonCompiler(long timeout) {
		this(timeout, new CommandLineExecutor());
	}

	@Override
	public CompileResult compile(String source, String folder, String fileName) throws IOException, InterruptedException, TimeoutException {
		writeToFile(source, folder, fileName);
		return new CompileResult(CompileStatus.OK, "", "", 0, fileName);
	}
	
	@Override
	public Language getLanguage() {
		return Language.PYTHON;
	}
}
