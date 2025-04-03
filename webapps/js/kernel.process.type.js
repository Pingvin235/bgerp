// "use strict";

$$.process.type = new function () {
    const selectedNode = (el, nodeId) => {
        $(el.parentNode).find("#" + nodeId + "_childs").toggle();
    }

    const selectedLeaf = (el, nodeId) => {
        let parent = el.parentNode;
        while (parent.tagName != 'FORM') {
            parent = parent.parentNode;
        }
        $(parent).find("input[name='typeId']").attr("value", nodeId);

        $(parent).find("span").css("font-weight", "").css("color", "");
        $(el).css("font-weight", "bold").css("color", "blue");

        $$.ajax.load("/user/process.do?method=processCreateGroups&typeId=" + nodeId,
                $(el).closest("#typeTree").parent().find("#groupSelect"));
    }

    const select = (uiid) => {
        const $uiid = $(document.getElementById(uiid));
        const $selectedLeaf = $uiid.find('.treeItemSelected');
        if ($selectedLeaf.length) {
            $selectedLeaf.click();
        } else
            $uiid.find('span.treeNode').first().click();
    }

    // public functions
    this.selectedNode = selectedNode;
    this.selectedLeaf = selectedLeaf;
    this.select = select;
}