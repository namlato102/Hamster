using MQTTnet;
using MQTTnet.Server;
using System.Net;
using System.Net.Security;
using System.Runtime.InteropServices;
using System.Security.Cryptography.X509Certificates;

var mqttFactory = new MqttFactory();

var options = mqttFactory.CreateServerOptionsBuilder()
                .WithDefaultEndpoint()
                .WithDefaultEndpointBoundIPAddress(IPAddress.Loopback)
                .WithClientCertificate(CheckClientCertificate)
                .WithEncryptedEndpoint()
                .WithEncryptedEndpointBoundIPAddress(IPAddress.Loopback)
                .WithEncryptionCertificate(GetServerCertificate())
                .WithConnectionBacklog(100)
                .Build();



bool CheckClientCertificate(object sender, X509Certificate? certificate, X509Chain? chain, SslPolicyErrors sslPolicyErrors)
{
    if (certificate == null)
    {
        return true;
    }

    if (chain != null)
    {
        return chain.ChainStatus.All(status =>
            status.Status == X509ChainStatusFlags.NoError
            || status.Status == X509ChainStatusFlags.UntrustedRoot);
    }

    return false;
}


X509Certificate2 GetServerCertificate()
{
    var dir = Path.GetFullPath(Environment.CurrentDirectory);
    while (dir != null && !Directory.Exists(Path.Combine(dir!, "certs")))
    {
        dir = Path.GetDirectoryName(dir);
    }

    if (dir == null)
    {
        throw new InvalidOperationException("Certificate directory not found, are you running in the correct folder?");
    }

    return new X509Certificate2(Path.Combine(dir, "certs", "server.pfx"));
}


Task OnClientConnected(ClientConnectedEventArgs arg)
{
    Console.WriteLine($"Client {arg.ClientId} connected");
    return Task.CompletedTask;
}


Task OnClientDisconnected(ClientDisconnectedEventArgs arg)
{
    Console.WriteLine($"Client {arg.ClientId} disconnected because {arg.DisconnectType}");
    return Task.CompletedTask;
}


Task OnClientSubscribed(ClientSubscribedTopicEventArgs arg)
{
    Console.WriteLine($"Client {arg.ClientId} subscribed {arg.TopicFilter.Topic}");
    return Task.CompletedTask;
}


Task OnMessagePublished(ApplicationMessageNotConsumedEventArgs arg)
{
    Console.WriteLine($"{arg.SenderId} published on topic {arg.ApplicationMessage.Topic}: {arg.ApplicationMessage.ConvertPayloadToString()}");
    return Task.CompletedTask;
}

var server = mqttFactory.CreateMqttServer(options);
server.StartAsync().Wait();
server.ClientConnectedAsync += OnClientConnected;
server.ClientDisconnectedAsync += OnClientDisconnected;
server.ClientSubscribedTopicAsync += OnClientSubscribed;
server.ApplicationMessageNotConsumedAsync += OnMessagePublished;

var tcs = new TaskCompletionSource<bool>();
var sigintReceived = false;
Console.CancelKeyPress += (_, ea) =>
{
    ea.Cancel = true;
    tcs.SetResult(true);
    sigintReceived = true;
};
AppDomain.CurrentDomain.ProcessExit += (_, _) =>
{
    if (!sigintReceived)
    {
        tcs.SetResult(false);
    }
};
tcs.Task.Wait();