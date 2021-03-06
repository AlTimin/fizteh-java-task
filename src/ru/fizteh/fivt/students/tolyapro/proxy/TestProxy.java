package ru.fizteh.fivt.students.tolyapro.proxy;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestProxy {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    interface InterfaceTestEscape {
        public String testEscape(String string);

    }

    class ClassTestEscape implements InterfaceTestEscape {

        @Override
        public String testEscape(String string) {
            String tmp = new String();
            tmp += "\n\t String: \n" + string + "\n";
            return tmp;
        }

    }

    interface InterfaceWithoutMethods {

    }

    class ClassWithoutMethods implements InterfaceWithoutMethods {

    }

    interface SimpleInterface {
        int getOne();

        void getException();

        String getHello();

        Object testBadRef();

        public String manyArgs(int a, int b, int c, int d);

        public String manyArgsExtended(int a, int b, int c, int d,
                String extended);
    }

    class SimpleClass implements SimpleInterface {

        @Override
        public int getOne() {
            return 1;
        }

        public String manyArgs(int a, int b, int c, int d) {
            return new Integer(a + b + c + d).toString();
        }

        @Override
        public String getHello() {
            return "Hello world!";
        }

        @Override
        public void getException() {
            throw new RuntimeException("I am an exception");
        }

        @Override
        public Object testBadRef() {
            Object[] array = new Object[1];
            array[0] = array;
            return array;
        }

        @Override
        public String manyArgsExtended(int a, int b, int c, int d,
                String extended) {
            return new Integer(a + b + c + d).toString() + extended;
        }

    }

    interface ExtendedInterface {
        public int[] fillTheArray(int n);

        public void iAmJustVeryVeryBigMethodNameAndICanBeEvenBiggerExclamationMark(
                int small);

        public void getException(String string);

    }

    class ExtendedClass implements ExtendedInterface {

        @Override
        public int[] fillTheArray(int n) {
            int[] result = new int[n];
            for (int i = 0; i < n; ++i) {
                result[i] = n;
            }
            return result;
        }

        @Override
        public void iAmJustVeryVeryBigMethodNameAndICanBeEvenBiggerExclamationMark(
                int small) {
            small++;
        }

        @Override
        public void getException(String string) {
            throw new RuntimeException(string);

        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull1() {
        LoggingProxyFactory factory = new LoggingProxyFactory();
        SimpleClass simpleClass = new SimpleClass();
        StringBuffer writer = new StringBuffer();
        factory.createProxy(simpleClass, null, SimpleInterface.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull2() {
        LoggingProxyFactory factory = new LoggingProxyFactory();
        SimpleClass simpleClass = new SimpleClass();
        StringBuffer writer = new StringBuffer();
        factory.createProxy(null, writer, SimpleInterface.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadInterface() {
        LoggingProxyFactory factory = new LoggingProxyFactory();
        ClassWithoutMethods classWithoutMethods = new ClassWithoutMethods();
        StringBuffer writer = new StringBuffer();
        factory.createProxy(classWithoutMethods, null,
                InterfaceWithoutMethods.class);
    }

    @Test
    public void testSimple() {
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Circular reference in:");
        LoggingProxyFactory factory = new LoggingProxyFactory();
        SimpleClass simpleClass = new SimpleClass();
        StringBuffer writer = new StringBuffer();
        SimpleInterface proxy = (SimpleInterface) factory.createProxy(
                simpleClass, writer, SimpleInterface.class);
        proxy.getOne();
        Assert.assertEquals("SimpleInterface.getOne() returned 1\n",
                writer.toString());
        proxy.getHello();
        Assert.assertTrue(writer.toString().contains(
                "SimpleInterface.getHello() returned \"Hello world!\""));
        proxy.testBadRef();
    }

    @Test(expected = Exception.class)
    public void testExtended() {

        LoggingProxyFactory factory = new LoggingProxyFactory();
        ExtendedClass extendedClass = new ExtendedClass();
        StringBuffer writer = new StringBuffer();
        ExtendedInterface proxy = (ExtendedInterface) factory.createProxy(
                extendedClass, writer, ExtendedInterface.class);
        proxy.fillTheArray(10);
        Assert.assertEquals(
                "ExtendedInterface.fillTheArray(10) returned 10{10, 10, 10, 10, 10, 10, 10, 10, 10, 10}\n",
                writer.toString());
        proxy.iAmJustVeryVeryBigMethodNameAndICanBeEvenBiggerExclamationMark(100500);
        Assert.assertTrue(writer
                .toString()
                .contains(
                        "ExtendedInterface.iAmJustVeryVeryBigMethodNameAndICanBeEvenBiggerExclamationMark(100500)"));
        proxy.getException("This string is so big that nobody will read it till the end This string is so big that nobody will read it till the end This string is so big that nobody will read it till the end");

    }

    @Test
    public void testEscape() {
        LoggingProxyFactory factory = new LoggingProxyFactory();
        ClassTestEscape classTestEscape = new ClassTestEscape();
        StringBuffer writer = new StringBuffer();
        InterfaceTestEscape proxy = (InterfaceTestEscape) factory.createProxy(
                classTestEscape, writer, InterfaceTestEscape.class);
        proxy.testEscape("yet another string");
        String returned = writer.toString();
        Assert.assertTrue(returned
                .contains("\"\\n\\t String: \\nyet another string\\n\""));
        proxy.testEscape(null);
        returned = writer.toString();
        Assert.assertTrue(returned
                .contains("InterfaceTestEscape.testEscape(null)"));

    }

    @Test
    public void testFormat() {
        LoggingProxyFactory factory = new LoggingProxyFactory();
        SimpleClass simpleClass = new SimpleClass();
        StringBuffer writer = new StringBuffer();
        SimpleInterface proxy = (SimpleInterface) factory.createProxy(
                simpleClass, writer, SimpleInterface.class);
        proxy.manyArgs(1, 2, 3, 4);
        Assert.assertTrue(writer.toString().contains(
                "SimpleInterface.manyArgs(1, 2, 3, 4) returned \"10\""));
    }

    @Test
    public void testFormat2() {
        LoggingProxyFactory factory = new LoggingProxyFactory();
        SimpleClass simpleClass = new SimpleClass();
        StringBuffer writer = new StringBuffer();
        SimpleInterface proxy = (SimpleInterface) factory.createProxy(
                simpleClass, writer, SimpleInterface.class);
        // writer = new StringBuffer();
        proxy.manyArgsExtended(
                1,
                2,
                3,
                4,
                "more than sixty chars more than sixty chars more than sixty chars more than sixty chars more than sixty chars");
        Assert.assertTrue(writer
                .toString()
                .contains(
                        "SimpleInterface.manyArgsExtended(\n  1,\n  2,\n  3,\n  4,\n  \"more than"));
        Assert.assertTrue(writer.toString().contains(
                "chars\"\n  )\n  returned \"10"));
        //System.out.println(writer);
    }
}
