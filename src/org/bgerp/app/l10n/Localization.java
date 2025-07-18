package org.bgerp.app.l10n;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.servlet.filter.AuthFilter;
import org.bgerp.app.servlet.util.ServletUtils;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.Utils;

/**
 * A localization unit, loaded from l10n.xml for a plugin.
 *
 * @author Shamil Vakhitov
 */
public class Localization {
    private static final Log log = Log.getLog();

    public static final String FILE_NAME = "l10n.xml";

    public static final String LANG_SYS = "sys";
    public static final String LANG_RU = Lang.RU.getId();
    public static final String LANG_EN = Lang.EN.getId();
    public static final String LANG_DE = Lang.DE.getId();

    /** Custom localizations may be placed in the custom/l10n.xml file in the application's directory. */
    private static final String PLUGIN_CUSTOM = "custom";

    /** Localizations of plugins. */
    private static volatile Map<String, Localization> localizations;

    // end of static part
    private final String pluginId;
    private final Map<String, Map<String, String>> translations;

    private Localization(String pluginId, Document doc) {
        this.pluginId = pluginId;
        this.translations = new HashMap<>(300);
        parseFile(doc);
    }

    /**
     * Gets localization for the plugin.
     * @param p the plugin.
     * @return localization if {@link #FILE_NAME} exists for the plugin, or null if missing.
     */
    public static Localization getLocalization(Plugin p) {
        var doc = p.getXml(FILE_NAME, null);
        if (doc == null)
            return null;
        return new Localization(p.getId(), doc);
    }

    private void parseFile(Document doc) {
        for (Element phrase : XMLUtils.elements(doc.getDocumentElement().getChildNodes())) {
            Map<String, String> phraseMap = null;

            for (Element lang : XMLUtils.elements(phrase.getChildNodes())) {
                String value = XMLUtils.getElementText(lang);
                // first language - key
                if (phraseMap == null) {
                    phraseMap = new HashMap<>(4);
                    if (translations.containsKey(value))
                        log.warn("Duplicated key: '{}' in '{}' l10n.xml", value, this);
                    translations.put(value, phraseMap);
                }
                phraseMap.put(lang.getNodeName(), value);
            }
        }
    }

    @Override
    public String toString() {
        return pluginId;
    }

    /**
     * Translation for a phrase.
     * @param toLang target lang.
     * @param value phrase.
     * @return translation or null if missing.
     */
    String getTranslation(String toLang, String value) {
        return translations.containsKey(value) ? translations.get(value).get(toLang) : null;
    }

    /**
     * Retrieves a Localizer for a plugin, using request URI.
     * @param request plugin ID is got out of getRequestURI().
     * @return
     */
    public static Localizer getLocalizer(HttpServletRequest request) {
        return getLocalizer(getLang(request), getPluginIdsFromURI(ServletUtils.getRequestURI(request)));
    }

    /**
     * Retrieves plugin IDs as substring after '/plugin/' and the following '/'.
     * @param uri HTTP request URI.
     * @return {@code null} if no plugin ID was found, otherwise array with plugin IDs.
     */
    @VisibleForTesting
    static String[] getPluginIdsFromURI(String uri) {
        final String pluginUriPrefix = "/plugin/";
        final int pluginUriPrefixLength = pluginUriPrefix.length();

        List<String> pluginIds = new ArrayList<>();

        int pos = 0;
        while ((pos = uri.indexOf(pluginUriPrefix, pos + 1)) > 0)
            pluginIds.add(StringUtils.substringBefore(uri.substring(pos + pluginUriPrefixLength), "/"));

        return pluginIds == null ? null : pluginIds.toArray(new String[0]);
    }

    /**
     * Called from JSP.
     * Retrieves language from the request params or session.
     * Once defined lang has persisted in the session attribute as well.
     * If not found language is taken from configuration parameter 'lang'.
     * If not defined there - returned 'ru'.
     * @param request
     * @return
     */
    @Dynamic
    public static String getLang(HttpServletRequest request) {
        final String langKeyName = "lang";

        String result = null;

        // open interface, no user
        if (request != null && AuthFilter.getUser(request) == null) {
            result = request.getParameter(langKeyName);

            HttpSession session = request.getSession(false);
            if (Utils.isBlankString(result) && session != null)
                result = (String) session.getAttribute(langKeyName);

            request.getSession().setAttribute(langKeyName, result);
        }

        // user interface or not defined in session / params
        if (Utils.isBlankString(result))
            result  = Setup.getSetup().get(langKeyName, LANG_EN);

        return result;
    }

    /**
     * Default UI language for open interface and the only one for others.
     * Calls {@link #getLang(HttpServletRequest)} with 'null'.
     * @return
     */
    public static final String getLang() {
        return getLang(null);
    }

    /**
     * Old version of {@link #getLang()}.
     * @return
     */
    @Deprecated
    public static final String getSysLang() {
        log.warndMethod("getSysLang", "getLang");
        return getLang();
    }

    /**
     * Retrieve localizer for a plugin.
     * The localizer includes the following localizations:
     * custom if exists, than for kernel and after for the plugin itself.
     * @param toLang target language's ID: {@link #LANG_RU}, {@link #LANG_EN}, {@link #LANG_DE}
     * @param pluginIds plugin IDs, null - for kernel
     */
    public static Localizer getLocalizer(String toLang, String... pluginIds) {
        loadLocalizations();

        var localizations = new ArrayList<Localization>(3);

        // custom plugin first
        var custom = Localization.localizations.get(PLUGIN_CUSTOM);
        if (custom != null)
            localizations.add(custom);

        // the defined plugin, if it has localization
        if (pluginIds != null) {
            for (String pluginId : pluginIds) {
                if (pluginId == null || org.bgerp.plugin.kernel.Plugin.ID.equals(pluginId))
                    continue;
                var pluginL10n = Localization.localizations.get(pluginId);
                if (pluginL10n != null)
                    localizations.add(pluginL10n);
            }
        }

        // kernel plugin
        localizations.add(Localization.localizations.get(org.bgerp.plugin.kernel.Plugin.ID));

        return new Localizer(toLang, localizations.toArray(new Localization[0]));
    }

    /**
     * Retrieves {@link Localizer} object for a single plugin.
     * @param pluginId plugin ID.
     * @param request used for getting target language in case of open interface.
     * @return
     */
    @Dynamic
    public static Localizer getLocalizer(String pluginId, HttpServletRequest request) {
        return getLocalizer(getLang(request), pluginId);
    }

    /**
     * Localizer for kernel only to the language, taken from {@link #getLang()}.
     * @return
     */
    public static Localizer getLocalizer() {
        return getLocalizer(getLang(), (String) null);
    }

    private static void loadLocalizations() {
        synchronized (Localization.class) {
            if (localizations == null) {
                try {
                    log.info("Loading localizations..");

                    localizations = new HashMap<>();

                    var customL10n = new File(PLUGIN_CUSTOM, FILE_NAME);
                    if (customL10n.exists())
                        loadL10n(new Localization(PLUGIN_CUSTOM, XMLUtils.parseDocument(new FileInputStream(customL10n))));

                    for (Plugin p : PluginManager.getInstance().getFullSortedPluginList())
                        loadL10n(p.getLocalization());

                    localizations = Collections.unmodifiableMap(localizations);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    private static void loadL10n(Localization localization) throws Exception {
        if (localization == null)
            return;

        localizations.put(localization.pluginId, localization);
        log.debug("Loaded localization for: {}", localization.pluginId);
    }
}
