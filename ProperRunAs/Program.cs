using System;
using System.Diagnostics;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Security;

namespace ProperRunAs
{
    public class Program
    {
        public static int Main(string[] args)
        {
            string workingDirecory = args[0], runCommand = args[1], executable = args[2];
            string inputFileName = Path.Combine(workingDirecory, "input.txt");
            string outputFileName = Path.Combine(workingDirecory, "output.txt");
            //Note: Use ASCII, not UTF8, because the latter writes a byte order mark at the start of the file, which confuses C++ programs
            File.WriteAllText(inputFileName, Console.In.ReadToEnd(), Encoding.ASCII);
            File.WriteAllText(outputFileName, "");

            var password = new SecureString();
            foreach (char c in "...")
                password.AppendChar(c);

            var process = new Process {
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
            process.WaitForExit();
            Console.Out.Write(File.ReadAllText(outputFileName, Encoding.ASCII));
            Console.Out.Flush();
            return process.ExitCode;
        }
    }
}
