package ru.bgerp.l10n;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.XMLUtils;
import ru.bgerp.util.Log;

/**
 * A localization unit, loaded from a file. 
 * @author Shamil
 */
public class Localization {
    private static final Log log = Log.getLog();

    /** Custom localizations may be placed in the custom/l10n.xml file in the root of the program. */
    private static final String PLUGIN_CUSTOM = "custom";
    private static final String PLUGIN_KERNEL = "kernel";

    private static final String FILE_NAME = "l10n.xml";
    // localizations of kernel and plugins
    private static Map<String, Localization> localizations;
    private static long lastLoadTime;

    // end of static part
    private final String pluginName;
    private final Map<String, Map<String, String>> translations;

    private Localization(String name, Document doc) throws Exception {
        this.pluginName = name;
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
                        log.warn("Duplicated key: " + value);
                    translations.put(value, phraseMap);
                }
                phraseMap.put(lang.getNodeName(), value);
            }
        }
    }

    public String getTranslation(String value, String toLang) {
        return translations.containsKey(value) ? translations.get(value).get(toLang) : null;
    }
    
    public static Localizer getLocalizer(HttpServletRequest request) {
        final String pluginUrl = "/plugin/";
        
        String plugin = null;
        
        String url = ((HttpServletRequest) request).getRequestURI();
        int pos = url.indexOf(pluginUrl);
        if (pos > 0)
            plugin = StringUtils.substringBefore(url.substring(pos + pluginUrl.length()), "/");
        
        return getLocalizer(plugin);
    }

    /** 
     * Retrieve localizer for a plugin. 
     * The localizer includes the following localizations:
     * custom if exists, than for kernel and after for the plugin itself.
     * 
     * @param plugin plugin name, null - for kernel
     */
    public static Localizer getLocalizer(String plugin) {
        String toLang = Setup.getSetup().get("lang", "ru");
        
        loadLocalizations();

        var localizations = new ArrayList<Localization>(3);
        var custom = Localization.localizations.get(PLUGIN_CUSTOM);
        if (custom != null)
            localizations.add(custom);
        localizations.add(Localization.localizations.get(PLUGIN_KERNEL));
        if (plugin != null)
            localizations.add(Localization.localizations.get(plugin));

        return new Localizer(localizations.toArray(new Localization[localizations.size()]), toLang);
    }
    
    public static Localizer getLocalizer() {
        return getLocalizer((String) null);
    }

    private static void loadLocalizations() {
        synchronized (Localization.class) {
            if (localizations == null || (!Setup.getSetup().getBoolean("localization.cache", true) && lastLoadTime + 10000  < System.currentTimeMillis())) {
                try {
                    log.info("Loading localizations..");

                    localizations = new HashMap<>();

                    var customL10n = new File(PLUGIN_CUSTOM, FILE_NAME);
                    if (customL10n.exists())
                        loadL10n(PLUGIN_CUSTOM, XMLUtils.parseDocument(new FileInputStream(customL10n)));

                    loadL10n(PLUGIN_KERNEL, XMLUtils.parseDocument(Localization.class.getResourceAsStream(FILE_NAME)));

                    for (Plugin p : PluginManager.getInstance().getPluginList()) {
                        var doc = p.getXml(FILE_NAME, null);
                        if (doc == null) continue;
                        loadL10n(p.getName(), doc);
                    }
                    
                    lastLoadTime = System.currentTimeMillis();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    private static void loadL10n(String plugin, Document doc) throws Exception {
        Localization l = new Localization(plugin, doc);
        localizations.put(l.pluginName, l);
        log.debug("Loaded localization for: %s", l.pluginName);
    }
}
