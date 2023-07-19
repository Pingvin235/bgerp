package org.bgerp.plugin.svc.backup;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.servlet.file.Files;
import org.bgerp.util.Dynamic;

public class Config extends org.bgerp.app.cfg.Config {
    /**
     * More than that amount of backup files will became cleanup candidate.
     */
    private final int cleanupCandidateCountMoreThan;
    /**
     * More than that amount of backup with DB files will became cleanup candidate.
     */
    private final int cleanupCandidateDbCountMoreThan;

    protected Config(ConfigMap config) {
        super(null);
        config = config.sub(Plugin.ID + ":");
        cleanupCandidateCountMoreThan = config.getInt("cleanup.candidate.count.more.than", 3);
        cleanupCandidateDbCountMoreThan = config.getInt("cleanup.candidate.db.count.more.than", 3);
    }

    /**
     * File names - candidates to cleaning up.
     * @param files must be sorted by modification time desc.
     * @return set with names.
     */
    @Dynamic
    public Set<String> cleanupCandidates(Files files) {
        var names = files.list().stream().map(File::getName).collect(Collectors.toList());

        var result = new HashSet<String>(names.size());

        int cleanupCandidateMoreThat = this.cleanupCandidateCountMoreThan;
        int cleanupCandidateDbMoreThat = this.cleanupCandidateDbCountMoreThan;

        for (String name : names) {
            if (name.contains(".db.")) {
                if (cleanupCandidateDbMoreThat-- <= 0)
                    result.add(name);
            } else if (cleanupCandidateMoreThat-- <= 0)
                result.add(name);
        }

        return Collections.unmodifiableSet(result);
    }
}
