package org.bgerp;

import javax.servlet.http.HttpServletRequest;

import org.bgerp.servlet.filter.AuthFilter;
import org.bgerp.servlet.filter.OpenFilter;

import ru.bgcrm.util.Setup;
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
     * Detects request interface.
     * @return {@link #USER}, {@link #OPEN} or {@code "undef"}
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

    /**
     * Parameter {@code url.user} from {@link ru.bgcrm.util.Setup}.
     * @return parameter value or {@code /user}.
     */
    public static String getUrlUser() {
        return Setup.getSetup().get("url.user", URL() + "/user");
    }

    /**
     * Parameter {@code url.open} from {@link ru.bgcrm.util.Setup}.
     * @return parameter value or {@code /open}.
     */
    public static String getUrlOpen() {
        return Setup.getSetup().get("url.open", URL() + "/open");
    }

    /**
     * URL parameter value.
     * @return value or ''.
     */
    private static String URL() {
        return Setup.getSetup().get("URL", "");
    }
}
