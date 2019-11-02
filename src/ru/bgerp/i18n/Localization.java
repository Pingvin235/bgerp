package ru.bgerp.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.XMLUtils;

/**
 * A localization unit, loaded from a file. 
 * @author Shamil
 */
public class Localization {
    private static final Logger log = Logger.getLogger(Localization.class);

    private static final String DIRECTORY = "plugin/i18n";
    // localizations of kernel and plugins
    private static Map<String, Localization> localizations;
    private static long lastLoadTime;

    // end of static part
    private final String pluginName;
    private final Map<String, Map<String, String>> translations;

    private Localization(File file) throws Exception {
        this.pluginName = StringUtils.substringBefore(file.getName(), ".");
        this.translations = new HashMap<>(300);
        parseFile(file);
    }

    private void parseFile(File file) throws Exception {
        Document doc = XMLUtils.parseDocument(new InputSource(new FileInputStream(file)));
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
        localizations[0] = Localization.localizations.get("kernel");
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
                    for (File file : new File(DIRECTORY).listFiles()) {
                        Localization l = new Localization(file);
                        localizations.put(l.pluginName, l);
                        log.info("Loaded localization for: " + l.pluginName);
                    }
                    
                    lastLoadTime = System.currentTimeMillis();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
