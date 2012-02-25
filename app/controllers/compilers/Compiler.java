package controllers.compilers;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import models.Language;
import models.compilers.CompileResult;

public interface Compiler {
  public CompileResult compile(String source, String folder, String fileName) throws IOException, InterruptedException, TimeoutException;
  public Language getLanguage();
}
