using MQTTnet.Server;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;
using System.Text;
using System.Threading.Tasks;

namespace HamsterIoHTests
{
    [TestFixture]
    public class ClientCertificateTests : CertificateTests
    {
        private string? _subjectFromCertificate;

        protected override string? CreateClientOptions()
        {
            return $"-e -c";
        }

        protected override MqttServerOptions CreateServerOptions()
        {
            return MqttFactory.CreateServerOptionsBuilder()
                .WithEncryptedEndpoint()
                .WithEncryptionCertificate(GetServerCertificate())
                .WithClientCertificate(ValidateCertificate)
                .WithConnectionBacklog(50)
                .Build();
        }

        private bool ValidateCertificate(object sender, X509Certificate? certificate, X509Chain? chain, SslPolicyErrors sslPolicyErrors)
        {
            if (certificate == null)
            {
                Console.Error.WriteLine("No certificate received. Please make sure you are sending the client certificates.");
                return false;
            }

            _subjectFromCertificate = certificate?.Subject;

            if (chain != null)
            {
                var result = chain.ChainStatus.All(status =>
                    status.Status == X509ChainStatusFlags.NoError
                    || status.Status == X509ChainStatusFlags.UntrustedRoot);

                if (!result)
                {
                    Console.Error.WriteLine(string.Join(", ", chain.ChainStatus.Select(status => status.StatusInformation)));
                }
                return true;
            }

            return false;
        }

        [Test]
        public void ClientId_MatchesCertificate()
        {
            Assert.That(_subjectFromCertificate!.Contains($"CN={WaitForConnection.Result}"));
        }
    }
}
