package org.bgerp.itest.helper;

import java.io.File;

import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.model.FileData;

public class FileHelper {
    
    public static FileData addFile(File file) throws Exception {
        try (var con = DbTest.conPoolRoot.getDBConnectionFromPool()) {
            var dao = new FileDataDAO(con);

            var fd = new FileData();
            fd.setTitle(file.getName());
            dao.add(fd);

            Assert.assertTrue(fd.getId() > 0);

            con.commit();

            return fd;
        }
    }

}