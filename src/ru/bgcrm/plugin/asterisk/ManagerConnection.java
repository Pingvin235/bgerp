package ru.bgcrm.plugin.asterisk;

import java.io.IOException;

import org.asteriskjava.AsteriskVersion;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.internal.ManagerConnectionImpl;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Log;

public class ManagerConnection extends ManagerConnectionImpl {
    private static final Log log = Log.getLog();

    private final AsteriskVersion version;

    public ManagerConnection(ConfigMap config) {
        super();

        String host = config.get("host");
        int port = config.getInt("port", 5038);
        String login = config.get("login");
        String pswd = config.get("pswd");
        String version = config.get("version");

        this.version = version == null ? null : AsteriskVersion.getDetermineVersionFromString(version);

        log.info("Connecting AMI host: {}; port: {}; login: {}; pswd: {}; version: {}", host, port, login, pswd, version);

        setHostname(host);
        setPort(port);
        setUsername(login);
        setPassword(pswd);
    }

    @Override
    protected AsteriskVersion determineVersion() throws IOException, TimeoutException {
        if (version != null)
            return version;
        return super.determineVersion();
    }
}