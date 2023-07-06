using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HamsterIoHTests
{
    public class HamsterMqttClient
    {
        public static string ClientExecutable { get; set; } = "java -jar ./build/libs/hamster_mqtt-0.0.1-SNAPSHOT.jar";

        private Process? _clientProcess;
        private TaskCompletionSource<string>? _clientResponse;

        private static string FindWorkingDirectory()
        {
            var dir = Path.GetFullPath(Environment.CurrentDirectory);
            while (dir != null && !Directory.Exists(Path.Combine(dir, "HamsterIoHTests")))
            {
                dir = Path.GetDirectoryName(dir);
            }
            return dir!;
        }


        public HamsterMqttClient(int hamsterId, string? options)
        {
            Environment.CurrentDirectory = FindWorkingDirectory();

            var commandString = $"{ClientExecutable} {hamsterId} -s {options}";
            var firstSpace = commandString.IndexOf(' ');
            var processStartInfo = new ProcessStartInfo
            {
                FileName = commandString.Substring(0, firstSpace),
                Arguments = commandString.Substring(firstSpace + 1),
                UseShellExecute = false,
                RedirectStandardInput = true,
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                WorkingDirectory = Environment.CurrentDirectory,
            };
            _clientProcess = Process.Start(processStartInfo);
        }

        public void ReadOutputs()
        {
            if (_clientProcess != null)
            {
                Task.Run(() => ReadStream(_clientProcess.StandardOutput));
                Task.Run(() => ReadStream(_clientProcess.StandardError));
            }
        }

        private void ReadStream(StreamReader reader)
        {
            string? line;
            do
            {
                line = reader.ReadLine();
                if (line != null)
                {
                    if (_clientResponse != null)
                    {
                        _clientResponse.TrySetResult(line);
                        _clientResponse = null;
                    }
                    Console.WriteLine(line);
                }
            } while (line != null);
        }

        public Task<string> ExpectResponse()
        {
            _clientResponse = new TaskCompletionSource<string>();
            return _clientResponse.Task;
        }

        public void SetPosition(string position)
        {
            Send($"move {position}");
        }

        public void Run()
        {
            Send("run");
        }

        public void Sleep()
        {
            Send("sleep");
        }

        public void Eat()
        {
            Send("eat");
        }
        
        public void Mate()
        {
            Send("mate");
        }

        private void Send(string message)
        {
            if (_clientProcess != null && !_clientProcess.HasExited)
            {
                _clientProcess.StandardInput.WriteLine(message);
            }
        }

        public void Terminate()
        {
            if (_clientProcess != null)
            {
                Send("quit");
                if (!_clientProcess.WaitForExit(2000))
                {
                    _clientProcess.Kill();
                }
                _clientProcess = null;
            }
        }
    }
}
