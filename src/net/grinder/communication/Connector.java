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

package net.grinder.communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Connection factory.
 *
 * @author Philip Aston
 * @version $Revision$
 */
public final class Connector {

  private final String m_hostString;
  private final int m_port;
  private final ConnectionType m_connectionType;

  /**
   * Constructor.
   *
   * @param hostString TCP address to connect to.
   * @param port TCP port to connect to.
   * @param connectionType Connection type.
   */
  public Connector(String hostString, int port,
                   ConnectionType connectionType) {
    m_hostString = hostString;
    m_port = port;
    m_connectionType = connectionType;
  }

  /**
   * Factory method that makes a TCP connection and returns a
   * corresponding socket.
   *
   * @param hostString TCP address to connect to.
   * @param port TCP port to connect to.
   * @param connectionType The connection type.
   * @return A socket wired to the connection.
   * @throws CommunicationException If connection could not be
   * establish.
   */
  Socket connect() throws CommunicationException {

    final InetAddress inetAddress;

    try {
      inetAddress = InetAddress.getByName(m_hostString);
    }
    catch (UnknownHostException e) {
      throw new CommunicationException(
        "Could not resolve host '" + m_hostString + "'", e);
    }

    try {
      // Bind to any local port.
      final Socket socket = new Socket(inetAddress, m_port);

      m_connectionType.write(socket.getOutputStream());

      return socket;
    }
    catch (IOException e) {
      throw new CommunicationException(
        "Failed to connect to '" + inetAddress + ":" + m_port + "'", e);
    }
  }
}