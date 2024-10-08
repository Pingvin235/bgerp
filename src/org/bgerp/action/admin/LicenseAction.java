package org.bgerp.action.admin;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.dist.lic.AppLicense;
import org.bgerp.app.dist.lic.License;
import org.bgerp.app.exception.BGMessageException;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/license", pathId = true)
public class LicenseAction extends BaseAction {
    public static final String PATH_JSP = PATH_JSP_ADMIN + "/license";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setRequestAttribute("license", AppLicense.instance());

        return html(conSet, form, PATH_JSP + "/license.jsp");
    }

    public ActionForward upload(DynActionForm form, ConnectionSet conSet) throws Exception {
        var file = form.getFile();

        byte[] data = IOUtils.toByteArray(file.getInputStream());

        String error = new License(new String(data, StandardCharsets.UTF_8)).getError();
        if (Utils.notBlankString(error))
            throw new BGMessageException(error);

        IOUtils.write(data, new FileOutputStream(License.FILE_NAME));
        AppLicense.init();

        return json(conSet, form);
    }
}
