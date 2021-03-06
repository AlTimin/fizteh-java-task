package ru.fizteh.fivt.students.tolyapro.proxy;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;

public class InvocationHandler implements java.lang.reflect.InvocationHandler {

    Object target;
    Appendable writer;

    private String escapeString(String string) {
        String result = string;
        result = result.replaceAll("\\\\", "\\\\\\\\");
        result = result.replaceAll("\t", "\\\\t");
        result = result.replaceAll("\b", "\\\\b");
        result = result.replaceAll("\n", "\\\\n");
        result = result.replaceAll("\r", "\\\\r");
        result = result.replaceAll("\f", "\\\\f");
        result = result.replaceAll("\"", "\\\"");
        return result;
    }

    private String toString(Object object,
            IdentityHashMap<Object, Object> circularRefDetector) {
        if (object == null) {
            return "null";
        } else if (object.getClass().isArray()) {
            if (!object.getClass().getComponentType().isPrimitive()) {
                if (circularRefDetector.containsKey(object)
                        && object.getClass().isArray()) {
                    throw new RuntimeException("Circular reference in: "
                            + object.toString());
                } else {
                    circularRefDetector.put(object, object);
                }
                Object[] tmpArray = (Object[]) object;
                StringBuilder result = new StringBuilder();
                result.append(new Integer(tmpArray.length).toString());
                result.append('{');
                for (int i = 0; i < tmpArray.length; ++i) {
                    result.append(toString(tmpArray[i], circularRefDetector));
                    if (i != tmpArray.length - 1) {
                        result.append(", ");
                    }
                }
                result.append('}');
                return result.toString();
            } else {
                int length = Array.getLength(object);
                Object[] outputArray = new Object[length];
                for (int i = 0; i < length; ++i) {
                    outputArray[i] = Array.get(object, i);
                }
                return toString(outputArray, circularRefDetector);
            }
        } else if (object.getClass().isEnum()) {
            return ((Enum) object).name();
        } else if (object.getClass().equals(String.class)) {
            return "\"" + escapeString((String) object) + "\"";
        } else if (ru.fizteh.fivt.students.tolyapro.proxy.DetecterOfPrimitiveTypes
                .isPrimitive(object)) {
            return object.toString();
        } else {
            return "[" + escapeString(object.toString()) + "]";
        }
    }

    public InvocationHandler(Object target, Appendable writer) {
        this.target = target;
        this.writer = writer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        IdentityHashMap<Object, Object> circularRefDetector = new IdentityHashMap<Object, Object>();
        method.setAccessible(true);
        Class clazz = method.getDeclaringClass();
        if (clazz.equals(Object.class)) {
            return method.invoke(target, args);
        }
        final int magicConst = 60;
        boolean extendedMode = false;
        ArrayList<String> argsAsStrings = new ArrayList<String>();
        if (args != null) {
            for (int i = 0; i < args.length; ++i) {
                argsAsStrings.add(toString(args[i], circularRefDetector));
                if (argsAsStrings.get(i).length() > magicConst) {
                    extendedMode = true;
                }
            }
        }
        writer.append(method.getDeclaringClass().getSimpleName() + '.');
        writer.append(method.getName() + '(');
        if (extendedMode) {
            writer.append('\n');
        }
        for (int i = 0; i < argsAsStrings.size(); ++i) {
            if (extendedMode) {
                writer.append("  ");
            }
            writer.append(argsAsStrings.get(i));
            if (i != argsAsStrings.size() - 1) {
                writer.append(",");
            }
            if (extendedMode) {
                writer.append('\n');
            } else {
                if (i != argsAsStrings.size() - 1) {
                    writer.append(" ");
                }
            }
        }
        if (extendedMode) {
            writer.append("  ");
        }
        writer.append(")");
        Object returned = null;
        try {
            returned = method.invoke(target, args);
            if (returned != null) {
                if (extendedMode) {
                    writer.append("\n  returned ");
                } else {
                    writer.append(" returned ");
                }
                writer.append(toString(returned, circularRefDetector));
            }
        } catch (InvocationTargetException e) {
            if (extendedMode) {
                writer.append('\n');
                writer.append("  ");
            } else {
                writer.append(" ");
            }
            StackTraceElement[] elements = e.getTargetException()
                    .getStackTrace();
            writer.append("threw "
                    + e.getTargetException().getClass().getCanonicalName()
                    + ": " + e.getTargetException().getMessage() + '\n');
            for (int i = 0; i < elements.length; ++i) {
                if (extendedMode) {
                    writer.append("  ");
                }
                writer.append("  at ");
                writer.append(elements[i].toString());
                if (i != elements.length - 1) {
                    writer.append('\n');
                }
            }
            // System.out.println(writer);
            throw e.getTargetException();
        } catch (Throwable e) {
            throw e;
        }

        writer.append('\n');
        return returned;
    }

}
