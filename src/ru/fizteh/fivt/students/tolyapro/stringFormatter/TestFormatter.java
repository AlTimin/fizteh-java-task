package ru.fizteh.fivt.students.tolyapro.stringFormatter;

import java.math.BigInteger;
import java.text.Format;
import java.util.Formatter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ru.fizteh.fivt.format.FormatterException;

public class TestFormatter {

    static ru.fizteh.fivt.students.tolyapro.stringFormatter.StringFormatter formatter = new StringFormatterFactory()
            .create(DoubleFormatter.class.getName(),
                    BigIntegerFormat.class.getName());

    static public class TestDouble {
        private final double d1 = 1;
        protected double d2 = 2;
        public final double d3 = 3;
        public double d4 = 4;
    }

    static public class TestDoubleChild extends TestDouble {
        public final double d3 = -3;
        private final double d5 = 0.55555;
    }

    static public class TestBigInteger {
        public final BigInteger b1 = new BigInteger("1");
        private BigInteger b2 = new BigInteger("2");
        private TestDoubleChild testDoubleChild = new TestDoubleChild();
    }

    static public class TestOtherFormats {
        public final String s1 = "s1";
        private final String s2 = "s2";
        private String s3 = null;
    }

    static public class TestNull {
        public TestDoubleChild t = null;
        private TestNull nullAgain = null;
    }

    static public class TestNull2 {
        public TestNull testNull = new TestNull();
    }

    TestDouble testDouble;
    TestDoubleChild testDoubleChild;
    TestBigInteger testBigInteger;
    TestOtherFormats testOtherFormats;
    Formatter format;
    String result;

    @Before
    public void init() {
        testDouble = new TestDouble();
        testDoubleChild = new TestDoubleChild();
        testBigInteger = new TestBigInteger();
        testOtherFormats = new TestOtherFormats();
        String result = new String();
        format = new Formatter();
    }

    @Test(expected = FormatterException.class)
    public void testBadPatternDouble() {
        double d = 100500;
        formatter.format("{0:...}", d);
    }

    @Test(expected = FormatterException.class)
    public void testExtn1() {
        ru.fizteh.fivt.students.tolyapro.stringFormatter.StringFormatter formatterTest = new StringFormatterFactory()
                .create(testDouble.getClass().toString());
    }

    @Test(expected = FormatterException.class)
    public void testExtn2() {
        ru.fizteh.fivt.students.tolyapro.stringFormatter.StringFormatter formatterTest = new StringFormatterFactory()
                .create(int.class.toString());
    }

    @Test(expected = FormatterException.class)
    public void testExtn3() {
        ru.fizteh.fivt.students.tolyapro.stringFormatter.StringFormatter formatterTest = new StringFormatterFactory()
                .create(null);
    }

    @Test(expected = FormatterException.class)
    public void testNullToFabric() {
        StringFormatterFactory stringFormatterFactory = new StringFormatterFactory();
        stringFormatterFactory.create(null);
    }

    @Test(expected = FormatterException.class)
    public void testBadPatternBigInteger() {
        BigInteger b = new BigInteger("100500");
        formatter.format("{0:...}", b);
    }

    @Test(expected = FormatterException.class)
    public void testBrackets1() {
        formatter.format("{0", 0);
    }

    @Test(expected = FormatterException.class)
    public void testBrackets2() {
        formatter.format("{}0", 0);
    }

    @Test(expected = FormatterException.class)
    public void testBrackets3() {
        formatter.format("0}", 0);
    }

    @Test(expected = FormatterException.class)
    public void testNegativeindex() {
        formatter.format("{-1}", testDouble.d4);
    }

    @Test(expected = FormatterException.class)
    public void testBadIndex() {
        formatter.format("{-0}", testDouble.d1);
    }

    @Test(expected = FormatterException.class)
    public void testBadDots1() {
        formatter.format("{0.}", "1");
    }

    @Test(expected = FormatterException.class)
    public void testBadDots2() {
        formatter.format("{0.}", 1);
    }

    @Test(expected = FormatterException.class)
    public void testBadDots3() {
        formatter.format("{0.d1.}", testDouble);
    }

    @Test
    public void correctnessTests() {

        result = formatter.format("{0.d5}", testDoubleChild);
        Assert.assertEquals("0.55555", result);

        result = formatter.format("{0.d5:.1f}", testDoubleChild);
        Assert.assertEquals(format.format("%.1f", 0.55555).toString(), result);

        result = formatter.format("{0.d4}", testDoubleChild);
        Assert.assertEquals("4.0", result);

        result = formatter.format("{{", testDoubleChild);
        Assert.assertEquals("{", result);

        result = formatter.format("}}", testDoubleChild);
        Assert.assertEquals("}", result);

        result = formatter.format("{{{0.d1}}}", testDouble);
        Assert.assertEquals("{1.0}", result);

        Double d = null;
        result = formatter.format("{0}", d);
        Assert.assertEquals("", result);

        result = formatter.format("{0.b1} + {0.b2} = 3", testBigInteger);
        Assert.assertEquals("1 + 2 = 3", result);

        result = formatter.format("{0.testDoubleChild.d4}", testBigInteger);
        Assert.assertEquals("4.0", result);

        result = formatter.format("{0.s1}", testOtherFormats);
        Assert.assertEquals("s1", result);

        BigInteger bNull = null;
        result = formatter.format("{0}a", bNull);
        Assert.assertEquals("a", result);

        result = formatter.format("a{0}b", bNull);
        Assert.assertEquals("ab", result);

        result = formatter.format("{0.s3}", testOtherFormats);
        Assert.assertEquals("", result);

        result = formatter.format("a{0.s3}b", testOtherFormats);
        Assert.assertEquals("ab", result);

        TestNull testNull = new TestNull();

        result = formatter.format("{0.t.d3}", testNull);
        Assert.assertEquals("", result);

        result = formatter.format(">>{0.t.d3}<<", testNull);
        Assert.assertEquals(">><<", result);

        result = formatter.format(">>{0.<>.<>}<<", testNull);
        Assert.assertEquals(">><<", result);

        result = formatter.format("{0.nullAgain.t.d3}", testNull);
        Assert.assertEquals("", result);

        TestNull2 testNull2 = new TestNull2();

        result = formatter.format("{0.testNull.nullAgain.t.d3}", testNull2);
        Assert.assertEquals("", result);

    }

}
