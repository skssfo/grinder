// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000, 2001, 2002 Philip Aston
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

package net.grinder.console.swingui;

import junit.framework.TestCase;
import junit.swingui.TestRunner;
//import junit.textui.TestRunner;

import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import net.grinder.console.model.ModelListener;
import net.grinder.statistics.StatisticsView;


/**
 * @author Philip Aston
 * @version $Revision$
 */
public class TestSwingDispatchedModelListener extends TestCase
{
    public static void main(String[] args)
    {
	TestRunner.run(TestSwingDispatchedModelListener.class);
    }

    public TestSwingDispatchedModelListener(String name)
    {
	super(name);
    }

    private Runnable m_voidRunnable = new Runnable() { public void run() {} };

    public void testDispatch() throws Exception
    {
	final MyModelListener listener = new MyModelListener();

	final ModelListener swingDispatchedListener =
	    new SwingDispatchedModelListener(listener);

	listener.update();

	// Wait for a dummy event to be processed by the swing event
	// queue.
	SwingUtilities.invokeAndWait(m_voidRunnable);

	assertTrue(listener.m_updateCalled);

	final Set myTests = new HashSet();
	listener.reset(myTests);
	SwingUtilities.invokeAndWait(m_voidRunnable);
	assertTrue(listener.m_resetCalled);
	assertSame(myTests, listener.m_resetSet);

	final StatisticsView view1 = new StatisticsView();
	final StatisticsView view2 = new StatisticsView();
	listener.newStatisticsViews(view1, view2);
	SwingUtilities.invokeAndWait(m_voidRunnable);
	assertTrue(listener.m_updateCalled);
	assertSame(view1, listener.m_intervalStatisticsView);
	assertSame(view2, listener.m_cumulativeStatisticsView);
    }

    private class MyModelListener implements ModelListener
    {
	public boolean m_resetCalled = false;
	public Set m_resetSet;

	public boolean m_updateCalled = false;

	public boolean m_newStatisticsViewsCalled = false;
	public StatisticsView m_intervalStatisticsView;
	public StatisticsView m_cumulativeStatisticsView;
	
	public void reset(Set newTests) 
	{
	    m_resetCalled = true;
	    m_resetSet = newTests;
	}

	public void update()
	{
	    m_updateCalled = true;
	}

	public void newStatisticsViews(StatisticsView intervalStatisticsView,
				       StatisticsView cumulativeStatisticsView)
	{
	    m_newStatisticsViewsCalled = true;
	    m_intervalStatisticsView = intervalStatisticsView;
	    m_cumulativeStatisticsView = cumulativeStatisticsView;
	}
    }
}

