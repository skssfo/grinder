// Copyright (C) 2002, 2003, 2004 Philip Aston
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

package net.grinder.engine.process;

import net.grinder.common.GrinderProperties;
import net.grinder.communication.Message;
import net.grinder.communication.QueuedSender;


/**
 * Stubbed ProcessContext. Allows the unit tests to
 * bypass package interface.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public class StubProcessContext extends ProcessContext {

  private static StubProcessContext s_processContext;

  public static StubProcessContext get() throws Exception {
    if (s_processContext == null) {
      final GrinderProperties properties = new GrinderProperties();
      properties.setBoolean("grinder.useConsole", false);

      s_processContext = new StubProcessContext(properties);
    }

    return s_processContext;
  }
    
  public StubProcessContext(GrinderProperties properties) throws Exception {

    super("Unit Test", properties,
          new LoggerImplementation("GrinderID", ".", true, 1),
          new QueuedSender() {
            public void send(Message message) { }
            public void flush() { }
            public void queue(Message message) { }
            public void shutdown() { }
          });
  }
}
