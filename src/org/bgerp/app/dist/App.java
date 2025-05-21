package org.bgerp.app.dist;

import org.bgerp.app.cfg.Setup;

public class App {
    public static final String URL = "https://bgerp.org";
    public static final String UPDATE_URL = Setup.getSetup().get("update.url", App.URL);
}
