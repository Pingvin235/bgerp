package org.bgerp.app.dist.inst;

import java.time.Duration;
import java.time.Instant;

import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

/**
 * Version checker caching remote build number.
 * Test keys:
 * <li> test.version.check.current.version
 * <li> test.version.check.current.build.number
 *
 * @author Shamil Vakhitov
 */
public class VersionCheck {
    private static final Log log = Log.getLog();

    public static final VersionCheck INSTANCE = new VersionCheck();

    private static final Duration REMOTE_VERSION_CHECK_INTERVAL = Duration.ofHours(2);
    // version of the running app
    private final InstalledModule currentVersion;

    private volatile Instant remoteVersionCheckTime;
    private volatile ModuleFile remoteVersion;

    private VersionCheck() {
        currentVersion = currentVersionInfo();
    }

    private InstalledModule currentVersionInfo() {
        final var props = Setup.getSetup().sub("test.version.check.current.");
        return props.isEmpty() ? InstalledModule.get(InstalledModule.MODULE_UPDATE) : new InstalledModule(props);
    }

    public boolean isUpdateNeeded() {
        // running in IDE and no test version is defined
        if (currentVersion == null)
            return false;

        if (remoteVersionCheckTime == null || 0 < Duration.between(remoteVersionCheckTime, Instant.now()).compareTo(REMOTE_VERSION_CHECK_INTERVAL)) {
            remoteVersionCheckTime = Instant.now();
            // running in thread to process event quickly
            new Thread() {
                @Override
                public void run() {
                    log.info("Retrieving remote version");
                    remoteVersion = new InstallerModules(currentVersion.getVersion()).getRemoteFileMap().get(InstalledModule.MODULE_UPDATE);
                    if (remoteVersion == null)
                        log.error("Not found remote version info");
                }
            }.start();
        }

        if (remoteVersion == null)
            return false;

        final int currentBuild = Utils.parseInt(currentVersion.getBuildNumber());
        final int remoteBuild = Utils.parseInt(remoteVersion.getBuildNumber());

        final boolean result = currentBuild < remoteBuild;
        if (result)
            log.info("Update is needed; currentBuild: {}, remoteBuild: {}", currentBuild, remoteBuild);
        else
            log.trace("Update is NOT needed; currentBuild: {}, remoteBuild: {}", currentBuild, remoteBuild);

        return result;
    }
}
