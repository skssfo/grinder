from net.grinder.plugin.http import HTTPTest
from random import random

logger = grinder.getLogger()

logger.logMessage("Hello from Grinder Thread " +
                  grinder.getGrinderID() + "-" +
                  `grinder.getThreadID()`)

httpTest = HTTPTest(999, "My test", "http://localhost:9001")
httpTest.invoke()

moreTests = [
    HTTPTest(1, "", "http://localhost:9001/security"),
    HTTPTest(2, "", "http://localhost:9001/security/welcome.jsp"),
    ]

for test in moreTests:
    logger.logMessage(`test`)

result = moreTests[0].invoke();

if result.isSuccessful():
    print("The test worked")
else:
    print("The test failed")

for x in range(10):
    if random() > 0.8:
        tests[0].invoke()
    else:
        tests[1].invoke()
