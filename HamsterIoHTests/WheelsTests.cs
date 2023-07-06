using MQTTnet;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HamsterIoHTests
{
    [TestFixture]
    public class WheelsTests : MqttTestBase
    {
        [Test]
        public void Wheels_NoRevolutionWhileSleeping()
        {
            var incomingMessage = ExpectMessage(t => t == $"/pension/hamster/{HamsterId}/wheels");

            _client.Sleep();

            if (Task.WaitAny(incomingMessage, Task.Delay(3000)) == 0)
            {
                Assert.Fail("Received wheel revolution while hamster was sleeping");
            }
        }

        [Test]
        public void Wheels_HamsterMakesTurnsWhenRunning()
        {
            var incomingMessage = ExpectMessage(t => t == $"/pension/hamster/{HamsterId}/wheels");

            _client.Run();

            if (Task.WaitAny(incomingMessage, Task.Delay(1000)) == 1)
            {
                Assert.Fail("Hamster did not publish wheel revolutions while running");
            }

            if (!int.TryParse(incomingMessage.Result.ConvertPayloadToString(), out var currentRevolutions))
            {
                Assert.Fail("Wheel revolutions is not a number");
            }

            var secondMessage = ExpectMessage(t => t == $"/pension/hamster/{HamsterId}/wheels");

            if (Task.WaitAny(secondMessage, Task.Delay(1000)) == 1)
            {
                Assert.Fail("Hamster did not publish wheel revolutions while running");
            }

            if (!int.TryParse(secondMessage.Result.ConvertPayloadToString(), out var nextRevolutions))
            {
                Assert.Fail("Wheel revolutions is not a number");
            }

            Assert.That(nextRevolutions, Is.GreaterThan(currentRevolutions));
        }
    }
}
