using Microsoft.VisualStudio.TestPlatform.Common.Utilities;
using MQTTnet;
using MQTTnet.Protocol;
using MQTTnet.Server;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace HamsterIoHTests
{
    public class MqttTestBase
    {
        protected readonly MqttFactory MqttFactory = new MqttFactory();

        private static int _nextHamsterId = 9000;
        private readonly int _hamsterId;

        protected MqttServer _server;
        protected HamsterMqttClient _client;

        private readonly List<(Predicate<string>, TaskCompletionSource<MqttApplicationMessage>)> _expectedMessages = new List<(Predicate<string>, TaskCompletionSource<MqttApplicationMessage>)>();
        private readonly List<(string, TaskCompletionSource<string>)> _expectedSubscriptions = new List<(string, TaskCompletionSource<string>)>();
        private readonly List<string> _subscribedTopics = new List<string>();
        private TaskCompletionSource<string> _waitForConnection = new TaskCompletionSource<string>();
        private bool _connected;
        private MqttClientDisconnectType _disconnectType;

        protected bool IsClientConnected => _connected;

        protected Task<string> WaitForConnection => _waitForConnection.Task;

        protected MqttClientDisconnectType DisconnectType => _disconnectType;

        public int HamsterId => _hamsterId;

        protected MqttTestBase()
        {
            _hamsterId = _nextHamsterId++;
        }

        protected virtual MqttServerOptions CreateServerOptions()
        {
            return MqttFactory.CreateServerOptionsBuilder()
                .WithDefaultEndpoint()
                .WithDefaultEndpointBoundIPAddress(IPAddress.Loopback)
                .WithConnectionBacklog(50)
                .Build();
        }

        protected virtual void BeforeClientStarts() { }

        protected virtual string? CreateClientOptions()
        {
            return null;
        }

        public void StartServer()
        {
            _server = MqttFactory.CreateMqttServer(CreateServerOptions());
            _server.StartAsync().Wait();
            _server.ClientConnectedAsync += OnClientConnected;
            _server.ClientDisconnectedAsync += OnClientDisconnected;
            _server.ClientSubscribedTopicAsync += OnClientSubscribed;
            _server.ApplicationMessageNotConsumedAsync += OnMessagePublished;
        }

        private Task OnMessagePublished(ApplicationMessageNotConsumedEventArgs arg)
        {
            return Task.Run(() =>
            {
                lock (_expectedMessages)
                {
                    for (int i = _expectedMessages.Count - 1; i >= 0; i--)
                    {
                        if (_expectedMessages[i].Item1(arg.ApplicationMessage.Topic))
                        {
                            _expectedMessages[i].Item2.TrySetResult(arg.ApplicationMessage);
                            _expectedMessages.RemoveAt(i);
                        }
                    }
                }
            });
        }

        private Task OnClientSubscribed(ClientSubscribedTopicEventArgs arg)
        {
            return Task.Run(() =>
            {
                lock (_subscribedTopics)
                {
                    _subscribedTopics.Add(arg.TopicFilter.Topic);
                }
                lock(_expectedSubscriptions)
                {
                    for (int i = _expectedSubscriptions.Count - 1; i >= 0; i--)
                    {
                        if (_expectedSubscriptions[i].Item1 == arg.TopicFilter.Topic)
                        {
                            _expectedSubscriptions[i].Item2.TrySetResult(arg.ClientId);
                            _expectedSubscriptions.RemoveAt(i);
                        }
                    }
                }
            });
        }

        private Task OnClientDisconnected(ClientDisconnectedEventArgs arg)
        {
            _connected = false;
            _disconnectType = arg.DisconnectType;
            return Task.CompletedTask;
        }

        private Task OnClientConnected(ClientConnectedEventArgs arg)
        {
            _connected = true;
            _waitForConnection.SetResult(arg.ClientId);
            return Task.CompletedTask;
        }

        public void StartClient()
        {
            _client = new HamsterMqttClient(_hamsterId, CreateClientOptions());
        }


        public Task<MqttApplicationMessage> ExpectMessage(Predicate<string> topicFilter)
        {
            var tcs = new TaskCompletionSource<MqttApplicationMessage>();
            lock (_expectedMessages)
            {
                _expectedMessages.Add((topicFilter, tcs));
            }
            return tcs.Task;
        }

        public Task<string> ExpectSubscription(string topic)
        {
            lock (_subscribedTopics)
            {
                if (_subscribedTopics.Contains(topic))
                {
                    return Task.FromResult(string.Empty);
                }
            }
            var tcs = new TaskCompletionSource<string>();
            lock (_expectedSubscriptions)
            {
                _expectedSubscriptions.Add((topic, tcs));
            }
            return tcs.Task;
        }


        [SetUp]
        public void Setup()
        {
            _subscribedTopics.Clear();
            _expectedSubscriptions.Clear();
            _expectedMessages.Clear();

            StartServer();
            BeforeClientStarts();
            StartClient();
            try
            {
                using (var cs = new CancellationTokenSource(5000))
                {
                    WaitForConnection.Wait(cs.Token);
                }
            }
            catch (OperationCanceledException)
            {
                Assert.Fail("The client did not connect successfully");
            }
            finally
            {
                _client.ReadOutputs();
            }            
        }

        [TearDown]
        public void TearDown()
        {
            if (_client != null)
            {
                _client.Terminate();
            }
            if (_server != null)
            {
                _server.StopAsync().Wait();
            }
        }
    }
}
