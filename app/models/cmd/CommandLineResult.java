package models.cmd;

public class CommandLineResult {
  public String stdOut;
  public String stdErr;
  public int exitCode;
  public long duration;
  
  public CommandLineResult(int exitCode, String stdOut, String stdErr,
                           long duration) {
    this.exitCode = exitCode;
    this.stdOut = stdOut;
    this.stdErr = stdErr;
    this.duration = duration;
  }
}
