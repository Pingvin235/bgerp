package org.bgerp.tool.prop;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.tools.ant.util.LayoutPreservingProperties;

import ru.bgcrm.util.Utils;

/**
 * Updates module properties.
 *
 * @author Alexander Yaryzhenko
 * @author Shamil Vakhitov
 */
public class Update extends Module {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private Update(String version, String dir, String changeId) throws Exception {
        super(dir);

        String moduleName = name;
        String path = dir + "/" + propertiesName();

        Properties prop = new LayoutPreservingProperties();
        try (var input = new FileInputStream(path)) {
            prop.load(input);
        }

        prop.setProperty(KEY_BUILD_TIME, FORMAT.format(LocalDateTime.now()));
        prop.setProperty(KEY_BUILD_NUMBER, String.valueOf(Utils.parseInt(prop.getProperty(KEY_BUILD_NUMBER)) + 1));
        prop.setProperty(KEY_NAME, moduleName);
        prop.setProperty(KEY_VERSION, version);
        if (!changeId.isBlank())
            prop.setProperty("change.id", changeId);

        try (var output = new FileOutputStream(path)) {
            prop.store(output, null);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args == null || args.length != 3)
            throw new IllegalArgumentException("Three command line arguments are expected.");
        new Update(args[0], args[1], args[2]);
    }
}
