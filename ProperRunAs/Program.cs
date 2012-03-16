using System;
using System.Diagnostics;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Security;
using System.Threading;

namespace ProperRunAs
{
    public class Program
    {
        private static Process process;
        private const int TimeoutExitCode = -42;
        private const int ExceptionExitCode = -43;

        public static int Main(string[] args)
        {
            var password = new SecureString();
            foreach (char c in "Passw0rd")
                password.AppendChar(c);
            try
            {
                return Run(args, password);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                Console.Out.Flush();
                return ExceptionExitCode;
            }
            finally
            {
                if (process != null && !process.HasExited)
                    Kill(process.Id, password);
            }
        }
        
        private static int Run(string[] args, SecureString password)
        {
            string workingDirecory = args[0], runCommand = args[1], executable = args[2];
            int timeLimit = int.Parse(args[3]);
            
            string inputFileName = Path.Combine(workingDirecory, "input.txt");
            string outputFileName = Path.Combine(workingDirecory, "output.txt");
            //Note: Use ASCII, not UTF8, because the latter writes a byte order mark at the start of the file, which confuses C++ programs
            File.WriteAllText(inputFileName, Console.In.ReadToEnd(), Encoding.ASCII);
            File.WriteAllText(outputFileName, "");

            process = new Process {
                StartInfo = new ProcessStartInfo {
                    FileName = "cmd",
                    Arguments = string.Format("/c \"{0} {1} < {2} > {3}\"", runCommand, executable, inputFileName, outputFileName),
                    UserName = "NioSubmission",
                    Password = password,
                    RedirectStandardInput = true,
                    RedirectStandardOutput = true,
                    RedirectStandardError = true,
                    UseShellExecute = false,
                    CreateNoWindow = true,
                    WindowStyle = ProcessWindowStyle.Hidden,
                    ErrorDialog = false,
                    WorkingDirectory = workingDirecory,
                }
            };

            process.Start();
            process.WaitForExit(timeLimit);
            if (process.HasExited)
            {
                Console.Out.Write(File.ReadAllText(outputFileName, Encoding.ASCII));
                Console.Out.Flush();
                return process.ExitCode;
            }
            else
            {
                return TimeoutExitCode;
            }
        }

        private static void Kill(int processId, SecureString password)
        {
            try
            {
                var process = new Process {
                    StartInfo = new ProcessStartInfo {
                        FileName = "taskkill",
                        Arguments = "/PID " + processId,
                        UserName = "NioSubmission",
                        Password = password,
                        UseShellExecute = false,
                        CreateNoWindow = true,
                        WindowStyle = ProcessWindowStyle.Hidden,
                        ErrorDialog = false,
                        WorkingDirectory = @"c:\",
                    }
                };
                process.Start();
                process.WaitForExit();
            }
            catch (Exception) { }
        }
    }
}
