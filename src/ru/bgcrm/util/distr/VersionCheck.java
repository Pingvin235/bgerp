package ru.bgcrm.util.distr;

import java.time.Duration;
import java.time.Instant;

import org.bgerp.util.Log;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.distr.InstallProcessor.FileInfo;

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
    private final VersionInfo currentVersion;

    private volatile Instant remoteVersionCheckTime;
    private volatile FileInfo remoteVersion;

    private VersionCheck() {
        currentVersion = currentVersionInfo();
    }

    private VersionInfo currentVersionInfo() {
        final var props = Setup.getSetup().sub("test.version.check.current.");
        return props.isEmpty() ? VersionInfo.getVersionInfo(VersionInfo.MODULE_UPDATE) : new VersionInfo(props);
    }

    public boolean isUpdateNeeded() {
        if (remoteVersionCheckTime == null || 0 < Duration.between(remoteVersionCheckTime, Instant.now()).compareTo(REMOTE_VERSION_CHECK_INTERVAL)) {
            remoteVersionCheckTime = Instant.now();
            // running in thread to process event quickly
            new Thread() {
                @Override
                public void run() {
                    log.info("Retrieving remote version");
                    remoteVersion = new InstallProcessor(currentVersion.getVersion()).getRemoteFileMap().get(VersionInfo.MODULE_UPDATE);
                    if (remoteVersion == null)
                        log.error("Not found remote version info");
                }
            }.start();
        }

        if (remoteVersion == null)
            return false;

        final int currentBuild = Utils.parseInt(currentVersion.getBuildNumber());
        final int remoteBuild = Utils.parseInt(remoteVersion.getBuildNumber());

        final boolean result =
            (Utils.notBlankString(currentVersion.getChangeId()) && currentBuild <= remoteBuild) ||
            currentBuild < remoteBuild;
        log.debug("Update is needed: {}, currentBuild: {}, remoteBuild: {}", result, currentBuild, remoteBuild);

        return result;
    }
}
