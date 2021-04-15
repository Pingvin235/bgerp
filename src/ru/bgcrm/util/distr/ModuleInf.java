package ru.bgcrm.util.distr;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Update module information.
 * 
 * @author Shamil Vakhitov
 */
public class ModuleInf {
    private static final String supportedModuleVersion = "1.0";

    private boolean errors = false;
    private String moduleVersion;
    private String inf;
    private String name;
    private List<String[]> calls = new ArrayList<String[]>();

    public ModuleInf(String moduleInf) {
        inf = moduleInf;
        parseInf();
        if (errors) {
            System.out.println("Error: not all params was found in module.properties");
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
                if (!value.equals(supportedModuleVersion)) {
                    System.out.println("Error: modules version " + value + " is not supported.");
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