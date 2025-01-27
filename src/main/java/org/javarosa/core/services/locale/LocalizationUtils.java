/**
 *
 */
package org.javarosa.core.services.locale;

import org.javarosa.core.util.OrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author ctsims
 *
 */
public class LocalizationUtils {
    private static final Logger logger = LoggerFactory.getLogger(LocalizationUtils.class);

    /**
     * @param is A path to a resource file provided in the current environment
     *
     * @return a dictionary of key/value locale pairs from a file in the resource directory
     * @throws IOException
     */
    public static OrderedMap<String, String> parseLocaleInput(InputStream is) throws IOException {
        // TODO: This might very well fail. Best way to handle?
        OrderedMap<String, String> locale = new OrderedMap<>();
        int chunk = 100;
        InputStreamReader isr;
        isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        boolean done = false;
        char[] cbuf = new char[chunk];
        int offset = 0;
        int curline = 0;

        String line = "";
        while (!done) {
            int read = isr.read(cbuf, offset, chunk - offset);
            if (read == -1) {
                done = true;
                if (line.length() != 0) {
                    parseAndAdd(locale, line, curline);
                }
                break;
            }
            String stringchunk = String.valueOf(cbuf, offset, read);

            int index = 0;

            while (index != -1) {
                int nindex = stringchunk.indexOf('\n', index);
                //UTF-8 often doesn't encode with newline, but with CR, so if we
                //didn't find one, we'll try that
                if (nindex == -1) {
                    nindex = stringchunk.indexOf('\r', index);
                }
                if (nindex == -1) {
                    line += stringchunk.substring(index);
                    break;
                } else {
                    line += stringchunk.substring(index, nindex);
                    //Newline. process our string and start the next one.
                    curline++;
                    parseAndAdd(locale, line, curline);
                    line = "";
                }
                index = nindex + 1;
            }
        }
        is.close();
        return locale;
    }

    private static void parseAndAdd(OrderedMap<String, String> locale, String line, int curline) {
        line = line.trim();

        //clear comments
        while (line.contains("#")) {
            line = line.substring(0, line.indexOf("#"));
        }
        if (line.indexOf('=') != -1) {
            //Check to see if there's anything after the '=' first. Otherwise there
            //might be some big problems.
            if (line.indexOf('=') != line.length() - 1) {
                String value = line.substring(line.indexOf('=') + 1);
                locale.put(line.substring(0, line.indexOf('=')), value);
            } else {
                logger.info("Invalid line (#{}) read: '{}'. No value follows the '='.", curline, line);
            }
        }
    }
}