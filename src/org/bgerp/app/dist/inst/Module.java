package org.bgerp.app.dist.inst;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.bgerp.util.Log;

/**
 * Installation module information.
 *
 * @author Shamil Vakhitov
 */
public class Module {
    private static final Log log = Log.getLog();

    private static final String SUPPORTED_MODULE_VERSION = "1.0";

    private boolean errors = false;
    private String moduleVersion;
    private String inf;
    private String name;
    private List<String[]> calls = new ArrayList<>();

    public Module(String moduleInf) {
        inf = moduleInf;
        parseInf();
        if (errors) {
            log.error("Not all params were found in module.properties");
        }
    }

    private void parseInf() {
        StringTokenizer st = new StringTokenizer(inf, "\n");
        while (st.hasMoreTokens()) {
            String line = st.nextToken().trim();
            StringTokenizer pair = new StringTokenizer(line, "=");
            if (pair.countTokens() != 2)
                continue;
            String key = pair.nextToken();
            String value = pair.nextToken();
            if (key.equals("name")) {
                name = value;
            } else if (key.equals("call")) {
                String[] class_param = value.split(";");
                if (class_param.length == 2) {
                    calls.add(class_param);
                }
            } else if (key.equals("module.version")) {
                if (!value.equals(SUPPORTED_MODULE_VERSION)) {
                    log.error("Modules version {} are not supported.", value);
                    errors = true;
                } else {
                    moduleVersion = value;
                }
            }
        }
        errors = name == null || moduleVersion == null;
    }

    public boolean hasErrors() {
        return errors;
    }

    public String getName() {
        return name;
    }

    public List<String[]> getCalls() {
        return calls;
    }
}