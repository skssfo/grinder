// Copyright (C) 2000, 2001, 2002, 2003, 2004 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.console.messages;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import net.grinder.communication.Message;
import net.grinder.statistics.ExpressionView;
import net.grinder.statistics.StatisticsView;
import net.grinder.statistics.TestStatisticsMap;
import net.grinder.testutility.Serializer;


/**
 *  Unit test case for console messages.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public class TestConsoleMessages extends TestCase {

  private static Random s_random = new Random();

  private static Message serialise(Message original) throws Exception {
    return (Message) Serializer.serialize(original);
  }

  public void testRegisterStatisticsViewMessage() throws Exception {

    final StatisticsView statisticsView = new StatisticsView();
    statisticsView.add(new ExpressionView("One", "blah", "userLong0"));

    final RegisterStatisticsViewMessage original =
      new RegisterStatisticsViewMessage(statisticsView);

    assertEquals(1, original.getStatisticsView().getExpressionViews().length);

    final RegisterStatisticsViewMessage received =
      (RegisterStatisticsViewMessage) serialise(original);

    assertEquals(1, received.getStatisticsView().getExpressionViews().length);
    assertEquals(original.getStatisticsView().getExpressionViews()[0],
                 received.getStatisticsView().getExpressionViews()[0]);
  }

  public void testRegisterTestsMessage() throws Exception {

    final Collection c = new HashSet();

    final RegisterTestsMessage original = new RegisterTestsMessage(c);

    assertEquals(c, original.getTests());

    final RegisterTestsMessage received =
      (RegisterTestsMessage) serialise(original);

    assertEquals(original.getTests(), received.getTests());
  }

  public void testReportStatisticsMessage() throws Exception {

    final TestStatisticsMap statisticsDelta = new TestStatisticsMap();

    final ReportStatisticsMessage original =
      new ReportStatisticsMessage(statisticsDelta);

    assertEquals(statisticsDelta, original.getStatisticsDelta());

    final ReportStatisticsMessage received =
      (ReportStatisticsMessage) serialise(original);

    assertEquals(original.getStatisticsDelta(), received.getStatisticsDelta());
  }

  public void testReportStatusMessage() throws Exception {

    final String uniqueID = "ID" + s_random.nextInt();

    final ReportStatusMessage original =
      new ReportStatusMessage(uniqueID, "test", (short)1, (short)2, (short)3);

    assertEquals(uniqueID, original.getIdentity());
    assertEquals("test", original.getName());
    assertEquals(1, original.getState());
    assertEquals(2, original.getNumberOfRunningThreads());
    assertEquals(3, original.getTotalNumberOfThreads());

    final ReportStatusMessage received =
      (ReportStatusMessage) serialise(original);

    assertEquals(uniqueID, received.getIdentity());
    assertEquals("test", received.getName());
    assertEquals(1, received.getState());
    assertEquals(2, received.getNumberOfRunningThreads());
    assertEquals(3, received.getTotalNumberOfThreads());
  }
}