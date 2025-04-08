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

    const selectEdit = (uiid, typeId) => {
        const $selectedTitle = $(document.getElementById(uiid)).find('#' + typeId + '_title');
        const $parents = $selectedTitle.parents();

        let i = 0;
        let $span = $();
        do {
            $span = $($parents[i]).find(' > span');
            $span.click();
            i += 2;
        }
        while ($span.length > 0)
    }

    // public functions
    this.selectedNode = selectedNode;
    this.selectedLeaf = selectedLeaf;
    this.select = select;
    this.selectEdit = selectEdit;
}