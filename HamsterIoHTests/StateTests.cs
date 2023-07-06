using MQTTnet;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HamsterIoHTests
{
    [TestFixture]
    public class StateTests : MqttTestBase
    {
        [Test]
        public void Eat_PublishesResult()
        {
            var incomingMessage = ExpectMessage(t => t == $"/pension/hamster/{HamsterId}/state");

            _client.Eat();

            ExpectStateUpdate(incomingMessage, "EATING");
        }

        private static void ExpectStateUpdate(Task<MqttApplicationMessage> incomingMessage, string expectedState)
        {
            if (Task.WaitAny(incomingMessage, Task.Delay(3000)) != 0)
            {
                Assert.Fail("Message was not published in a timely manner");
            }

            Assert.That(incomingMessage.Result.ConvertPayloadToString(), Is.EqualTo(expectedState));
        }

        [Test]
        public void Sleep_PublishesResult()
        {
            var incomingMessage = ExpectMessage(t => t == $"/pension/hamster/{HamsterId}/state");

            _client.Sleep();

            ExpectStateUpdate(incomingMessage, "SLEEPING");
        }

        [Test]
        public void Run_PublishesResult()
        {
            var incomingMessage = ExpectMessage(t => t == $"/pension/hamster/{HamsterId}/state");

            _client.Run();

            ExpectStateUpdate(incomingMessage, "RUNNING");
        }

        [Test]
        public void Mate_PublishesResult()
        {
            var incomingMessage = ExpectMessage(t => t == $"/pension/hamster/{HamsterId}/state");

            _client.Mate();

            ExpectStateUpdate(incomingMessage, "MATEING");
        }
    }
}
