package org.javarosa.xpath;

/**
 * An exception detailing a function call that was provided the incorrect
 * number of arguments.
 *
 * Created by wpride1 on 3/28/15.
 */
public class XPathArityException extends XPathException {

    /**
     * An exception detailing a function call that was provided the incorrect
     * number of arguments.
     *
     * @param funcName      name of function that was called with incorrect number
     *                      of arguments
     * @param expectedArity number of arguments expected for this function call
     * @param providedArity number of arguments provided for this function call
     */
    public XPathArityException(String funcName, int expectedArity, int providedArity) {
        super("The " + funcName +
                " function was provided the incorrect number of arguments:" + providedArity +
                ". It expected " + expectedArity + " arguments.");
    }
}