// Copyright (C) 2009 Philip Aston
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
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.engine.process.jython;

import java.util.Random;

import junit.framework.TestCase;
import net.grinder.common.StubTest;
import net.grinder.common.Test;
import net.grinder.common.UncheckedGrinderException;
import net.grinder.common.UncheckedInterruptedException;
import net.grinder.engine.process.Instrumenter;
import net.grinder.engine.process.ScriptEngine.Instrumentation;
import net.grinder.engine.process.jython.JythonScriptEngine.JythonVersionAdapter;
import net.grinder.script.NotWrappableTypeException;
import net.grinder.testutility.AssertUtilities;
import net.grinder.testutility.RandomStubFactory;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyInstance;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;


/**
 * Instrumentation unit tests.
 *
 * @author Philip Aston
 * @version $Revision:$
 */
public abstract class AbstractInstrumenterTestCase extends TestCase {

  {
    PySystemState.initialize();
  }

  private final Instrumenter m_instrumenter;
  private final JythonVersionAdapter m_versionAdapter;

  private final PythonInterpreter m_interpreter =
    new PythonInterpreter(null, new PySystemState());

  private final PyObject m_one = new PyInteger(1);
  private final PyObject m_two = new PyInteger(2);
  private final PyObject m_three = new PyInteger(3);
  private final PyObject m_six = new PyInteger(6);
  private final Test m_test = new StubTest(1, "test");

  private final RandomStubFactory<Instrumentation>
    m_instrumentationStubFactory =
      RandomStubFactory.create(Instrumentation.class);
  private final Instrumentation m_instrumentation =
    m_instrumentationStubFactory.getStub();

  public AbstractInstrumenterTestCase(Instrumenter instrumenter)
    throws Exception {
    super();
    m_versionAdapter = new JythonVersionAdapter();
      m_instrumenter = instrumenter;
  }

  public void testCreateProxyWithPyFunction() throws Exception {
    m_interpreter.exec("def return1(): return 1");
    final PyObject pyFunction = m_interpreter.get("return1");
    final PyObject pyFunctionProxy = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, pyFunction);

    final PyObject result = pyFunctionProxy.invoke("__call__");
    assertEquals(m_one, result);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
    assertSame(m_test, pyFunctionProxy.__getattr__("__test__").__tojava__(
      Test.class));

    final PyObject targetReference =
      pyFunctionProxy.__getattr__("__target__");
    assertSame(pyFunction, targetReference);
    assertNotSame(pyFunctionProxy, targetReference);
    final PyObject targetResult =  targetReference.invoke("__call__");
    assertEquals(m_one, targetResult);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("def multiply(x, y): return x * y");
    final PyObject pyFunction2 = m_interpreter.get("multiply");
    final PyObject pyFunctionProxy2 = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, pyFunction2);
    final PyObject result2 =
      pyFunctionProxy2.invoke("__call__", m_two, m_three);
    assertEquals(m_six, result2);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject targetReference2 =
      pyFunctionProxy2.__getattr__("__target__");
    assertSame(pyFunction2, targetReference2);
    assertNotSame(pyFunctionProxy2, targetReference2);
    final PyObject targetResult2 =
      targetReference2.invoke("__call__", m_two, m_three);
    assertEquals(m_six, targetResult2);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result3 =
      pyFunctionProxy2.invoke("__call__", new PyObject[] { m_two, m_three});
    assertEquals(m_six, result3);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("def square(x): return x * x");
    final PyObject pyFunction11 = m_interpreter.get("square");
    final PyObject pyFunctionProxy11 = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, pyFunction11);
    final PyObject result11 = pyFunctionProxy11.invoke("__call__", m_two);
    assertEquals(new PyInteger(4), result11);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject targetReference11 =
      pyFunctionProxy11.__getattr__("__target__");
    assertSame(pyFunction11, targetReference11);
    assertNotSame(pyFunctionProxy11, targetReference11);
    final PyObject targetResult11 =
      targetReference11.invoke("__call__", m_three);
    assertEquals(new PyInteger(9), targetResult11);
    m_instrumentationStubFactory.assertNoMoreCalls();

    // From Jython.
    m_interpreter.set("proxy", pyFunctionProxy);
    m_interpreter.set("proxy2", pyFunctionProxy2);

    m_interpreter.exec("result5 = proxy()");
    final PyObject result5 = m_interpreter.get("result5");
    assertEquals(m_one, result5);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result6 = proxy.__target__()");
    final PyObject result6 = m_interpreter.get("result6");
    assertEquals(m_one, result6);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result7 = proxy2.__target__(2, 3)");
    final PyObject result7 = m_interpreter.get("result7");
    assertEquals(m_six, result7);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("def multiply4(a, b, c, d): return a * b * c * d");
    final PyObject pyFunction3 = m_interpreter.get("multiply4");
    final PyObject pyFunctionProxy3 = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, pyFunction3);
    m_interpreter.set("proxy3", pyFunctionProxy3);

    m_interpreter.exec("result8 = proxy3.__target__(1, 2, 3, 1)");
    final PyObject result8 = m_interpreter.get("result8");
    assertEquals(m_six, result8);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result9 = proxy3.__target__(1, 2, 3, d=1)");
    final PyObject result9 = m_interpreter.get("result9");
    assertEquals(m_six, result9);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("def identity(a): return a");
    final PyObject pyFunction4 = m_interpreter.get("identity");
    final PyObject pyFunctionProxy4 = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, pyFunction4);
    m_interpreter.set("proxy4", pyFunctionProxy4);

    m_interpreter.exec("result10 = proxy4.__target__(1)");
    final PyObject result10 = m_interpreter.get("result10");
    assertEquals(m_one, result10);
    m_instrumentationStubFactory.assertNoMoreCalls();
  }

  public void testCreateProxyWithPyInstance() throws Exception {
    // PyInstance.
    m_interpreter.exec(
      "class Foo:\n" +
      " def two(self): return 2\n" +
      " def identity(self, x): return x\n" +
      " def sum(self, x, y): return x + y\n" +
      " def sum3(self, x, y, z): return x + y + z\n" +
      "x=Foo()");

    final PyObject pyInstance = m_interpreter.get("x");
    final PyObject pyInstanceProxy = (PyObject) m_instrumenter
        .createInstrumentedProxy(m_test, m_instrumentation, pyInstance);
    final PyObject result1 = pyInstanceProxy.invoke("two");
    assertEquals(m_two, result1);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
    assertSame(m_test, pyInstanceProxy.__getattr__("__test__").__tojava__(
      Test.class));
    assertNull(pyInstanceProxy.__findattr__("__blah__"));

    final PyObject targetReference =
      pyInstanceProxy.__getattr__("__target__");
    assertSame(pyInstance, targetReference);
    assertNotSame(pyInstanceProxy, targetReference);
    final PyObject targetResult =  targetReference.invoke("two");
    assertEquals(m_two, targetResult);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result2 = pyInstanceProxy.invoke("identity", m_one);
    assertSame(m_one, result2);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result3 = pyInstanceProxy.invoke("sum", m_one, m_two);
    assertEquals(m_three, result3);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result4 = pyInstanceProxy.invoke("sum3", new PyObject[] {
        m_one, m_two, m_three });
    assertEquals(m_six, result4);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result5 = pyInstanceProxy.invoke("sum", new PyObject[] {
        m_one, m_two }, new String[] { "x", "y" });
    assertEquals(m_three, result5);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    // From Jython.
    m_interpreter.set("proxy", pyInstanceProxy);

    m_interpreter.exec("result6 = proxy.sum(2, 4)");
    final PyObject result6 = m_interpreter.get("result6");
    assertEquals(m_six, result6);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result7 = proxy.__target__.two()");
    final PyObject result7 = m_interpreter.get("result7");
    assertEquals(m_two, result7);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result8 = proxy.__target__.identity(2)");
    final PyObject result8 = m_interpreter.get("result8");
    assertEquals(m_two, result8);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result9 = proxy.__target__.sum(2, 4)");
    final PyObject result9 = m_interpreter.get("result9");
    assertEquals(m_six, result9);
    m_instrumentationStubFactory.assertNoMoreCalls();
  }

  public void testCreateProxyWithPyMethod() throws Exception {
    m_interpreter.exec(
      "class Foo:\n" +
      " def two(self): return 2\n" +
      " def identity(self, x): return x\n" +
      " def sum(self, x, y): return x + y\n" +
      " def sum3(self, x, y, z): return x + y + z\n" +
      "x=Foo()");
    final PyObject pyInstance = m_interpreter.get("x");
    m_interpreter.exec("y=Foo.two");
    final PyObject pyMethod = m_interpreter.get("y");
    final PyObject pyMethodProxy = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, pyMethod);
    final PyObject result = pyMethodProxy.invoke("__call__", pyInstance);
    assertEquals(m_two, result);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
    assertSame(m_test, pyMethodProxy.__getattr__("__test__").__tojava__(
      Test.class));
    assertNull(pyMethodProxy.__findattr__("__blah__"));

    final PyObject targetReference =
      pyMethodProxy.__getattr__("__target__");
    assertSame(pyMethod, targetReference);
    assertNotSame(pyMethodProxy, targetReference);
    final PyObject targetResult =
      targetReference.invoke("__call__", pyInstance);
    assertEquals(m_two, targetResult);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("y=Foo.identity");
    final PyObject pyMethod2 = m_interpreter.get("y");
    final PyObject pyMethodProxy2 = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, pyMethod2);
    final PyObject result2 =
      pyMethodProxy2.invoke("__call__", pyInstance, m_one);
    assertEquals(m_one, result2);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("y=Foo.sum");
    final PyObject pyMethod3 = m_interpreter.get("y");
    final PyObject pyMethodProxy3 = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, pyMethod3);
    final PyObject result3 =
      pyMethodProxy3.invoke(
        "__call__", new PyObject[] { pyInstance, m_one, m_two });
    assertEquals(m_three, result3);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("y=x.two"); // Bound method.
    final PyObject pyMethod4 = m_interpreter.get("y");
    final PyObject pyMethodProxy4 = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, pyMethod4);
    final PyObject result4 = pyMethodProxy4.invoke("__call__");
    assertEquals(m_two, result4);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    // From Jython.
    m_interpreter.set("proxy", pyMethodProxy);
    m_interpreter.set("proxy2", pyMethodProxy2);
    m_interpreter.set("proxy3", pyMethodProxy3);
    m_interpreter.set("proxy4", pyMethodProxy4);

    m_interpreter.exec("result5 = proxy(x)");
    final PyObject result5 = m_interpreter.get("result5");
    assertEquals(m_two, result5);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result6 = proxy.__target__(x)");
    final PyObject result6 = m_interpreter.get("result6");
    assertEquals(m_two, result6);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result7 = proxy2.__target__(x, 2)");
    final PyObject result7 = m_interpreter.get("result7");
    assertEquals(m_two, result7);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result8 = proxy3.__target__(x, 2, 4)");
    final PyObject result8 = m_interpreter.get("result8");
    assertEquals(m_six, result8);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result9 = proxy4.__target__()");
    final PyObject result9 = m_interpreter.get("result9");
    assertEquals(m_two, result9);
    m_instrumentationStubFactory.assertNoMoreCalls();
  }

  public void testCreateProxyWithPyJavaInstance() throws Exception {
    m_interpreter.exec("from java.util import Random\nx=Random()");
    final PyObject pyJava = m_interpreter.get("x");
    final PyObject pyJavaProxy = (PyObject) m_instrumenter
        .createInstrumentedProxy(m_test, m_instrumentation, pyJava);
    final PyObject result = pyJavaProxy.invoke("getClass");
    assertEquals(Random.class, result.__tojava__(Class.class));
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
    assertSame(m_test,
               pyJavaProxy.__getattr__("__test__").__tojava__(Test.class));
    assertNull(pyJavaProxy.__findattr__("__blah__"));

    final PyObject targetReference = pyJavaProxy.__getattr__("__target__");
    assertSame(pyJava, targetReference);
    assertNotSame(pyJavaProxy, targetReference);
    final PyObject targetResult =  targetReference.invoke("getClass");
    assertEquals(Random.class, targetResult.__tojava__(Class.class));
    m_instrumentationStubFactory.assertNoMoreCalls();

    // From Jython.
    m_interpreter.set("proxy", pyJavaProxy);

    m_interpreter.exec("result2 = proxy.getClass()");
    final PyObject result2 = m_interpreter.get("result2");
    assertEquals(Random.class, result2.__tojava__(Class.class));
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result3 = proxy.__target__.getClass()");
    final PyObject result3 = m_interpreter.get("result3");
    assertEquals(Random.class, result3.__tojava__(Class.class));
    m_instrumentationStubFactory.assertNoMoreCalls();
  }

  public void testCreateProxyWithPyReflectedFunction() throws Exception {
    m_interpreter.exec("from java.util import Random\nx=Random()");
    final PyObject pyJava = m_interpreter.get("x");
    m_interpreter.exec("y=Random.nextInt");
    final PyObject pyJavaMethod = m_interpreter.get("y");
    final PyObject pyJavaMethodProxy = (PyObject) m_instrumenter
        .createInstrumentedProxy(m_test, m_instrumentation, pyJavaMethod);
    final PyObject result = pyJavaMethodProxy.__call__(pyJava);
    assertTrue(result.__tojava__(Object.class) instanceof Integer);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
    assertSame(m_test, pyJavaMethodProxy.__getattr__("__test__").__tojava__(
      Test.class));
    assertNull(pyJavaMethodProxy.__findattr__("__blah__"));

    final PyObject targetReference =
      pyJavaMethodProxy.__getattr__("__target__");
    assertSame(pyJavaMethod, targetReference);
    assertNotSame(pyJavaMethodProxy, targetReference);
    final PyObject targetResult =  targetReference.__call__(pyJava);
    assertTrue(targetResult.__tojava__(Object.class) instanceof Integer);
    m_instrumentationStubFactory.assertNoMoreCalls();

    // From Jython.
    m_interpreter.set("proxy", pyJavaMethodProxy);

    m_interpreter.exec("result2 = proxy(x)");
    final PyObject result2 = m_interpreter.get("result2");
    assertTrue(result2.__tojava__(Object.class) instanceof Integer);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result3 = proxy.__target__(x)");
    final PyObject result3 = m_interpreter.get("result3");
    assertTrue(result3.__tojava__(Object.class) instanceof Integer);
    m_instrumentationStubFactory.assertNoMoreCalls();
  }

  public void testCreateProxyWithPyProxy() throws Exception {
    // PyProxy's come paired with PyInstances - need to call
    // __tojava__ to get the PyProxy.
    m_interpreter.exec("from java.util import Random");
    m_interpreter.exec(
      "class PyRandom(Random):\n" +
      " def one(self): return 1\n" +
      "x=PyRandom()");
    final Object pyProxy = m_interpreter.get("x").__tojava__(Object.class);
    final PyObject pyProxyProxy = (PyObject) m_instrumenter
        .createInstrumentedProxy(m_test, m_instrumentation, pyProxy);
    final PyObject result = pyProxyProxy.invoke("one");
    assertEquals(m_one, result);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
    assertSame(m_test, pyProxyProxy.__getattr__("__test__")
        .__tojava__(Test.class));

    final PyObject targetReference =  pyProxyProxy.__getattr__("__target__");
    assertSame(pyProxy, targetReference.__tojava__(Object.class));
    assertNotSame(pyProxyProxy, targetReference);
    final PyObject targetResult =  targetReference.invoke("one");
    assertEquals(m_one, targetResult);
    m_instrumentationStubFactory.assertNoMoreCalls();

    // From Jython.
    m_interpreter.set("proxy", pyProxyProxy);

    m_interpreter.exec("result2 = proxy.one()");
    final PyObject result2 = m_interpreter.get("result2");
    assertEquals(m_one, result2);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result3 = proxy.nextInt()");
    final PyObject result3 = m_interpreter.get("result3");
    assertNotNull(result3);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result4 = proxy.__target__.one()");
    final PyObject result4 = m_interpreter.get("result4");
    assertEquals(m_one, result4);
    m_instrumentationStubFactory.assertNoMoreCalls();
  }

  public void testCreateProxyWithJavaInstance() throws Exception {
    final Object java = new MyClass();
    final PyObject javaProxy = (PyObject)
      m_instrumenter.createInstrumentedProxy(m_test, m_instrumentation, java);
    final PyObject result =
      javaProxy.invoke("addOne", Py.java2py(new Integer(10)));
    assertEquals(new Integer(11), result.__tojava__(Integer.class));
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
    assertSame(m_test, javaProxy.__getattr__("__test__").__tojava__(Test.class));

    final PyObject targetReference = javaProxy.__getattr__("__target__");
    assertSame(java, targetReference.__tojava__(Object.class));
    assertNotSame(javaProxy, targetReference);
    final PyObject targetResult =
      targetReference.invoke("addOne", Py.java2py(new Integer(10)));
    assertEquals(new Integer(11), targetResult.__tojava__(Integer.class));
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result1 = javaProxy.invoke("getClass");
    assertEquals(MyClass.class, result1.__tojava__(Class.class));
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result2 = javaProxy.invoke("sum", m_one, m_two);
    assertEquals(m_three, result2);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result3 = javaProxy.invoke("sum3", new PyObject[] { m_one,
        m_two, m_three });
    assertEquals(m_six, result3);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result4 = javaProxy.invoke("sum", new PyObject[] { m_one,
        m_two }, Py.NoKeywords);
    assertEquals(m_three, result4);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    // From Jython.
    m_interpreter.set("proxy", javaProxy);

    m_interpreter.exec("result5 = proxy.sum3(0, -29, 30)");
    final PyObject result5 = m_interpreter.get("result5");
    assertEquals(m_one, result5);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result5Cached = proxy.sum3(0, -29, 30)");
    final PyObject result5Cached = m_interpreter.get("result5Cached");
    assertEquals(m_one, result5Cached);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result6 = proxy.sum(1, 1)");
    final PyObject result6 = m_interpreter.get("result6");
    assertEquals(m_two, result6);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result7 = proxy.getClass()");
    final PyObject result7 = m_interpreter.get("result7");
    assertEquals(MyClass.class, result7.__tojava__(Class.class));
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result8 = proxy.__target__.getClass()");
    final PyObject result8 = m_interpreter.get("result8");
    assertEquals(MyClass.class, result8.__tojava__(Class.class));
    m_instrumentationStubFactory.assertNoMoreCalls();
  }

  public void testCreateProxyWithJavaClass() throws Exception {
    final Class<?> javaClass = MyClass.class;
    final PyObject javaProxy = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, javaClass);
    final PyObject result =
      javaProxy.invoke("addTwo", Py.java2py(new Integer(10)));
    assertEquals(new Integer(12), result.__tojava__(Integer.class));
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
    assertSame(m_test, javaProxy.__getattr__("__test__").__tojava__(Test.class));

    final PyObject targetReference = javaProxy.__getattr__("__target__");
    assertSame(javaClass, targetReference.__tojava__(Object.class));
    assertNotSame(javaProxy, targetReference);
    final PyObject targetResult =
      targetReference.invoke("addTwo", Py.java2py(new Integer(10)));
    assertEquals(new Integer(12), targetResult.__tojava__(Integer.class));
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result1 = javaProxy.invoke("staticSum", m_one, m_two);
    assertEquals(m_three, result1);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result2 = javaProxy.invoke("staticSum3",
      new PyObject[] { m_one,  m_two, m_three });
    assertEquals(m_six, result2);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result3 = javaProxy.invoke("staticSum",
      new PyObject[] { m_one, m_two }, Py.NoKeywords);
    assertEquals(m_three, result3);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject result4 = javaProxy.invoke("staticSix");
    assertEquals(m_six, result4);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject instance = javaProxy.__call__();
    assertEquals(MyClass.class,
      m_versionAdapter.getClassForInstance((PyInstance) instance)
      .__tojava__(Class.class));
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);

    final PyObject instance2 = javaProxy.__call__(
      new PyObject[] { m_one, m_two, m_three, },
      new String[] { "c", "b", "a" });
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    final MyClass javaInstance2 = (MyClass) instance2.__tojava__(MyClass.class);
    assertEquals(3, javaInstance2.getA());
    assertEquals(2, javaInstance2.getB());
    assertEquals(1, javaInstance2.getC());
    m_instrumentationStubFactory.assertNoMoreCalls();

    final PyObject instance3 = javaProxy.__call__(m_one, m_two, m_three);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    final MyClass javaInstance3 = (MyClass) instance3.__tojava__(MyClass.class);
    assertEquals(1, javaInstance3.getA());
    assertEquals(2, javaInstance3.getB());
    assertEquals(3, javaInstance3.getC());
    m_instrumentationStubFactory.assertNoMoreCalls();

    // From Jython.
    m_interpreter.set("proxy", javaProxy);

    m_interpreter.exec("result5 = proxy.staticSum3(0, -29, 30)");
    final PyObject result5 = m_interpreter.get("result5");
    assertEquals(m_one, result5);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result5Cached = proxy.staticSum3(0, -29, 30)");
    final PyObject result5Cached = m_interpreter.get("result5Cached");
    assertEquals(m_one, result5Cached);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result6 = proxy.staticSum(1, 1)");
    final PyObject result6 = m_interpreter.get("result6");
    assertEquals(m_two, result6);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("result7 = proxy.staticSix()");
    final PyObject result7 = m_interpreter.get("result7");
    assertEquals(m_six, result7);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("instance = proxy(a=1, c=2, b=3)\nb=instance.b");
    final PyObject result8 = m_interpreter.get("b");
    assertEquals(m_three, result8);
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();

    m_interpreter.exec("instance = proxy()\n");
    final PyObject result9 = m_interpreter.get("instance");
    assertEquals(MyClass.class,
      m_versionAdapter.getClassForInstance((PyInstance) result9)
      .__tojava__(Class.class));
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
  }

  public void testCreateProxyWithRecursiveCode() throws Exception {
    m_interpreter.exec(
      "class Recurse:\n" +
      "  def __init__(self):\n" +
      "    self.i = 3\n" +
      "  def foo(self):\n" +
      "    self.i = self.i - 1\n" +
      "    if self.i == 0: return 0\n" +
      "    return self.i + self.foo()\n" +
      "r = Recurse()");

    final PyObject proxy = (PyObject)
      m_instrumenter.createInstrumentedProxy(
        m_test, m_instrumentation, m_interpreter.get("r"));

    final PyObject result = proxy.invoke("foo");

    assertEquals(new PyInteger(3), result);
    // The dispatcher will be called multiple times. The real dispatcher
    // only records the outer invocation.
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertSuccess("end", true);
    m_instrumentationStubFactory.assertNoMoreCalls();
  }

  public void testCreateProxyWithNonWrappableParameters() throws Exception {

    // Can't wrap arrays.
    assertNotWrappable(new int[] { 1, 2, 3 });
    assertNotWrappable(new Object[] { "foo", new Object() });

    // Can't wrap strings.
    assertNotWrappable("foo bah");

    // Can't wrap numbers.
    assertNotWrappable(new Long(56));
    assertNotWrappable(new Integer(56));
    assertNotWrappable(new Short((short) 56));
    assertNotWrappable(new Byte((byte) 56));

    final PythonInterpreter interpreter = new PythonInterpreter(null,
      new PySystemState());

    // Can't wrap PyInteger.
    interpreter.exec("x=1");
    assertNotWrappable(interpreter.get("x"));

    // Can't wrap PyClass.
    interpreter.exec("class Foo: pass");
    assertNotWrappable(interpreter.get("Foo"));

    // Can't wrap None.
    assertNotWrappable(null);
  }

  private void assertNotWrappable(Object o) throws Exception {
    try {
      m_instrumenter.createInstrumentedProxy(null, null, o);
      fail("Expected NotWrappableTypeException");
    }
    catch (NotWrappableTypeException e) {
    }
  }

  public void testPyDispatcherErrorHandling() throws Exception {
    m_interpreter.exec("def blah(): raise 'a problem'");
    final PyObject pyFunction = m_interpreter.get("blah");
    final PyObject pyFunctionProxy = (PyObject) m_instrumenter
        .createInstrumentedProxy(m_test, m_instrumentation, pyFunction);
    try {
      pyFunctionProxy.invoke("__call__");
      fail("Expected PyException");
    }
    catch (PyException e) {
      AssertUtilities.assertContains(e.toString(), "a problem");
    }

    m_instrumentationStubFactory.assertSuccess("start");
    m_instrumentationStubFactory.assertSuccess("end", false);
    m_instrumentationStubFactory.assertNoMoreCalls();

    final UncheckedGrinderException e = new UncheckedInterruptedException(null);
    m_instrumentationStubFactory.setThrows("start", e);

    try {
      pyFunctionProxy.invoke("__call__");
      fail("Expected UncheckedGrinderException");
    }
    catch (UncheckedGrinderException e2) {
      assertSame(e, e2);
    }
  }

  public static class MyClass {
    private int m_a;
    private int m_b;
    private int m_c;

    public MyClass() {
      this(0, 0, 0);
    }

    public MyClass(int a, int b, int c) {
      m_a = a;
      m_b = b;
      m_c = c;
    }

    public int addOne(int i) {
      return i + 1;
    }

    public int sum(int x, int y) {
      return x + y;
    }

    public int sum3(int x, int y, int z) {
      return x + y + z;
    }

    public static int addTwo(int i) {
      return i + 2;
    }

    public static int staticSum(int x, int y) {
      return x + y;
    }

    public static int staticSum3(int x, int y, int z) {
      return x + y + z;
    }

    public static int staticSix() {
      return 6;
    }

    public int getA() {
      return m_a;
    }

    public void setA(int a) {
      m_a = a;
    }

    public int getB() {
      return m_b;
    }

    public void setB(int b) {
      m_b = b;
    }

    public int getC() {
      return m_c;
    }

    public void setC(int c) {
      m_c = c;
    }
  }
}