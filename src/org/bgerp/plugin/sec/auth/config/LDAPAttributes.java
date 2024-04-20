package org.bgerp.plugin.sec.auth.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import ru.bgcrm.util.Utils;

/**
 * Supporting class for handling LDAP attributes.
 */
public class LDAPAttributes {
    private final Attributes attrs;

    LDAPAttributes(Attributes attrs) {
        this.attrs = attrs;
    }

    /**
     * Single attribute value.
     * @param name name of attribute.
     * @return first value of the attribute with {@code name} or {@code null} if no presented.
     * @throws NamingException
     */
    public String value(String name) throws NamingException {
        return Utils.getFirst(values(name));
    }

    /**
     * List of attribute values.
     * @param name name of attribute.
     * @return unmodifiable list of values, never {@code null}.
     * @throws NamingException
     */
    public List<String> values(String name) throws NamingException {
        var result = new ArrayList<String>();

        Attribute attr = attrs.get(name);
        if (attr != null) {
            for (Enumeration<?> e = attr.getAll(); e.hasMoreElements();) {
                String value = e.nextElement().toString();
                result.add(value);
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public String toString() {
        return attrs.toString();
    }
}