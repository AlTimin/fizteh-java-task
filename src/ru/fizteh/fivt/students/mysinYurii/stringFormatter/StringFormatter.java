package ru.fizteh.fivt.students.mysinYurii.stringFormatter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import ru.fizteh.fivt.format.FormatterException;
import ru.fizteh.fivt.format.StringFormatterExtension;

public class StringFormatter implements ru.fizteh.fivt.format.StringFormatter {
    Vector<StringFormatterExtension> extentions;
    
    public StringFormatter() {
        extentions = new Vector<StringFormatterExtension>();
    }
    
    public void addNewExtension(StringFormatterExtension newExtension) throws FormatterException {
        if (newExtension == null) {
            return;
        } else {
            try {
                extentions.add(newExtension);
            } catch (FormatterException e) {
                throw new FormatterException("Cannot add extension");
            }
        }
    }
    
    public String format(String toFormat, Object... arguments) throws FormatterException {
        StringBuilder result = new StringBuilder();
        format(result, toFormat, arguments);
        return result.toString();
    }
    
    public void format(StringBuilder result, String toFormat, Object... arguments) throws FormatterException {
        formatFrom(result, toFormat, 0, arguments);
    }

    private void formatFrom(StringBuilder result, String toFormat, int index,
            Object... arguments) throws FormatterException {
        int openBracketPos = toFormat.indexOf("{", index);
        int closeBracketPos = toFormat.indexOf("}", index);
        if (openBracketPos == -1 && closeBracketPos == -1) {
            result.append(toFormat.substring(index));
            return;
        }
        if (openBracketPos == -1) {
            openBracketPos = toFormat.length();
        }
        if (closeBracketPos == -1) {
            closeBracketPos = toFormat.length();
        }
        if (closeBracketPos > openBracketPos) {
            if (openBracketPos < toFormat.length() - 1
                    && toFormat.charAt(openBracketPos + 1) == '{') {
                result.append(toFormat.substring(index, openBracketPos + 1));
                formatFrom(result, toFormat, openBracketPos + 2, arguments);
            } else {
                if (closeBracketPos == openBracketPos + 1) {
                    throw new FormatterException("Argument without index");
                }
                if (closeBracketPos == toFormat.length()) {
                    throw new FormatterException("Invalid brackets");
                }
                result.append(toFormat.substring(index, openBracketPos));
                getField(result, toFormat.substring(openBracketPos + 1, closeBracketPos), arguments);
                formatFrom(result, toFormat, closeBracketPos + 1, arguments);
            }
        } else {
            if (closeBracketPos < toFormat.length() - 1 &&  toFormat.charAt(closeBracketPos + 1) == '}') {
                result.append(toFormat.substring(index, closeBracketPos + 1));
                formatFrom(result, toFormat, closeBracketPos + 2, arguments);
            } else {
                throw new FormatterException("Invalid brackets");
            }
        }
    }

    private void getField(StringBuilder result, String toFormat,
            Object... arguments) throws FormatterException {
        Object tempObject = null;
        if (arguments == null) {
            return;
        }
        int patternBegin = toFormat.indexOf(":");
        if (patternBegin == -1) {
            patternBegin = toFormat.length();
        }
        StringTokenizer tokens = new StringTokenizer(toFormat.substring(0, patternBegin), ".");
        String newToken = tokens.nextToken();
        if (newToken.charAt(0) == '-' || newToken.charAt(0) == '+') {
            throw new FormatterException("Index of argument is invalid: " + newToken);
        }
        int argNum = -1;
        try {
            argNum = Integer.parseInt(newToken);
        } catch (Throwable e) {
            throw new FormatterException(e.getMessage());
        }
        if (argNum >= arguments.length) {
            tempObject = null;
        } else {
            try {
                tempObject = arguments[argNum];
            } catch (Throwable e) {
                throw new FormatterException(e.getMessage());
            }
            while (tokens.hasMoreTokens()) {
                tempObject = getFieldFrom(tempObject, tokens.nextToken());
            }
        }
        if (tempObject == null) {
            return;
        }
        if (patternBegin == toFormat.length()) {
            result.append(tempObject);
        } else {
            StringFormatterExtension goodExtension = null;
            for (StringFormatterExtension ext : extentions) {
                if (ext.supports(tempObject.getClass())) {
                    goodExtension = ext;
                    break;
                }
            }
            if (goodExtension != null) {
                goodExtension.format(result, tempObject, toFormat.substring(patternBegin + 1));
            } else {
                throw new FormatterException("No suitable extesion : " + tempObject.getClass());
            }
        }
        if (tempObject == null) {
            return;
        }
    }

    private Object getFieldFrom(Object tempObject, String field) throws FormatterException {
        try {
            if (tempObject == null) { 
                return null;
            }
            Class<?> parentClass = tempObject.getClass();
            while (parentClass != null) {
                try {
                    Field f = parentClass.getDeclaredField(field);
                    f.setAccessible(true);
                    return f.get(tempObject);
                } catch (NoSuchFieldException e) {
                    parentClass = parentClass.getSuperclass();
                    continue;
                }
            }
            return null;
        } catch (Throwable e) {
            throw new FormatterException("Cannot get access to the field " + field);
        }
    }
}
