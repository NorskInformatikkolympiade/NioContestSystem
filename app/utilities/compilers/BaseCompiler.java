package utilities.compilers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import utilities.cmd.ICommandLineExecutor;

public abstract class BaseCompiler {
  protected final long timeout;
  protected final ICommandLineExecutor commandLineExecutor;
  
  protected BaseCompiler(long timeout, ICommandLineExecutor commandLineExecutor) {
    this.timeout = timeout;
    this.commandLineExecutor = commandLineExecutor;
  }
  
  protected void writeToFile(String contents, String folder, String fileName) throws IOException {
    File parent = new File(folder);
    File file = new File(parent, fileName);
    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    out.write(contents);
    out.close();
  }
}
