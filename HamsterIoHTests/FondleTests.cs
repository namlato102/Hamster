using MQTTnet;
using MQTTnet.Server;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HamsterIoHTests
{
    [TestFixture]
    public class FondleTests : MqttTestBase
    {
        [TestCase(1)]
        [TestCase(2)]
        [TestCase(42)]
        public void Fondle_CausesLogInClient(int amount)
        {
            byte[] bytes = GetBytesForInt32(amount);

            var topic = $"/pension/hamster/{HamsterId}/fondle";

            if (Task.WaitAny(ExpectSubscription(topic), Task.Delay(3000)) != 0)
            {
                Assert.Fail("Client did not subscribe to topic");
            }

            var waitForLog = _client.ExpectResponse();
            _server.InjectApplicationMessage(
                new InjectedMqttApplicationMessage(new MqttApplicationMessageBuilder()
                    .WithTopic(topic)
                    .WithPayload(bytes)
                    .Build()));

            var expected = $"{amount} fondles received for Hamster {HamsterId}";

            if (Task.WaitAny(waitForLog, Task.Delay(5000)) != 0)
            {
                Console.WriteLine("Failed to detect message, expected " + expected);
                Assert.Fail("Client did not receive message, expected " + expected);
            }

            StringAssert.EndsWith(expected, waitForLog.Result);
        }

        private static byte[] GetBytesForInt32(int amount)
        {
            var bytes = BitConverter.GetBytes(amount);
            if (BitConverter.IsLittleEndian)
            {
                Array.Reverse(bytes);
            }

            return bytes;
        }

        [Test]
        public void Fondle_DifferentHamster_IsIgnored()
        {
            var bytes = GetBytesForInt32(1);

            var topic = $"/pension/hamster/{HamsterId}/fondle";

            if (Task.WaitAny(ExpectSubscription(topic), Task.Delay(3000)) != 0)
            {
                Assert.Fail("Client did not subscribe to topic");
            }

            var waitForLog = _client.ExpectResponse();
            _server.InjectApplicationMessage(
                new InjectedMqttApplicationMessage(new MqttApplicationMessageBuilder()
                    .WithTopic($"/pension/hamster/{HamsterId+1}/fondle")
                    .WithPayload(bytes)
                    .Build()));

            if (Task.WaitAny(waitForLog, Task.Delay(1000)) == 0)
            {
                Assert.Fail("Client caught message for different hamster");
            }
        }

        [Test]
        public void Fondle_RepeatedFondles_AllPublished()
        {
            byte[] bytes = GetBytesForInt32(1);

            var topic = $"/pension/hamster/{HamsterId}/fondle";

            if (Task.WaitAny(ExpectSubscription(topic), Task.Delay(3000)) != 0)
            {
                Assert.Fail("Client did not subscribe to topic");
            }

            for (int i = 0; i < 10; i++)
            {
                var waitForLog = _client.ExpectResponse();
                _server.InjectApplicationMessage(
                    new InjectedMqttApplicationMessage(new MqttApplicationMessageBuilder()
                        .WithTopic(topic)
                        .WithPayload(bytes)
                        .Build()));

                var expected = $"{1} fondles received for Hamster {HamsterId}";

                if (Task.WaitAny(waitForLog, Task.Delay(5000)) != 0)
                {
                    Console.WriteLine("Failed to detect message, expected " + expected);
                    Assert.Fail("Client did not receive message, expected " + expected);
                }

                StringAssert.EndsWith(expected, waitForLog.Result);
            }
        }
    }
}
