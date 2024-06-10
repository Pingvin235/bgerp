package org.bgerp.app.dist.lic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.action.admin.LicenseAction;
import org.bgerp.action.base.Actions;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.servlet.user.LoginStat;
import org.bgerp.event.client.LicenseEvent;
import org.bgerp.model.Message;
import org.bgerp.util.TimeConvert;

import ru.bgcrm.model.user.User;
import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * License for plugins.
 *
 * @author Shamil Vakhitov
 */
public class License {
    /**
     * Accepted signature.
     */
    private static final Sign SIGN = new Sign("Team <team@bgerp.org>", "AAAAB3NzaC1yc2EAAAADAQABAAABAQDE4LeZHn/rW/J5fX52ozR2B+wwxEqfE9lhkZDmG3wCCtGNXzxFpQXVRROHi0FwSZAnQXLvTwMH1Lw54SBxPbEk3f35B3ULorIzibMwokzdD6daJdmI9nq4fm7FcpnM8Wv81RvRbKjBFQz1waJLiALpTxBSOrgbFFM6jilgv9fSEJNsz2c/sh/TlMxa5XlHhwutdp6qip2QyTngD8oq1ZNtHqzx3kI/tj2L+fRZEhWZD2Fj9oWKs9uiS+G4Gzsty2bA6hHYMyDdzFKUvN3I9Lj9NF2ZeLalsen5zaoHh5kzPyzCymZfVupu6M1DQqBtE5rQgY+JWXDegBYzRawiwzpx");

    public static final String FILE_NAME = "lic.data";

    private static final String KEY_LIC = "lic.";
    public static final String KEY_LIC_ID = KEY_LIC + "id";
    public static final String KEY_LIC_EMAIL = KEY_LIC + "email";
    public static final String KEY_LIC_LIMIT = KEY_LIC + "limit";
    public static final String KEY_LIC_DATE_TO = KEY_LIC + "date.to";
    public static final String KEY_LIC_PLUGIN = KEY_LIC + "plugin.";
    private static final int KEY_LIC_PLUGIN_LENGTH = KEY_LIC_PLUGIN.length();
    public static final String KEY_LIC_SIGN = KEY_LIC + "sign";

    private static final int CHECK_EXPIRATION_DAYS_BEFORE = 3;
    private static final int CHECK_EXPIRATION_NOTIFICATION_RANDOM_BOUND = 200;

    private static final int CHECK_ERROR_NOTIFICATION_RANDOM_BOUND = 100;

    private final LocalDate created = LocalDate.now();
    private final String data;
    private final ConfigMap config;
    /** Session limit, 0 - no limit. */
    private final int limit;
    private final Date dateTo;
    private final byte[] digest;
    /** The error must not be localized! */
    private final String error;
    private final Set<String> plugins;

    public License(String data) {
        this.data = data;
        this.config = new Preferences(data);
        this.limit = config.getInt(KEY_LIC_LIMIT);
        this.dateTo = TimeUtils.parse(config.get(KEY_LIC_DATE_TO), TimeUtils.PATTERN_DDMMYYYY);
        this.digest = digest();
        this.error = error();
        this.plugins = plugins();
    }

    /**
     * License content.
     * @return
     */
    public String getData() {
        return data;
    }

    /**
     * Digest for all the license's lines before lic.sign.signature.
     * @return
     */
    public byte[] getDigest() {
        return digest;
    }

    /**
     * License check result.
     * @return {@code null} on correct result, or error text.
     */
    public String getError() {
        return error;
    }

    /**
     * Plugin IDs.
     * @return
     */
    public Set<String> getPlugins() {
        return plugins;
    }

    /**
     * License content with signature on the end.
     * @param keyFilePath file of Java resource path to SSH private key file.
     * @param keyFilePswd password to SSH private key file, {@code null} - no password is used.
     * @return UTF-8 encoded signed license.
     */
    public byte[] sign(String keyFilePath, String keyFilePswd) throws Exception {
        var sign = new Sign("key.id", new String(org.bgerp.util.IOUtils.read(keyFilePath), StandardCharsets.UTF_8), keyFilePswd);

        var data = new StringBuilder(this.data);
        data.append(KEY_LIC_SIGN + "=")
            .append(sign.signatureGenerate(getDigest()))
            .append("\n");

        return data.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Adds {@link LicenseEvent} to {@code form} response case the license has error.
     * @param form
     */
    public void check(DynActionForm form) {
        // do not handle JSP includes, when form.l is null && feature flag
        if (form.l == null || !isCheckEnabled())
            return;

        // login action or open interface
        User user = form.getUser();
        if (user == null)
            return;

        final boolean actionAllowed = user.checkPerm(Actions.getByClass(LicenseAction.class).getId() + ":null");

        // notification
        if (Utils.isBlankString(error)) {
            // send the notification only to users with allowed action
            if (!actionAllowed ||
                CHECK_EXPIRATION_DAYS_BEFORE < ChronoUnit.DAYS.between(LocalDate.now(), TimeConvert.toLocalDate(dateTo)))
                return;

            if (Setup.getSetup().getBoolean("test.license.check.notification") || new Random().nextInt(CHECK_EXPIRATION_NOTIFICATION_RANDOM_BOUND) == 0) {
                form.getResponse().addEvent(
                    new LicenseEvent(new Message(form.l.l("License Will Expire Soon"),
                        form.l.l("Your license will expire at {}", TimeUtils.format(dateTo, TimeUtils.FORMAT_TYPE_YMD))),
                        true));
            }
        }
        // error
        else if (Setup.getSetup().getBoolean("test.license.check.error") || new Random().nextInt(CHECK_ERROR_NOTIFICATION_RANDOM_BOUND) == 0) {
            form.getResponse().addEvent(
                new LicenseEvent(new Message(form.l.l("License Check Error"), error),
                    actionAllowed));
        }
    }

    /**
     * Checks if count of logged in users is less that license limit.
     * When {@link #error} is not blank, returns {@code true} to allow fix the license.
     * Return {@code true} when license check is disabled.
     * @return allowance to log in.
     */
    public boolean checkSessionLimit() {
        if (!isCheckEnabled() || Utils.notBlankString(error) || limit == 0)
            return true;

        return LoginStat.instance().loggedUsers().size() < limit;
    }

    /**
     * @return is the license object created today.
     */
    public boolean isCreatedToday() {
        return ChronoUnit.DAYS.between(created, LocalDate.now()) == 0;
    }

    private boolean isCheckEnabled() {
        return true;
    }

    private byte[] digest() {
        var buffer = new StringBuilder(1000);

        try (var scanner = new Scanner(data)) {
            while (scanner.hasNextLine()) {
                var line = scanner.nextLine();
                if (line.startsWith(KEY_LIC_SIGN))
                    break;
                buffer.append(line);
            }
        }

        try {
            var digest = MessageDigest.getInstance("SHA-512");
            return digest.digest(buffer.toString().getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Digest exception", e);
        }
    }

    private String error() {
        if (Utils.isBlankString(data))
            return "License is missing";

        var signature = config.get(KEY_LIC_SIGN);

        if (Utils.isEmptyString(signature))
            return "Signature is undefined";

        if (!SIGN.signatureVerify(digest, signature))
            return "Signature is not correct";

        if (dateTo == null || TimeUtils.dateBefore(dateTo, new Date()))
            return "Date To is not defined or expired";

        final Set<String> plugins = plugins();

        Set<String> missing = PluginManager.getInstance().getPluginList().stream()
            .filter(p -> !p.isSystem() && !p.getId().startsWith("custom."))
            .map(Plugin::getId)
            .filter(id -> !plugins.contains(id))
            .collect(Collectors.toSet());

        if (!missing.isEmpty())
            return "Missing plugins: " + Utils.toString(missing);

        return null;
    }

    /**
     * @return license plugins IDs + kernel.
     */
    private Set<String> plugins() {
        var result = new HashSet<String>();

        for (var me : config.entrySet()) {
            var key = me.getKey();
            if (!key.startsWith(KEY_LIC_PLUGIN) || !Utils.parseBoolean(me.getValue(), false))
                continue;

            result.add(key.substring(KEY_LIC_PLUGIN_LENGTH));
        }

        return Collections.unmodifiableSet(result);
    }

    @Override
    public String toString() {
        return "License [created=" + created + "]";
    }
}
