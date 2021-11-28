package com.example.binanceparser.util;
public class ExceptionFormatter {
    public static String formatThrowable(Throwable t) {
        return appendThrowable(new StringBuilder(), t).toString();
    }


    public static StringBuilder appendThrowable(StringBuilder builder, Throwable ex) {
        builder.append("EX(")
                .append(simpize(ex.getClass().getSimpleName(), 1))
                .append(":").append(ex.getMessage()).append("|");
        appendCompactTrace(builder, ex.getStackTrace()); //todo cause
        builder.append(") ");
        return builder;
    }


    public static void appendCompactTrace(StringBuilder builder, StackTraceElement[] elements) {
        for (int i = 0, elementsLength = Math.min(elements.length, 9); i < elementsLength; i++) {
            StackTraceElement element = elements[i];

            String simpize = simpize(element.getClassName(), 0);
            if (simpize.equals("Debug")) continue;

            builder.append(simpize).append(".").append(element.getMethodName()).append(":")
                    .append(element.getLineNumber()).append(" ");
        }
    }


    public static String simpize(String name, int ci) {
        int of = name.lastIndexOf('.');
        if (of > 0) name = name.substring(of + 1);
        String[] split = name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        StringBuilder b = new StringBuilder();
        for (int i = 0, splitLength = split.length; i < splitLength; i++) {
            if (i == ci) b.append(split[i]);
            else b.append(split[i].charAt(0));
        }
        return b.toString();
    }

}