package org.bgerp.app.servlet.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.exception.BGIllegalArgumentException;

import com.google.common.collect.Lists;

import ru.bgcrm.servlet.ActionServlet;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Files accessor, allowing to see and remove directory files.
 *
 * @author Shamil Vakhitov
 */
public class Files {
    private final Class<? extends BaseAction> actionClass;
    private final Path basedir;
    private final String id;
    private final Options options;
    private final FileFilter fileFilter;

    /**
     * Constructor
     * @param actionClass the linked action class
     * @param id unique ID of the files
     * @param basedir base dir, only the files there are shown
     * @param options extended possibilities
     * @param wildcards wildcards for filtering files, '*' is supported
     */
    public Files(Class<? extends BaseAction> actionClass, String id, String basedir, Options options, String... wildcards) {
        this.actionClass = actionClass;
        this.id = id;
        this.basedir = Paths.get(basedir);
        this.options = options;
        this.fileFilter = new WildcardFileFilter(wildcards);
    }

    /**
     * Unique string ID of the files set.
     * Used for generation action names.
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Options.
     * @return
     */
    public Options getOptions() {
        return options;
    }

    /**
     * File download URL, e.g.: '/admin/app.do?method=downloadLogUpdate'.
     * A {@code name} param has to be added at the end.
     * @return
     */
    public String getDownloadURL() {
        return
            ActionServlet.getActionPath(actionClass) + ".do?" +
            DynActionForm.PARAM_ACTION_METHOD + "=" + getActionMethod("download");
    }

    /**
     * File download permission action for checking it using ctxUser.checkPerm().
     * @return
    */
    public String getDownloadPermissionAction() {
        return actionClass.getName() + ":" + getActionMethod("download");
    }

    /**
     * File deletion URL, e.g.: '/admin/app.do?method=deleteLogUpdate'.
     * A {@code name} param has to be added at the end.
     * @return
     */
    public String getDeleteURL() {
        return
            ActionServlet.getActionPath(actionClass) + ".do?" +
            DynActionForm.PARAM_ACTION_METHOD + "=" + getActionMethod("delete");
    }

    /**
     * File deletion permission action for checking it using ctxUser.checkPerm().
     * @return
    */
    public String getDeletePermissionAction() {
        return actionClass.getName() + ":" + getActionMethod("delete");
    }

    private String getActionMethod(String prefix) {
        return prefix + id.substring(0, 1).toUpperCase() + id.substring(1);
    }

    /**
     * List of files matching {@link #wildcard} sorted in reversed order by modification time.
     * @return
     */
    public List<File> list() {
        File basedir = this.basedir.toFile();

        List<File> result =
            basedir.isDirectory() ?
            Lists.newArrayList(basedir.listFiles(fileFilter)) :
            new ArrayList<>();
        if (options.getOrder() != null)
            result.sort(options.getOrder());

        return result;
    }

    /**
     * Input stream to a file.
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    private InputStream getInputStream(String path) throws FileNotFoundException {
        return new FileInputStream(new File(basedir.toFile(), path));
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

    /**
     * Delete file(s).
     * @param form form with 'name' parameter values, containing file name.
     */
    public void delete(DynActionForm form) {
        for (String name : form.getParamValuesStr("name"))
            FileUtils.deleteQuietly(new File(basedir.toFile(), name));
    }
}
