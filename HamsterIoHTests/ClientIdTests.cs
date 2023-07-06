using NUnit.Framework.Internal;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace HamsterIoHTests
{
    [TestFixture]
    public class ClientIdTests : MqttTestBase
    {
        [Test]
        public void ClientId_MatchesHamsterId()
        {
            Assert.That(WaitForConnection.Result, Is.EqualTo(HamsterId.ToString()));
        }
    }
}
