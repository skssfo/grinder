// Copyright (C) 2001, 2002, 2003 Philip Aston
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

import java.io.File;

import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import net.grinder.engine.EngineException;


/**
 * Wrap up the context information necessary to invoke a Jython script.
 *
 * Package scope.
 *
 * @author Philip Aston
 * @version $Revision$
 */
final class JythonScript {
  private static final String TEST_RUNNER_CALLABLE_NAME = "TestRunner";

  private final PySystemState m_systemState;
  private final PythonInterpreter m_interpreter;
  private final PyObject m_testRunnerFactory;

  public JythonScript(ProcessContext processContext, File scriptFile)
    throws EngineException {

    PySystemState.initialize();

    m_systemState = new PySystemState();
    m_interpreter = new PythonInterpreter(null, m_systemState);

    m_interpreter.set("grinder", processContext.getScriptContext());

    final String parentPath = scriptFile.getParent();

    m_systemState.path.insert(0, new PyString(parentPath != null ?
                                              parentPath : ""));

    processContext.getLogger().output(
      "executing \"" + scriptFile.getPath() + "\"");

    try {
      // Run the test script, script does global set up here.
      m_interpreter.execfile(scriptFile.getPath());
    }
    catch (PyException e) {
      throw new JythonScriptExecutionException("initialising test script", e);
    }

    // Find the callable that acts as a factory for test runner instances.
    m_testRunnerFactory = m_interpreter.get(TEST_RUNNER_CALLABLE_NAME);

    if (m_testRunnerFactory == null || !m_testRunnerFactory.isCallable()) {
      throw new EngineException(
        "There is no callable (class or function) named '" +
        TEST_RUNNER_CALLABLE_NAME + "' in " + scriptFile);
    }
  }

  /**
   * We don't use m_interpreter.cleanup(), which delegates to
   * PySystemState.callExitFunc, as callExitFunc logs problems to
   * stderr. Instead we duplicate the callExitFunc behaviour raise
   * our own exceptions.
  */
  public void shutdown() throws EngineException {

    final PyObject exitfunc = m_systemState.__findattr__("exitfunc");

    if (exitfunc != null) {
      try {
        exitfunc.__call__();
      }
      catch (PyException e) {
        throw new JythonScriptExecutionException(
          "calling script exit function", e);
      }
    }
  }

  /**
   * Wrapper for script's TestRunner.
   */
  final class JythonRunnable {

    private final PyObject m_testRunner;

    public JythonRunnable() throws EngineException {

      try {
        // Script does per-thread initialisation here and
        // returns a callable object.
        m_testRunner = m_testRunnerFactory.__call__();
      }
      catch (PyException e) {
        throw new JythonScriptExecutionException(
          "creating per-thread test runner object", e);
      }

      if (!m_testRunner.isCallable()) {
        throw new EngineException(
          "The result of '" + TEST_RUNNER_CALLABLE_NAME +
          "()' is not callable");
      }
    }

    public void run() throws EngineException {

      try {
        m_testRunner.__call__();
      }
      catch (PyException e) {
        throw new JythonScriptExecutionException("invoking test runner", e);
      }
    }

    /**
     * <p>Ensure that if the test runner has a <code>__del__</code>
     * attribute, it is called when the thread is shutdown. Normally
     * Jython defers this to the Java garbage collector, so we might
     * have done something like
     *
     * <blockquote><pre>
     * m_testRunner = null; Runtime.getRuntime().gc();
     *</pre></blockquote>
     *
     * instead. However this would have a number of problems:
     *
     * <ol>
     * <li>Some JVM's may chose not to finalise the test runner in
     * response to <code>gc()</code>.</li>
     * <li><code>__del__</code> would be called by a GC thread.</li>
     * <li>The standard Jython finalizer wrapping around
     * <code>__del__</code> logs to <code>stderr</code>.</li>
     * </ol></p>
     *
     * <p>Instead, we call any <code>__del__</code> ourselves. After
     * calling this method, the <code>PyObject</code> that underlies
     * this class is made invalid.</p>
    */
    public void shutdown() throws EngineException {

      final PyObject del = m_testRunner.__class__.__findattr__("__del__");

      if (del != null) {
        try {
          del.__call__(m_testRunner);
        }
        catch (PyException e) {
          throw new JythonScriptExecutionException(
            "deleting test runner object", e);
        }
      }

      // To avoid the (pretty small) chance of the test runner being
      // finalized and __del__ being run twice, we do disable it.
      // Unfortunately, Jython caches the __del__ attribute and makes
      // it impossible to turn it off at a class level. Instead we do
      // this:
      m_testRunner.__class__ = null;
    }
  }
}
