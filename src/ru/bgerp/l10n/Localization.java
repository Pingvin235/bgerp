package ru.bgerp.l10n;

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

    public static Localizer getLocalizer(String plugin) {
        String toLang = Setup.getSetup().get("lang", "ru");
        
        loadLocalizations();

        Localization[] localizations = new Localization[2];
        localizations[0] = Localization.localizations.get(PLUGIN_KERNEL);
        if (plugin != null)
            localizations[1] = Localization.localizations.get(plugin);

        return new Localizer(localizations, toLang);
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

                    Document doc = XMLUtils.parseDocument(Localization.class.getResourceAsStream(FILE_NAME));
                    Localization l = new Localization(PLUGIN_KERNEL, doc);
                    localizations.put(l.pluginName, l);
                    log.debug("Loaded localization for kernel.");

                    for (Plugin p : PluginManager.getInstance().getPluginList()) {
                        doc = p.getXml(FILE_NAME, null);
                        if (doc == null) continue;
                        
                        l = new Localization(p.getName(), doc);
                        localizations.put(l.pluginName, l);
                        log.debug("Loaded localization for: " + l.pluginName);
                    }
                    
                    lastLoadTime = System.currentTimeMillis();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }
}
