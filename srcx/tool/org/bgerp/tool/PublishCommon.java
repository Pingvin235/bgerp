package org.bgerp.tool;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Common SSH publishing class.
 *
 * @author Shamil Vakhitov
 */
public class PublishCommon {
    protected final static String SSH_LOGIN = "cdn@cdn.bgerp.org";
    protected final static String[] SSH_OPTIONS = { "-o", "StrictHostKeyChecking=no", "-o", "UserKnownHostsFile=/dev/null" };

    /** App version. */
    protected final String version;
    /** Local dir with built files. */
    protected final String dir;
    /** Remote dir for the process. */
    protected final String sshDir;

    protected PublishCommon(String version, String dir, String sshDir) {
        this.version = version;
        this.dir = dir;
        this.sshDir = sshDir;
    }

    /**
     * Executes using SSH shell commands on {@link #SSH_LOGIN} with {@link #SSH_OPTIONS}.
     * @param cmd commands.
     * @return the runner instance.
     * @throws Exception
     */
    protected RuntimeRunner ssh(String... cmd) throws Exception {
        var result = new RuntimeRunner("ssh", SSH_OPTIONS, ArrayUtils.addAll(new String[] { SSH_LOGIN }, cmd));
        result.run();
        return result;
    }

    /**
     * Copies using SCP local file from {@link #dir} to remote {@link #sshDir}.
     * @param file local file name with path.
     * @throws Exception
     */
    protected void scp(String file) throws Exception {
        new RuntimeRunner("scp", SSH_OPTIONS, file, SSH_LOGIN + ":" + sshDir).run();
    }
}
