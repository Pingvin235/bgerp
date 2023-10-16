package org.bgerp.action.open;

import java.util.List;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/test")
public class TestAction extends BaseAction {
    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        setValues(form);
        return html(conSet, null, PATH_JSP_OPEN + "/test/test.jsp");
    }

    protected void setValues(DynActionForm form) {
        form.setResponseData("comboSingleList", List.of(
            new IdTitle(1, "First dyn value"),
            new IdTitle(2, "Second dyn value with a loooooooooooooooooooong teeeeeeeeeeeeext")
        ));

        form.setResponseData("comboCheckList", List.of(
            new IdTitle(1, "First value"),
            new IdTitle(2, "Second value and a long texttttttttt after it")
        ));
        form.setResponseData("comboCheckValues", Set.of(2));
    }

    // TODO: Some helper methods for testing parameters validatation and so on.
}
