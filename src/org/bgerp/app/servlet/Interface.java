package org.bgerp.app.servlet;

import javax.servlet.http.HttpServletRequest;

import org.bgerp.app.cfg.Setup;
import org.bgerp.app.servlet.filter.AuthFilter;
import org.bgerp.app.servlet.filter.OpenFilter;

import ru.bgcrm.util.Utils;

/**
 * Types of user interfaces
 *
 * @author Shamil Vakhitov
 */
public class Interface {
    public static final String USER = "user";
    public static final String OPEN = "open";
    public static final String USER_MOB = "usermob";

    /**
     * @return detected request interface: {@link #USER}, {@link #OPEN}, {@link #USER_MOB} or {@code "undef"}
     */
    public static String getIface(HttpServletRequest request) {
        var uri = OpenFilter.getRequestURI(request);
        var user = AuthFilter.getUser(request);

        if (Utils.notBlankString(uri))
            return OPEN;

        if (user != null) {
            if (request.getRequestURI().contains("/" + USER_MOB))
                return USER_MOB;
            return USER;
        }

        return "undef";
    }

    /**
     * @return the {@code url.user} parameter value from {@link org.bgerp.app.cfg.Setup}, or {@code /user} by default
     */
    public static String getUrlUser() {
        return Setup.getSetup().get("url.user", URL() + "/user");
    }

    /**
     * @return the {@code url.open} parameter value from {@link org.bgerp.app.cfg.Setup}, or {@code /open} by default
     */
    public static String getUrlOpen() {
        return Setup.getSetup().get("url.open", URL() + "/open");
    }

    /**
     * @return the {@code URL} parameter value, or '' by default
     */
    private static String URL() {
        return Setup.getSetup().get("URL", "");
    }
}
