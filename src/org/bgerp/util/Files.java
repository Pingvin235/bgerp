package org.bgerp.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.struts.action.ActionForward;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.servlet.ActionServlet;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * File accessor.
 * 
 * @author Shamil Vakhitov
 */
// TODO: Move to global Files registry, with cleaning up old logs.
public class Files {
    private final Class<? extends BaseAction> actionClass;
    private final Path basedir;
    private final String id;
    private final String wildcard;

    public Files(Class<? extends BaseAction> actionClass, String id, String basedir, String wildcard) {
        this.actionClass = actionClass;
        this.id = id;
        this.basedir = Paths.get(basedir);
        this.wildcard = wildcard;
    }

    /**
     * Unique string ID of the files set.
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * File download URL, e.g.: '/admin/app.do?action=downloadLogUpdate'.
     * A {@code name} param has to be added at the end.
     * @return
     */
    public String getDownloadURL() {
        return
            ActionServlet.getActionPath(actionClass) + ".do?" +
            "action=" + getActionMethod();
    }

    /**
     * File download permission action for checking it using p:check.
     * @return 
    */
    public String getDownloadPermissionAction() {
        return actionClass.getName() + ":" + getActionMethod();
    }

    private String getActionMethod() {
        return "download" + id.substring(0, 1).toUpperCase() + id.substring(1);
    }

    /**
     * List of files matching {@link #wildcard} sorted in reversed order by modification time.
     * @return
     */
    public List<File> list() {
        FileFilter fileFilter = new WildcardFileFilter(wildcard);
        
        var result = Lists.newArrayList(basedir.toFile().listFiles(fileFilter)) ;
        result.sort((f1, f2) -> (int) ((f2.lastModified() - f1.lastModified()) / 1000));

        return result;
    }

    /**
     * Input stream to a file.
     * @param name
     * @return
     * @throws FileNotFoundException
     */
    private InputStream getInputStream(String name) throws FileNotFoundException {
        return new FileInputStream(new File(basedir.toFile(), name));
    }

    /**
     * Send a file content to output stream.
     * @param form
     * @throws BGIllegalArgumentException
     * @throws IOException
     * @throws FileNotFoundException
     * @return always 'null'.
     */
    public ActionForward download(DynActionForm form) throws BGIllegalArgumentException, IOException, FileNotFoundException {
        var name = form.getParam("name", Utils::notBlankString);

        var response = form.getHttpResponse();
        Utils.setFileNameHeaders(response, name);

        IOUtils.copy(getInputStream(name), response.getOutputStream());

        return null;
    }
}
