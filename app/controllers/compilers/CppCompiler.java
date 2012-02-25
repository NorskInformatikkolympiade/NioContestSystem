package controllers.compilers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

import controllers.cmd.CommandLineExecutor;

import models.Language;
import models.cmd.CommandLineResult;
import models.compilers.CompileResult;
import models.compilers.CompileStatus;

public class CppCompiler extends BaseCompiler implements Compiler {
  public CppCompiler(long timeout) {
    super(timeout);
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
    CommandLineResult result = CommandLineExecutor.execute(commandLine, true, true, timeout);
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
