package org.bgerp.plugin.sec.auth.config;

import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import org.bgerp.app.cfg.SimpleConfigMap;
import org.junit.Assert;
import org.junit.Test;

import javassist.NotFoundException;

public class LdapAuthConfigTest {
    @Test
    public void testAuth() throws Exception {
        var config = SimpleConfigMap.of(
            "url", "ldap://172.16.0.45:389",
            "login.expression", "login + \"@ozna\"",
            "search.base", "dc=ozna,dc=corp",
            "search.expression", "\"sAMAccountName=\" + login",
            "group.ids.expression",
                "result = {};\n" +
                "for (memberOf : attrs.values(\"memberOf\")) {\n" +
                "    memberOfLc = memberOf.toLowerCase();\n" +
                "    if (memberOfLc.startsWith(\"cn=vpn koronavirus ozna,ou=rdp-limit\")) {\n" +
                "       result.add(10);\n" +
                "    } else if (memberOfLc.startsWith(\"cn=пользователи vpn-озна,ou=rdp-limit\")) {\n" +
                "       result.add(12);\n" +
                "    }\n" +
                "}\n" +
                "return result;"
        );

        boolean[] loginOk = { false };
        boolean[] passwordOk = { false };
        boolean[] searchFilterOk = { false };

        var auth = new LdapAuthConfig(0, config) {
            @Override
            protected Attributes searchAttributes(String login, String password, String searchFilter)
                    throws NamingException, NotFoundException {
                loginOk[0] = "bgrptest".equals(login);
                passwordOk[0] = "HerWam".equals(password);
                searchFilterOk[0] = "sAMAccountName=bgrptest".equals(searchFilter);

                var attrs = new BasicAttributes();

                var attr = new BasicAttribute("memberOf");
                attr.add("CN=VPN Koronavirus OZNA,OU=RDP-LIMIT,DC=ozna,DC=corp");
                attr.add("CN=Пользователи VPN-ОЗНА,OU=RDP-LIMIT,DC=ozna,DC=corp");
                attrs.put(attr);

                attr = new BasicAttribute("name");
                attr.add("New Пользователь");
                attrs.put(attr);

                return attrs;
            }
        };

        var result = auth.auth("bgrptest","HerWam");
        Assert.assertTrue("No success", result.isSuccess());
        var user = result.getUser();
        Assert.assertNotNull("User is null", user);
        Assert.assertEquals("Wrong group IDs", Set.of(10, 12), user.getGroupIds());
        Assert.assertEquals("Wrong title", "New Пользователь", user.getTitle());
        Assert.assertTrue("Wrong login used", loginOk[0]);
        Assert.assertTrue("Wrong password used", passwordOk[0]);
        Assert.assertTrue("Wrong searchFilter user", searchFilterOk[0]);
    }
}
