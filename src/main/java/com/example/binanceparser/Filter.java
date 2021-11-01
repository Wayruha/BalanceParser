package com.example.binanceparser;

import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class Filter {

    //1. Not a good idea to build json manually in order to de-serialize it later. However, let it be like that for now
    //2. Too specific. For example, it would be easier to understand it having signature like 'AbstractEvent *name*(String logLine) {...'
    //    basically, it could be responsible for converting a LINE to Event instance
    public static String fromPlainToJson(String[] plainTextArray) {
        String resultString = "";
        for (String string : plainTextArray) {
            StringBuilder sb = new StringBuilder().append(string.replace('=', ':'));
            boolean emptyArray = false;

            if (Objects.equals(plainTextArray[0], string)) {
                sb.delete(0, sb.lastIndexOf("[") + 1);
                sb.insert(0, "{");
                sb.insert(sb.length(), "\"");
            }
            else if(string.contains("[]")){
                sb.replace(sb.indexOf("]"), sb.lastIndexOf("]") + 1, "]");
                emptyArray = true;
            }
            else if (string.contains("]}]")) {
                sb.replace(string.indexOf(']'), string.lastIndexOf(']') + 1, "\"}]");
            } else if (string.contains("[")) {
                if (string.contains("{")) {
                    sb.replace(sb.indexOf("{"), sb.indexOf("[") + 1, "[{\"");
                    sb.insert(sb.indexOf(":"), "\"");
                } else sb.replace(0, string.indexOf("[") + 1, "{");
                sb.insert(sb.length(), "\"");
            } else if (string.contains("]}")) {
                sb.replace(string.indexOf("]"), string.indexOf("}") + 1, "\"}]");
            } else if (string.contains("]")) {
                sb.replace(string.indexOf("]"), string.indexOf("]") + 1, "\"}");
            }
            else if(string.contains("true") || string.contains("false")) {
                resultString += booleanValue(sb, sb.lastIndexOf(":"));
                continue;
            }
            else sb.insert(sb.length(), "\"");
            int equalsChar = sb.lastIndexOf(":");

            sb.insert(equalsChar, '"');
            if(!emptyArray) sb.insert(equalsChar + 2, '"');
            if (sb.toString().startsWith("{")) sb.insert(1, "\"");
            else sb.insert(0, "\"");
            if (!string.equals(plainTextArray[plainTextArray.length - 1])) sb.insert(sb.length(), ',');
            resultString += sb;
        }
        return resultString;
    }

    public static StringBuilder booleanValue(StringBuilder logString, int equalsChar) {
        logString.insert(equalsChar, '"');
        logString.insert(0, "\"");
        if(String.valueOf(logString).contains("true")){
            logString.replace(equalsChar + 3, logString.length(),"1");
        }
        else logString.replace(equalsChar + 3, logString.length(),"0");

        logString.insert(logString.length(), ',');

        return logString;
    }
    }


