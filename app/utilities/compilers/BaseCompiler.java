package utilities.compilers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class BaseCompiler {
  protected long timeout;
  
  protected BaseCompiler(long timeout) {
    this.timeout = timeout;
  }
  
  protected void writeToFile(String contents, String folder, String fileName) throws IOException {
    File parent = new File(folder);
    File file = new File(parent, fileName);
    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    out.write(contents);
    out.close();
  }
}
