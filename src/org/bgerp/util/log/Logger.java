package org.bgerp.util.log;

/**
 * Inherited Logger for access the parent's constructor only.
 *
 * @author Shamil Vakhitov
 */
public class Logger extends org.apache.log4j.Logger {
    Logger(String name) {
        super(name);
    }
}
