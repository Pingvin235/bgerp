package org.bgerp.tool.prop;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.bgerp.util.Log;

/**
 * Copies module properties to compiled classes to be archived.
 *
 * @author Shamil Vakhitov
 */
public class Copy extends Module {
    private static final Log LOG = Log.getLog();

    private Copy(String dir, String classesPath) throws Exception {
        super(dir);

        LOG.info("dir: {}, classesPath: {}", dir, classesPath);

        String name = this.name + ".properties";

        Path to = Path.of(classesPath,"ru/bgcrm/version");
        to.toFile().mkdirs();

        Files.copy(
            Path.of(dir, name),
            to.resolve(name),
            StandardCopyOption.REPLACE_EXISTING
        );
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length != 2)
            throw new IllegalArgumentException("Two command line arguments are expected.");
        new Copy(args[0], args[1]);
    }
}
