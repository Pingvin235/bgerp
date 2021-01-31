package ru.bgerp.l10n;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.plugin.Lang;
import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.servlet.filter.AuthFilter;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.XMLUtils;
import ru.bgerp.util.Log;

/**
 * A localization unit, loaded from a resource for a plugin.
 * @author Shamil Vakhitov
 */
public class Localization {
    private static final Log log = Log.getLog();

    public static final String LANG_RU = Lang.RU.getId();
    public static final String LANG_EN = Lang.EN.getId();
    public static final String LANG_DE = Lang.DE.getId();

    /** Custom localizations may be placed in the custom/l10n.xml file in the application's directory. */
    private static final String PLUGIN_CUSTOM = "custom";

    private static final String FILE_NAME = "l10n.xml";
    /** Localizations of plugins. */
    private static volatile Map<String, Localization> localizations;

    private static final String PLUGIN_URI_PREFIX = "/plugin/";
    private static final int PLUGIN_URI_PREFIX_LENGTH = PLUGIN_URI_PREFIX.length();

    private static final String LANG_KEY_NAME = "lang";

    // end of static part
    private final String pluginId;
    private final Map<String, Map<String, String>> translations;

    private Localization(String pluginId, Document doc) throws Exception {
        this.pluginId = pluginId;
        this.translations = new HashMap<>(300);
        parseFile(doc);
    }

    private void parseFile(Document doc) throws Exception {
        for (Element phrase : XMLUtils.elements(doc.getDocumentElement().getChildNodes())) {
            Map<String, String> phraseMap = null;

            for (Element lang : XMLUtils.elements(phrase.getChildNodes())) {
                String value = XMLUtils.getElementText(lang);
                // first language - key
                if (phraseMap == null) {
                    phraseMap = new HashMap<>(4);
                    if (translations.containsKey(value))
                        log.warn("Duplicated key: %s in %s", value, this);
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
     * @param value phrase.
     * @param toLang target lang.
     * @return translation or null if missing.
     */
    public String getTranslation(String value, String toLang) {
        return translations.containsKey(value) ? translations.get(value).get(toLang) : null;
    }

    /**
     * Retrieves a Localizer for a plugin, using request URI.
     * @param request plugin ID is got out of getRequestURI().
     * @return
     */
    public static Localizer getLocalizer(HttpServletRequest request) {
        return getLocalizer(getPluginIdFromURI(request), getToLang(request));
    }

    private static String getPluginIdFromURI(HttpServletRequest request) {
        String pluginId = null;

        String url = request.getRequestURI();
        int pos = url.indexOf(PLUGIN_URI_PREFIX);
        if (pos > 0)
            pluginId = StringUtils.substringBefore(url.substring(pos + PLUGIN_URI_PREFIX_LENGTH), "/");

        return pluginId;
    }

    public static String getToLang(HttpServletRequest request) {
        String result = null;
        
        // open interface
        if (AuthFilter.getUser(request) == null) {
            result = request.getParameter(LANG_KEY_NAME);
            
            HttpSession session = request.getSession(false);
            if (Utils.isBlankString(result) && session != null)
                result = (String) session.getAttribute(LANG_KEY_NAME);

            request.getSession().setAttribute(LANG_KEY_NAME, result);
        }

        if (Utils.isBlankString(result))
            result  = Setup.getSetup().get("lang", LANG_RU);

        return result;
    }

    /**
     * Retrieve localizer for a plugin.
     * The localizer includes the following localizations:
     * custom if exists, than for kernel and after for the plugin itself.
     *
     * @param pluginId plugin ID, null - for kernel
     * @param toLang target language's ID: {@link #LANG_RU}, {@link #LANG_EN}, {@link #LANG_DE}
     */
    public static Localizer getLocalizer(String pluginId, String toLang) {
        loadLocalizations();

        var localizations = new ArrayList<Localization>(3);

        // custom plugin first
        var custom = Localization.localizations.get(PLUGIN_CUSTOM);
        if (custom != null)
            localizations.add(custom);

        // kernel plugin
        localizations.add(Localization.localizations.get(org.bgerp.plugin.kernel.Plugin.ID));

        // the defined plugin, if it has localization
        if (pluginId != null) {
            var pluginL10n = Localization.localizations.get(pluginId);
            if (pluginL10n != null)
                localizations.add(pluginL10n);
        }

        return new Localizer(localizations.toArray(new Localization[localizations.size()]), toLang);
    }

    /**
     * Localizer for kernel only to English.
     * @return
     */
    public static Localizer getSysLocalizer() {
        return getLocalizer((String) null, LANG_EN);
    }

    private static void loadLocalizations() {
        synchronized (Localization.class) {
            if (localizations == null) {
                try {
                    log.info("Loading localizations..");

                    localizations = new HashMap<>();

                    var customL10n = new File(PLUGIN_CUSTOM, FILE_NAME);
                    if (customL10n.exists())
                        loadL10n(PLUGIN_CUSTOM, XMLUtils.parseDocument(new FileInputStream(customL10n)));

                    for (Plugin p : PluginManager.getInstance().getPluginList()) {
                        var doc = p.getXml(FILE_NAME, null);
                        if (doc == null)
                            continue;
                        loadL10n(p.getId(), doc);
                    }

                    localizations = Collections.unmodifiableMap(localizations);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    private static void loadL10n(String pluginId, Document doc) throws Exception {
        Localization l = new Localization(pluginId, doc);
        localizations.put(l.pluginId, l);
        log.debug("Loaded localization for: %s", l.pluginId);
    }
}
