package org.bgerp.plugin.msg.email;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;

import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.model.BGMessageException;

public class AddressesTest {
    @Test
    public void testParseSafe() throws Exception {
        Addresses expected = new Addresses(Map.of(
            RecipientType.TO, List.of(new InternetAddress("test@bgerp.org"))
        ));
        Assert.assertEquals(expected, Addresses.parseSafe("test@bgerp.org"));

        expected = new Addresses(Map.of(
            RecipientType.TO, List.of(new InternetAddress("test@bgerp.org")),
            RecipientType.CC, List.of(new InternetAddress("test1@bgerp.org"))
        ));
        Assert.assertEquals(expected, Addresses.parseSafe("test@bgerp.org; CC: test1@bgerp.org"));

        expected = new Addresses(Map.of(
            RecipientType.TO, List.of(new InternetAddress("test@bgerp.org")),
            RecipientType.CC, List.of(new InternetAddress("test1@bgerp.org"))
        ));
        Assert.assertEquals(expected, Addresses.parseSafe("test@bgerp.org, CC: test1@bgerp.org"));

        expected = new Addresses(Map.of(
            RecipientType.TO, List.of(new InternetAddress("test@bgerp.org")),
            RecipientType.CC, List.of(new InternetAddress("test1@bgerp.org"), new InternetAddress("test2@bgerp.org"))
        ));
        Assert.assertEquals(expected, Addresses.parseSafe("test@bgerp.org, CC: test1@bgerp.org, test2@bgerp.org"));

        expected = new Addresses(Map.of(
            RecipientType.CC, List.of(new InternetAddress("test2@bgerp.org")),
            RecipientType.BCC, List.of(new InternetAddress("test3@bgerp.org"))
        ));
        Assert.assertEquals(expected, Addresses.parseSafe("CC: test2@bgerp.org, BCC: test3@bgerp.org"));

        expected = new Addresses(Map.of(
            RecipientType.TO, List.of(new InternetAddress("test@bgerp.org"))
        ));
        Assert.assertEquals(expected, Addresses.parseSafe("test@bgerp.org, ddd"));

        expected = new Addresses(Map.of());
        Assert.assertEquals(expected, Addresses.parseSafe("XX: test@bgerp.org; ddd"));

        expected = new Addresses(Map.of());
        Assert.assertEquals(expected, Addresses.parseSafe("XX: test@bgerp.org, ddd"));
    }

    @Test
    public void testParse() throws Exception {
        Addresses expected = new Addresses(Map.of());
        Assert.assertEquals(expected, Addresses.parse(""));

        expected = new Addresses(Map.of(
            RecipientType.TO, List.of(new InternetAddress("test@bgerp.org")),
            RecipientType.CC, List.of(new InternetAddress("test1@bgerp.org"))
        ));
        Assert.assertEquals(expected, Addresses.parse("test@bgerp.org; CC: test1@bgerp.org"));

        BGMessageException exception = null;
        try {
            Addresses.parse("ddd");
        } catch (BGMessageException e) {
            exception = e;
        }
        Assert.assertEquals(new BGMessageException("Incorrect email: {}", "ddd"), exception);

        exception = null;
        try {
            Addresses.parse("test@bgerp.org, ddd");
        } catch (BGMessageException e) {
            exception = e;
        }
        Assert.assertEquals(new BGMessageException("Incorrect email: {}", "ddd"), exception);

        exception = null;
        try {
            Addresses.parse("XX: test@bgerp.org; ddd");
        } catch (BGMessageException e) {
            exception = e;
        }
        Assert.assertEquals(new BGMessageException("Incorrect prefix: {}", "XX"), exception);

        exception = null;
        try {
            Addresses.parse("XX: test@bgerp.org, ddd");
        } catch (BGMessageException e) {
            exception = e;
        }
        Assert.assertEquals(new BGMessageException("Incorrect prefix: {}", "XX"), exception);
    }

    @Test
    public void testAddTo() throws Exception {
        Addresses expected = new Addresses(Map.of(
            RecipientType.TO, List.of(new InternetAddress("test@bgerp.org"), new InternetAddress("test4@bgerp.org")),
            RecipientType.CC, List.of(new InternetAddress("test1@bgerp.org"), new InternetAddress("test2@bgerp.org"))
        ));
        Assert.assertEquals(expected, Addresses.parse("test@bgerp.org; CC: test1@bgerp.org, test2@bgerp.org").addTo("test4@bgerp.org"));
    }

    @Test
    public void testExclude() throws Exception {
        Addresses expected = new Addresses(Map.of(
            RecipientType.TO, List.of(),
            RecipientType.CC, List.of(new InternetAddress("test2@bgerp.org"))
        ));
        Assert.assertEquals(expected, Addresses.parse("test@bgerp.org; CC: test@bgerp.org, test2@bgerp.org").exclude("test@bgerp.org"));
    }

    @Test
    public void testSerialize() throws Exception {
        Assert.assertEquals("test@bgerp.org", new Addresses(Map.of(
            RecipientType.TO, List.of(new InternetAddress("test@bgerp.org"))
        )).serialize());

        Assert.assertEquals("test@bgerp.org, CC: test1@bgerp.org, test2@bgerp.org", new Addresses(Map.of(
            RecipientType.TO, List.of(new InternetAddress("test@bgerp.org")),
            RecipientType.CC, List.of(new InternetAddress("test1@bgerp.org"), new InternetAddress("test2@bgerp.org"))
        )).serialize());

        Assert.assertEquals("CC: test2@bgerp.org, BCC: test3@bgerp.org", new Addresses(Map.of(
            RecipientType.CC, List.of(new InternetAddress("test2@bgerp.org")),
            RecipientType.BCC, List.of(new InternetAddress("test3@bgerp.org"))
        )).serialize());
    }

    @Test
    public void testRecipients() throws Exception {
        var expected = new LinkedHashMap<RecipientType, List<InternetAddress>>();

        expected.clear();
        expected.put(RecipientType.TO, List.of(new InternetAddress("test@bgerp.org")));
        expected.put(RecipientType.CC, List.of(new InternetAddress("test1@bgerp.org")));
        expected.put(RecipientType.BCC, List.of(new InternetAddress("test2@bgerp.org"), new InternetAddress("test3@bgerp.org")));
        Assert.assertEquals(expected, Addresses.parse("test@bgerp.org; CC: test1@bgerp.org; BCC: test2@bgerp.org, test3@bgerp.org"));

        expected.clear();
        expected.put(RecipientType.TO, List.of(new InternetAddress("test@bgerp.org"), new InternetAddress("test2@bgerp.org")));
        expected.put(RecipientType.BCC, List.of(new InternetAddress("test2@bgerp.org"), new InternetAddress("test3@bgerp.org")));
        Assert.assertEquals(expected, Addresses.parse("test@bgerp.org, test2@bgerp.org; BCC: test2@bgerp.org, test3@bgerp.org"));
    }
}
