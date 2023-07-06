using MQTTnet;

namespace HamsterIoHTests
{
    [TestFixture]
    public class LivestockTests : MqttTestBase
    {
        private Task<MqttApplicationMessage>? _livestockMessage;

        protected override void BeforeClientStarts()
        {
            _livestockMessage = ExpectMessage(t => t == $"/pension/livestock");
        }

        [Test]
        public void HamsterPublishedLivestockMessage()
        {
            if (Task.WaitAny(_livestockMessage!, Task.Delay(3000)) != 0)
            {
                Assert.Fail("Did not see livestock published within 3s");
            }

            var message = _livestockMessage!.Result;
            Assert.That(message.ConvertPayloadToString(), Is.EqualTo(HamsterId.ToString()));
            
        }
    }
}