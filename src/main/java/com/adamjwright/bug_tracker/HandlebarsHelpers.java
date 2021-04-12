/******************************************************************************
**  File of functions that will be called by the Handlebars templating engine
******************************************************************************/

package com.adamjwright.bug_tracker;

public class HandlebarsHelpers {

    // Compare equality of string and number
    public static String eq_str_num(String a, Integer b) {
        // Test for null parameters
        if (a == null || b == null) {
            return "";
        }
        
        int aToInt = Integer.parseInt(a);

        if (aToInt == b) {
            return "true";
        }
        else {
            return "";
        }
    }

    // Return whether arg a is larger than b
    public static String greater(Integer a, Integer b) {
        // Test for null parameters
        if (a == null || b == null) {
            return "";
        }

        if (a > b) {
            return "true";
        }
        else {
            return "";
        }
    }

    // Increment the incomming value
    public static String inc(String size) {
        // Test for null parameters
        if (size == null) {
            return "";
        }

        int sizeInt = Integer.parseInt(size);
        sizeInt++;

        return String.valueOf(sizeInt);
    }

    // Decrement the incomming value
    public static String dec(String size) {
        // Test for null parameters
        if (size == null) {
            return "";
        }

        int sizeInt = Integer.parseInt(size);
        sizeInt--;

        return String.valueOf(sizeInt);
    }

}
