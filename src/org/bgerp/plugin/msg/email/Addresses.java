package org.bgerp.plugin.msg.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.bgerp.l10n.Localization;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import com.google.common.annotations.VisibleForTesting;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.util.Utils;

/**
 * EMail addresses recipients parser and serializer.
 * Supported recipient types: {@code TO}, {@code CC}, {@code BCC}.
 *
 * @author Shamil Vakhitov
 */
@Dynamic
public class Addresses extends HashMap<RecipientType, List<InternetAddress>> {
    private static final Log log = Log.getLog();

    private static final RecipientType[] RECIPIENT_TYPES = new RecipientType[] { RecipientType.TO, RecipientType.CC, RecipientType.BCC };

    private static final String RECIPIENT_TYPE_PREFIX_CC = RecipientType.CC.toString().toUpperCase();
    private static final String RECIPIENT_TYPE_PREFIX_BCC = RecipientType.BCC.toString().toUpperCase();

    public Addresses() {
        super();
    }

    @Dynamic
    public Addresses(String addresses) {
        super();
        try {
            parse(addresses, true);
        } catch (BGMessageException e) {
            throw new IllegalStateException("The exception must not be thrown here", e);
        }
    }

    @VisibleForTesting
    Addresses(Map<RecipientType, List<InternetAddress>> addresses) {
        super(addresses);
    }

    /**
     * Parses email addresses.
     * Actual format: <pre>to1, to2..,CC: cc1, cc2..,BCC: bcc1, bcc2..</pre>
     * Old one: <pre>to1, to2..,CC: cc1, cc2..,BCC: bcc1, bcc2..</pre>
     * @param addresses email addresses string.
     * @return a created instance.
     * @throws BGMessageException incorrect email was given in {@code addresses} and {@code silent} is {@code false}.
     */
    static Addresses parse(String addresses) throws BGMessageException {
        return new Addresses().parse(addresses, false);
    }

    /**
     * Parses email addresses.
     * Actual format: <pre>to1, to2..,CC: cc1, cc2..,BCC: bcc1, bcc2..</pre>
     * Old one: <pre>to1, to2..,CC: cc1, cc2..,BCC: bcc1, bcc2..</pre>
     * @param addresses email addresses string.
     * @return a created instance.
     */
    static Addresses parseSafe(String addresses) {
        try {
            return new Addresses().parse(addresses, true);
        } catch (BGMessageException e) {
            throw new IllegalStateException("The exception must not be thrown here", e);
        }
    }

    private Addresses parse(String addresses, boolean silent) throws BGMessageException {
        if (Utils.isEmptyString(addresses))
            return this;

        RecipientType type = RecipientType.TO;
        for (String token : addresses.split("\\s*[,;]\\s*")) {
            int pos = token.indexOf(':');

            String prefix = null;
            if (pos > 0) {
                prefix = token.substring(0, pos);
                token = token.substring(pos + 1);

                if (prefix.equalsIgnoreCase(RECIPIENT_TYPE_PREFIX_CC))
                    type = RecipientType.CC;
                else if (prefix.equalsIgnoreCase(RECIPIENT_TYPE_PREFIX_BCC))
                    type = RecipientType.BCC;
                else {
                    if (silent) {
                        log.debug("Incorrect prefix: {}", prefix);
                        continue;
                    } else
                        throw new BGMessageException(Localization.getLocalizer(Localization.getSysLang(), Plugin.ID), "Incorrect prefix: {}", prefix);
                }
            }

            try {
                InternetAddress addr = InternetAddress.parse(token)[0];
                addr.validate();

                computeIfAbsent(type, unused -> new ArrayList<>()).add(addr);
            } catch (AddressException e) {
                if (silent)
                    log.debug("Incorrect email: {}", token);
                else
                    throw new BGMessageException(Localization.getLocalizer(Localization.getSysLang(), Plugin.ID), "Incorrect email: {}", token);
            }
        }

        return this;
    }

    /**
     * Appends an email as recipient {@code TO} address.
     * @param value the email.
     * @return
     */
    public Addresses addTo(String value) {
        try {
            computeIfAbsent(RecipientType.TO, unused -> new ArrayList<>(1)).add(new InternetAddress(value));
        } catch (AddressException e) {
            throw new IllegalStateException("The exception must not be thrown", e);
        }
        return this;
    }

    /**
     * Deletes an email from all the types of existing addresses.
     * @param value the email.
     * @return
     */
    public Addresses exclude(String value) {
        try {
            var address = InternetAddress.parse(value)[0];
            values().forEach(values -> values.remove(address));
        } catch (AddressException e) {
            throw new IllegalStateException("The exception must not be thrown", e);
        }
        return this;
    }

    @Dynamic
    public String serializeTo() {
        return serialize(RecipientType.TO);
    }

    @Dynamic
    public String serializeCc() {
        return serialize(RecipientType.CC);
    }

    @Dynamic
    public String serializeBcc() {
        return serialize(RecipientType.BCC);
    }

    private String serialize(RecipientType type) {
        var addresses = get(type);
        if (addresses == null)
            return "";
        return addresses.stream().map(InternetAddress::getAddress).collect(Collectors.joining(Utils.DEFAULT_DELIM));
    }

    /**
     * Serializes emails to a string like: <pre>to1, to2..,CC: cc1, cc2..,BCC: bcc1, bcc2..</pre>
     * @return the string.
     */
    String serialize() {
        StringBuilder result = new StringBuilder();

        for (RecipientType type : RECIPIENT_TYPES) {
            List<InternetAddress> addressList = get(type);
            if (addressList == null)
                continue;

            StringBuilder part = new StringBuilder();
            for (InternetAddress addr : addressList)
                Utils.addCommaSeparated(part, addr.getAddress());

            if (type != RecipientType.TO)
                part.insert(0, type.toString().toUpperCase() + ": ");

            Utils.addSeparated(result, ", ", part.toString());
        }

        return result.toString();
    }

    /**
     * Returns ordered recipients map. First {@code TO}, than {@code CC} and {@code BCC}.
     * @return
     */
    LinkedHashMap<RecipientType, InternetAddress[]> recipients() {
        LinkedHashMap<RecipientType, InternetAddress[]> result = new LinkedHashMap<>();

        for (RecipientType type : RECIPIENT_TYPES) {
            List<InternetAddress> addresses = get(type);
            if (addresses == null)
                continue;

            result.put(type, addresses.toArray(new InternetAddress[0]));
        }

        return result;
    }
}
