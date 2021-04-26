package org.bgerp;

import javax.servlet.http.HttpServletRequest;

import org.bgerp.servlet.filter.OpenFilter;

import ru.bgcrm.servlet.filter.AuthFilter;
import ru.bgcrm.util.Utils;

/**
 * Types of user interfaces.
 * 
 * @author Shamil Vakhitov
 */
public class Interface {
    public static final String USER = "user";
    public static final String OPEN = "open";
    public static final String USER_MOB = "usermob";

    /**
     * Detects requests interface: USER, OPEN or 'undef'
     */
    public static String getIface(HttpServletRequest request) {
        var uri = OpenFilter.getRequestURI(request);
        var user = AuthFilter.getUser(request);

        if (Utils.notBlankString(uri))
            return OPEN;
        if (user != null)
            return USER;
        
        return "undef";
    }
}
