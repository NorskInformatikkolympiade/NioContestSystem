package utilities.compilers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import models.Language;
import models.compilers.CompileResult;
import models.compilers.CompileStatus;

/**
 * Controls the invocation of various source code compilers. It makes sure only
 * one compilation is running at any given time. This is probably enough for our
 * purposes.
 * 
 * Adding compilers for new languages is done by implementing the Compiler
 * interface and registering in the Compilers constructor.
 * 
 * @author geir
 *
 */
public class Compilers implements ICompilers {
  private static final long TIME_LIMIT = 10000;  // 10 seconds
  private static Compilers instance = null;
  private ArrayList<Compiler> compilers;
  
  private Compilers() {  // Disallow evil constructors
    compilers = new ArrayList<Compiler>();
    compilers.add(new CppCompiler(TIME_LIMIT));
//    compilers.add(new CCompiler());
//    compilers.add(new JavaCompiler());
  }
  
  /**
   * Compiles the given string of source code using a compiler appropriate for
   * the given language.
   * @param language the code should be compiled with
   * @param source code that should be compiled
   * @param folder to output file to
   * @param fileName name of the compiled executable
   * @return the result of the compilation
   */
  public static synchronized Compilers instance() {
    if (instance == null) {
      instance = new Compilers();
    }
    return instance;
  }
  
  public CompileResult compile(Language language, String source, String folder, String fileName) {
    for (Compiler compiler : compilers) {
      if (compiler.getLanguage() == language) {
        try {
          return compiler.compile(source, folder, fileName);
        } catch (IOException e) {
          e.printStackTrace();
          return new CompileResult(CompileStatus.INTERNAL_ERROR, null, null, -1);
        } catch (InterruptedException e) {
          e.printStackTrace();
          return new CompileResult(CompileStatus.INTERNAL_ERROR, null, null, -1);
        } catch (TimeoutException e) {
          e.printStackTrace();
          return new CompileResult(CompileStatus.TIMEOUT, null, null, -1);
        }
      }
    }
    return null;
  }
}
