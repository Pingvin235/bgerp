package org.bgerp.action;

import java.util.List;

import org.apache.struts.action.ActionForward;
import org.bgerp.model.base.tree.IdTitleTreeItem;

import ru.bgcrm.model.user.PermissionNode;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/test")
public class TestAction extends org.bgerp.action.open.TestAction {
    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        setPermTreesValues(form);
        setTreeValues(form);
        setComboValues(form);
        return html(conSet, form, PATH_JSP_USER + "/test.jsp");
    }

    private void setPermTreesValues(DynActionForm form) {
        form.setRequestAttribute("permTrees", PermissionNode.getPermissionTrees());
    }

    private void setTreeValues(DynActionForm form) {
        int cnt = 1;

        var rootNode = new IdTitleTreeItem.Default(cnt++, "Root Node");

        var node1 = rootNode.addChild(new IdTitleTreeItem.Default(cnt++, "Node 1"));
        var node11 = node1.addChild(new IdTitleTreeItem.Default(cnt++, "Node 1 1"));
        node11.addChild(new IdTitleTreeItem.Default(cnt++, "Node 1 1 1"));
        node1.addChild(new IdTitleTreeItem.Default(cnt++, "Node 1 2 with a loooooooooooooooooong text"));

        var node2 = rootNode.addChild(new IdTitleTreeItem.Default(cnt++, "Node 2"));
        for (int i = 1; i <= 5; i++)
            node2.addChild(new IdTitleTreeItem.Default(cnt++, "Node 2 "+ i));

        form.setResponseData("treeRootNode", rootNode);
    }

    public ActionForward enumValues(DynActionForm form, ConnectionSet conSet) {
        List<String> values = List.of(
                "mail1@domain.com",
                "Ivan2 Pupkin <mail2@domain.com>",
                "Ivan3 Pupkin <mail3@domain.com>");
        form.setResponseData("values", values);
        return json(conSet, form);
    }

    // TODO: Some helper methods for testing parameters validatation and so on.
}
