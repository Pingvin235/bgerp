package ru.bgcrm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

import ru.bgcrm.model.BGMessageException;

/**
 * Checks string to one of configured regexps.
 *
 * @author Shamil Vakhitov
 */
public class RegexpCheckerConfig extends Config {
    private static final Log log = Log.getLog();

    private List<Pattern> regexpList;
    private String regexpTitles = "";

    public RegexpCheckerConfig(ConfigMap config) {
        super(null);

        for (ConfigMap value : config.subIndexed("regexp.").values()) {
            String title = value.get("title");
            String regexp = value.get("regexp");

            if (Utils.isBlankString(title)) {
                continue;
            }

            try {
                Pattern pattern = Pattern.compile(regexp);
                if (regexpList == null) {
                    regexpList = new ArrayList<Pattern>();
                }
                regexpList.add(pattern);

                if (Utils.notBlankString(regexpTitles)) {
                    regexpTitles += "\n";
                }
                regexpTitles += title;
            } catch (Exception e) {
                log.error("Regexp pattern load: " + e.getMessage() + ". Title: " + title + "; regexp: " + regexp);
            }
        }
    }

    public void checkValue(String value) throws BGMessageException {
        if (regexpList != null) {
            for (Pattern p : regexpList) {
                if (p.matcher(value).matches()) {
                    return;
                }
            }
            throw new BGMessageException("Значение должно соответствовать одному из шаблонов: {}", regexpTitles);
        }
    }
}