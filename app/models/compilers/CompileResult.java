package models.compilers;

public class CompileResult {
  public CompileStatus status;
  public String stdOut;
  public String stdErr;
  public long duration;  // in msec
  
  public CompileResult(CompileStatus status,
                       String stdOut,
                       String stdErr,
                       long duration) {
    this.status = status;
    this.stdOut = stdOut;
    this.stdErr = stdErr;
    this.duration = duration;
  }
}
