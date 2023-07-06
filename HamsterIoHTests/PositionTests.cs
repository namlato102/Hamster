using MQTTnet;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HamsterIoHTests
{
    [TestFixture]
    public class PositionTests : MqttTestBase
    {
        [TestCase("A")]
        [TestCase("B")]
        [TestCase("C")]
        [TestCase("D")]
        public void PositionChange_Works(string position)
        {
            var incomingMessage = ExpectMessage(t => t == $"/pension/hamster/{HamsterId}/position");

            _client.SetPosition(position);

            if (Task.WaitAny(incomingMessage, Task.Delay(3000)) != 0)
            {
                Assert.Fail("Message was not published in a timely manner");
            }

            Assert.That(incomingMessage.Result.ConvertPayloadToString(), Is.EqualTo(position));
        }

        [Test]
        public void PositionChange_MultipleChanges_Works()
        {
            foreach (var position in new[] { "A", "B", "C", "D" })
            {
                PositionChange_Works(position);
            }
        }
    }
}
