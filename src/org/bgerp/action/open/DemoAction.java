package org.bgerp.action.open;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.base.IdTitleComment;
import org.bgerp.model.base.tree.IdTitleTreeItem;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/demo")
public class DemoAction extends BaseAction {
    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) {
        comboSingleValues(form);
        selectSingleValues(form);
        selectMultValues(form);
        treeValues(form);
        return html(conSet, null, PATH_JSP_OPEN + "/demo/demo.jsp");
    }

    private void comboSingleValues(DynActionForm form) {
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

    private void selectSingleValues(DynActionForm form) {
        form.setResponseData("selectSingle1List", List.of(
            new IdTitle(1, "First value"),
            new IdTitle(2, "Current second value and a long texttttttttt after it"),
            new IdTitle(3, "Must not be available!!!")
        ));
        form.setResponseData("selectSingle1AvailableIdSet", Set.of(1, 2));

        form.setResponseData("selectSingle2Map", Map.of(
            1, new IdTitle(1, "Second value"),
            2, new IdTitle(2, "First value"),
            3, new IdTitle(3, "Must not be available!!!")
        ));
        form.setResponseData("selectSingle2AvailableIdList", List.of(2, 1));

        form.setResponseData("selectSingle3List", List.of(
            new IdTitleComment(1, "First selected value", "Comment 1"),
            new IdTitleComment(2, "Second value", "Comment 1")
        ));

        form.setResponseData("selectSingle4List", List.of(
            new IdTitle(1, "Must not be available, because of disabled input!"),
            new IdTitle(2, "Current second value")
        ));
    }

    private void selectMultValues(DynActionForm form) {
        List<IdTitle> list = List.of(
            new IdTitle(1, "First value"),
            new IdTitle(2, "Second value and a long texttttttttt after it")
        );

        form.setResponseData("selectMult1List", list);
        form.setResponseData("selectMult1Values", Set.of(2));

        form.setResponseData("selectMult2List", list);
        form.setResponseData("selectMult2Map", list.stream().collect(Collectors.toMap(IdTitle::getId, Function.identity())));
        form.setResponseData("selectMult2Values", List.of(2, 1));
    }

    private void treeValues(DynActionForm form) {
        int cnt = 1;

        var rootNode = new IdTitleTreeItem(cnt++, "Root Node");

        var node1 = rootNode.addChild(new IdTitleTreeItem(cnt++, "Node 1"));
        var node11 = node1.addChild(new IdTitleTreeItem(cnt++, "Node 1 1"));
        node11.addChild(new IdTitleTreeItem(cnt++, "Node 1 1 1"));
        node1.addChild(new IdTitleTreeItem(cnt++, "Node 1 2 with a loooooooooooooooooong text"));

        var node2 = rootNode.addChild(new IdTitleTreeItem(cnt++, "Node 2"));
        for (int i = 1; i <= 5; i++)
            node2.addChild(new IdTitleTreeItem(cnt++, "Node 2 "+ i));

        form.setResponseData("treeRootNode", rootNode);
    }

    // TODO: Some helper methods for testing parameters validatation and so on.
}
