// Copyright (C) 2003, 2004 Philip Aston
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

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.grinder.console.model.editor.Buffer;


/**
 * TreeModel that walks file system.
 *
 * @author Philip Aston
 * @version $Revision$
 */
final class FileTreeModel implements TreeModel {

  private RootNode m_rootNode;
  private final EventListenerList m_listeners = new EventListenerList();

  FileTreeModel() {
    m_rootNode = null;
  }

  public void setRootDirectory(File rootDirectory) {
    m_rootNode = new RootNode(rootDirectory);
    fireTreeStructureChanged();
  }

  public Object getRoot() {
    return m_rootNode;
  }

  public Object getChild(Object parent, int index) {

    if (parent instanceof DirectoryNode) {
      return ((DirectoryNode)parent).getChild(index);
    }

    return null;
  }

  public int getChildCount(Object parent) {

    if (parent instanceof DirectoryNode) {
      return ((DirectoryNode)parent).getChildCount();
    }

    return 0;
  }

  public int getIndexOfChild(Object parent, Object child) {

    if (parent == null || child == null) {
      // The TreeModel Javadoc says we should do this.
      return -1;
    }

    if (parent instanceof DirectoryNode) {
      return ((DirectoryNode)parent).getIndexOfChild((Node)child);
    }
    else {
      return -1;
    }
  }

  public boolean isLeaf(Object node) {
    return node instanceof FileNode;
  }

  public void addTreeModelListener(TreeModelListener listener) {
    m_listeners.add(TreeModelListener.class, listener);
  }

  public void removeTreeModelListener(TreeModelListener listener) {
    m_listeners.remove(TreeModelListener.class, listener);
  }

  private void fireTreeStructureChanged() {

    final Object[] listeners = m_listeners.getListenerList();

    final TreeModelEvent event =
      new TreeModelEvent(this, new Object[] { getRoot() }, null, null);

    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        ((TreeModelListener)listeners[i + 1]).treeStructureChanged(event);
      }
    }
  }

  public void valueForPathChanged(TreePath path, Object newValue) {
    System.err.println("valueForPathChanged(" + path + ", " + newValue + ")");
    // Do nothing.
  }

  private static final FilenameFilter s_directoryFilter =
    new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return new File(dir, name).isDirectory();
      }
    };

  private static final FilenameFilter s_fileFilter =
    new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return new File(dir, name).isFile();
      }
    };

  /**
   * Node in the tree.
   */
  public abstract class Node {

    private final File m_file;

    protected Node(File file) {
      m_file = file;
    }

    public String toString() {
      return m_file.getName();
    }

    public final File getFile() {
      return m_file;
    }
  }

  /**
   * Node that represents a file. Used for the leaves of the tree.
   */
  public final class FileNode extends Node {

    private Buffer m_buffer = null;

    private FileNode(File file) {
      super(file);
    }

    public void setBuffer(Buffer buffer) {
      m_buffer = buffer;
    }

    public Buffer getBuffer() {
      return m_buffer;
    }

    public boolean isPythonFile() {
      return getFile().getName().endsWith(".py");
    }

    public boolean isBoringFile() {
      final String name = getFile().getName();

      return
        getFile().isHidden() ||
        name.endsWith(".class") ||
        name.endsWith("~") ||
        name.startsWith(".");
    }

    public boolean isOpen() {
      return getBuffer() != null;
    }

    public boolean isDirty() {
      return getBuffer() != null && getBuffer().isDirty();
    }
  }

  /**
   * Node that represents a directory.
   */
  private class DirectoryNode extends Node {

    private final File[] m_childDirectories;
    private final File[] m_childFiles;

    DirectoryNode(File file) {
      super(file);

      m_childDirectories = file.listFiles(s_directoryFilter);
      m_childFiles = file.listFiles(s_fileFilter);
    }

    public final Node getChild(int index) {
      if (index < m_childDirectories.length) {
        return new DirectoryNode(m_childDirectories[index]);
      }
      else {
        return new FileNode(m_childFiles[index - m_childDirectories.length]);
      }
    }

    public final int getChildCount() {
      return m_childDirectories.length + m_childFiles.length;
    }

    public final int getIndexOfChild(Node child) {
      for (int i = 0; i < m_childDirectories.length; ++i) {
        if (m_childDirectories[i].equals(child.getFile())) {
          return i;
        }
      }

      for (int i = 0; i < m_childFiles.length; ++i) {
        if (m_childFiles[i].equals(child.getFile())) {
          return m_childDirectories.length + i;
        }
      }

      return -1;
    }
  }

  /**
   * Root node of the tree.
   */
  private final class RootNode extends DirectoryNode {

    private RootNode(File file) {
      super(file);
    }

    public String toString() {
      return getFile().getPath();
    }
  }
}
