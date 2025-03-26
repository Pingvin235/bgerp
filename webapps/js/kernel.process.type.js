// "use strict";

$$.process.type = new function () {
    function selectedNode(el, nodeId) {
        $(el.parentNode).find("#" + nodeId + "_childs").toggle();
    }

    function selectedLeaf(el, nodeId) {
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

    // public functions
    this.selectedNode = selectedNode;
    this.selectedLeaf = selectedLeaf;
}