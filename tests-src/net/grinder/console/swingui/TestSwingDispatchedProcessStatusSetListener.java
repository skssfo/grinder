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

import javax.swing.SwingUtilities;

import net.grinder.common.ProcessStatus;
import net.grinder.console.model.ProcessStatusSetListener;


/**
 * @author Philip Aston
 * @version $Revision$
 */
public class TestSwingDispatchedProcessStatusSetListener extends TestCase
{
    public static void main(String[] args)
    {
	TestRunner.run(TestSwingDispatchedProcessStatusSetListener.class);
    }

    public TestSwingDispatchedProcessStatusSetListener(String name)
    {
	super(name);
    }

    private Runnable m_voidRunnable = new Runnable() { public void run() {} };

    public void testDispatch() throws Exception
    {
	final MyProcessStatusSetListener listener =
	    new MyProcessStatusSetListener();

	final ProcessStatusSetListener swingDispatchedListener =
	    new SwingDispatchedProcessStatusSetListener(listener);

	final ProcessStatus[] data = new ProcessStatus[0];
	final int running = 1;
	final int total = 2;

	listener.update(data, running, total);

	// Wait for a dummy event to be processed by the swing event
	// queue.
	SwingUtilities.invokeAndWait(m_voidRunnable);

	assertTrue(listener.m_updateCalled);
	assertEquals(data, listener.m_updateData);
	assertEquals(running, listener.m_updateRunning);
	assertEquals(total, listener.m_updateTotal);
    }

    private class MyProcessStatusSetListener
	implements ProcessStatusSetListener
    {
	public boolean m_updateCalled = false;
	public ProcessStatus[] m_updateData;
	public int m_updateRunning;
	public int m_updateTotal;

	public void update(ProcessStatus[] data, int running, int total)
	{
	    m_updateCalled = true;
	    m_updateData = data;
	    m_updateRunning = running;
	    m_updateTotal = total;
	}
    }
}

