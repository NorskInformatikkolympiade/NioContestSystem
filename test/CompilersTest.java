import org.junit.*;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import play.db.jpa.Transactional;
import play.test.*;
import utilities.compilers.Compilers;
import viewmodels.ScoreboardEntry;
import models.*;
import models.compilers.CompileResult;
import models.compilers.CompileStatus;

public class CompilersTest extends FunctionalTest {
  private String readAll(BufferedReader input) throws IOException {
    String line = null;
    StringBuffer capture = new StringBuffer();
    while ((line = input.readLine()) != null) {
      capture.append(line);
      capture.append("\n");
    }
    return capture.toString();
  }
  
  @Test
  /**
   * TODO(geir): Test won't work if /tmp does not exist
   * @throws IOException
   */
  public void compileCpp() throws IOException {
    File testProgram = new File("test", "test.cpp");
    assertTrue(testProgram.exists());
    BufferedReader br = new BufferedReader(new FileReader(testProgram));
    String program = readAll(br);
    CompileResult res = Compilers.compile(Language.CPP, program, "/tmp", "test");
    assertEquals(CompileStatus.OK, res.status);
    System.out.println("Compile took: " + res.duration + "ms");
  }
  
  @Test
  /**
   * TODO(geir): Test won't work if /tmp does not exist
   * @throws IOException
   */
  public void compileCppWhiteSpace() throws IOException {
    File testProgram = new File("test", "test.cpp");
    assertTrue(testProgram.exists());
    BufferedReader br = new BufferedReader(new FileReader(testProgram));
    String program = readAll(br);
    CompileResult res = Compilers.compile(Language.CPP, program, "/tmp/", "the awesome executable");
    assertEquals(CompileStatus.OK, res.status);
    System.out.println("Compile took: " + res.duration + "ms");
  }
}
