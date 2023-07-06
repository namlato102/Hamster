using MQTTnet.Server;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HamsterIoHTests
{
    [TestFixture]
    public class CleanDisconnectTests : MqttTestBase
    {
        [Test]
        public void Quit_PerformsCleanDisconnect()
        {
            _client.Terminate();
            Assert.That(DisconnectType, Is.EqualTo(MqttClientDisconnectType.Clean));
        }
    }
}
