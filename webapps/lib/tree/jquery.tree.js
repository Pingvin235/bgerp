/**
 * jQuery Tree Control
 *
 * @author Maxim Vasiliev
 */
(function ($) {

    var CLASS_JQUERY_TREE = 'jquery-tree';
    var CLASS_JQUERY_TREE_CONTROLS = 'jquery-tree-controls';
    var CLASS_JQUERY_TREE_COLLAPSE_ALL = 'jquery-tree-collapseall';
    var CLASS_JQUERY_TREE_EXPAND_ALL = 'jquery-tree-expandall';
    var CLASS_JQUERY_TREE_COLLAPSED = 'jquery-tree-collapsed';
    var CLASS_JQUERY_TREE_HANDLE = 'jquery-tree-handle';
    var CLASS_JQUERY_TREE_TITLE = 'jquery-tree-title';
    var CLASS_JQUERY_TREE_NODE = 'jquery-tree-node';
    var CLASS_JQUERY_TREE_LEAF = 'jquery-tree-leaf';
    var CLASS_JQUERY_TREE_CHECKED = 'jquery-tree-checked';
    var CLASS_JQUERY_TREE_UNCHECKED = 'jquery-tree-unchecked';
    var CLASS_JQUERY_TREE_CHECKED_PARTIAL = 'jquery-tree-checked-partial';
    var CLASS_JQUERY_TREE_FILTER_BUTTON = 'jquery-tree-filter-button';
    var CLASS_JQUERY_TREE_FILTER_INPUT = 'jquery-tree-filter-input';
    var CLASS_JQUERY_TREE_SELECT_NODE = 'select_node';

    var COLLAPSE_ALL_CODE = '<span class="' + CLASS_JQUERY_TREE_COLLAPSE_ALL + ' ti-plus" title="Collapse all"></span>';
    var EXPAND_ALL_CODE = '<span class="' + CLASS_JQUERY_TREE_EXPAND_ALL + ' ti-minus" title="Expand all"></span>';
    var FILTER_CODE = '<div class="filter"><input class="' + CLASS_JQUERY_TREE_FILTER_INPUT + '" type="text" placeholder="Filter"/><button class="ml05 btn-white icon ' + CLASS_JQUERY_TREE_FILTER_BUTTON + '" type="button" title="Filter"><i class="ti-filter"></i></button></div>';
    var TREE_CONTROLS_CODE = '<div class="mb05 ' + CLASS_JQUERY_TREE_CONTROLS + '">' +
        FILTER_CODE +
        COLLAPSE_ALL_CODE +
        EXPAND_ALL_CODE +
        '</div>';

    var TREE_NODE_HANDLE_CODE = '<span class="' + CLASS_JQUERY_TREE_HANDLE + '">+</span>';
    var TREE_NODE_HANDLE_COLLAPSED = "+";
    var TREE_NODE_HANDLE_EXPANDED = "&minus;";

    var SINGLE_SELECT = false;

    $.fn.extend({

        /**
         * Делает дерево из структуры вида:
         * <ul>
         *   <li><label><input type="checkbox" />Item1</label></li>
         *   <li>
         *     <label><input type="checkbox" />ItemWithSubitems</label>
         *     <ul>
         *       <li><label><input type="checkbox" />Subitem1</label></li>
         *     </ul>
         *   </li>
         * </ul>
         */
        Tree: function (o) {

            if( o!==undefined) {
                SINGLE_SELECT = o['singleSelect'];
            }

            var tree = $(this);

            // Добавим контролы для всего дерева (все свернуть, развернуть и т.д.), и добавим класс
            $(this)
                .addClass(CLASS_JQUERY_TREE)
                .before(TREE_CONTROLS_CODE)
                .prev('.' + CLASS_JQUERY_TREE_CONTROLS)
                .find('.' + CLASS_JQUERY_TREE_COLLAPSE_ALL).click(function () {
                    $(this).parent().next('.' + CLASS_JQUERY_TREE)
                        .find('li:has(ul)')
                        .addClass(CLASS_JQUERY_TREE_COLLAPSED)
                        .find('.' + CLASS_JQUERY_TREE_HANDLE)
                        .html(TREE_NODE_HANDLE_COLLAPSED);
                })

                .parent('.' + CLASS_JQUERY_TREE_CONTROLS).find('.' + CLASS_JQUERY_TREE_EXPAND_ALL)
                .click(function () {
                    $(this).parent().next('.' + CLASS_JQUERY_TREE)
                        .find('li:has(ul)')
                        .removeClass(CLASS_JQUERY_TREE_COLLAPSED)
                        .find('.' + CLASS_JQUERY_TREE_HANDLE)
                        .html(TREE_NODE_HANDLE_EXPANDED);
                })

                .parent('.' + CLASS_JQUERY_TREE_CONTROLS).find('.' + CLASS_JQUERY_TREE_FILTER_INPUT)
                .keypress(function (event) {
                    if (enterPressed(event)) {
                        event.preventDefault();
                        filter(tree, $(this).val().toLowerCase());
                        $(tree).prev('.' + CLASS_JQUERY_TREE_CONTROLS).find('span.' + CLASS_JQUERY_TREE_EXPAND_ALL).click();
                    }
                });

            $('.' + CLASS_JQUERY_TREE_CONTROLS).find('button.' + CLASS_JQUERY_TREE_FILTER_BUTTON).click(function () {
                filter(tree, $(this).prev().val().toLowerCase());
                $(tree).prev('.' + CLASS_JQUERY_TREE_CONTROLS).find('span.' + CLASS_JQUERY_TREE_EXPAND_ALL).click();
            });

            $('li', this).find(':first').addClass(CLASS_JQUERY_TREE_TITLE)
                .closest('li').addClass(CLASS_JQUERY_TREE_LEAF);

            // Для всех элементов, являющихся узлами (имеющих дочерние элементы)...
            $('li:has(ul:has(li))', this).find(':first')
                // ... добавим элемент, открывающий/закрывающий узел
                .before(TREE_NODE_HANDLE_CODE)
                // ... добавим к контейнеру класс "узел дерева" и "свернем".
                .closest('li')
                .addClass(CLASS_JQUERY_TREE_NODE)
                .addClass(CLASS_JQUERY_TREE_COLLAPSED)
                .removeClass(CLASS_JQUERY_TREE_LEAF);

            // ... повесим обработчик клика
            $('.' + CLASS_JQUERY_TREE_HANDLE, this).bind('click', function () {
                var leafContainer = $(this).parent('li');
                var leafHandle = leafContainer.find('>.' + CLASS_JQUERY_TREE_HANDLE);

                leafContainer.toggleClass(CLASS_JQUERY_TREE_COLLAPSED);

                if (leafContainer.hasClass(CLASS_JQUERY_TREE_COLLAPSED))
                    leafHandle.html(TREE_NODE_HANDLE_COLLAPSED);
                else
                    leafHandle.html(TREE_NODE_HANDLE_EXPANDED);
            });

            // Добавляем обработку клика по чекбоксам
            $('input:checkbox', this).click(function () {

                if (SINGLE_SELECT) {
                    var test = this;
                    $(tree).find("> ul > li").each(function () {

                        $(this).closest('li').find('input:checkbox').each(function () {
                            if (this != test) {
                                this.checked = 0;
                                checkCheckbox(this);
                                setLabelClass(this);
                            }
                        });
                    })
                }

                checkCheckbox(this);
                setLabelClass(this);
            })
                // Выставляем чекбоксам изначальные классы
                .each(function () {
                    setLabelClass(this);
                });

            $(this).find('li').each(function () {
                checkNodeSelected($(this));
            });
        }
    })
    ;

    /**
     * Проверяет и возвращает выделенность узла, 0 - не выбран, 1 - частично, 2 - полностью.
     * Одновременно маркирует его соответственно выделенности.
     */
    function checkNodeSelected($node) {
        var result = null;

        var checked = $node.find("> label > input:checkbox").is(":checked");

        var $subNodes = $node.find("> ul > li");
        if ($subNodes.length == 0) {
            result = checked ? 2 : 0;
        }
        else {
            $subNodes.each(function () {
                var subChecked = checkNodeSelected($(this));
                if (result == null) {
                    result = subChecked;
                }
                else {
                    result = subChecked == result ? result : 1;
                }
            });
        }

        var $label = $node.find("> label");
        if (result == 1) {
            $label
                .addClass(CLASS_JQUERY_TREE_CHECKED_PARTIAL)
                .removeClass(CLASS_JQUERY_TREE_UNCHECKED);
        }
        else if (result == 2) {
            $label
                .addClass(CLASS_JQUERY_TREE_CHECKED)
                .removeClass(CLASS_JQUERY_TREE_UNCHECKED);

            $node.find("> label > input:checkbox").attr('checked', 'true');
        }

        return result;
    }

    /**
     * Рекурсивно проверяет, все ли чекбоксы в поддереве родительского узла выбраны.
     * Если ни один чекбокс не выбран - снимает чек с родительского чекбокса
     * Если хотя бы один, но не все - выставляет класс CLASS_JQUERY_TREE_CHECKED_PARTIAL родительскому чекбоксу
     * Если все - ставит чек на родительский чекбокс
     *
     * @param {Object} checkboxElement текущий чекбокс
     */
    function checkParentCheckboxes(checkboxElement) {
        if (typeof checkboxElement == 'undefined' || !checkboxElement)
            return;

        // проверим, все ли чекбоксы выделены/частично выделены на вышележащем уровне
        var closestNode = $(checkboxElement).closest('ul');
        var allCheckboxes = closestNode.find('input:checkbox');
        var checkedCheckboxes = closestNode.find('input:checkbox:checked');

        var allChecked = allCheckboxes.length == checkedCheckboxes.length;

        var parentCheckbox = closestNode.closest('li').find('>.' + CLASS_JQUERY_TREE_TITLE + ' input:checkbox');

        if (parentCheckbox.length > 0) {
            parentCheckbox.get(0).checked = allChecked;

            if (!allChecked && checkedCheckboxes.length > 0)
                parentCheckbox.closest('label')
                    .addClass(CLASS_JQUERY_TREE_CHECKED_PARTIAL)
                    .removeClass(CLASS_JQUERY_TREE_CHECKED)
                    .removeClass(CLASS_JQUERY_TREE_UNCHECKED);
            else if (allChecked)
                parentCheckbox.closest('label')
                    .removeClass(CLASS_JQUERY_TREE_CHECKED_PARTIAL)
                    .removeClass(CLASS_JQUERY_TREE_UNCHECKED)
                    .addClass(CLASS_JQUERY_TREE_CHECKED);
            else
                parentCheckbox.closest('label')
                    .removeClass(CLASS_JQUERY_TREE_CHECKED_PARTIAL)
                    .removeClass(CLASS_JQUERY_TREE_CHECKED)
                    .addClass(CLASS_JQUERY_TREE_UNCHECKED);

            checkParentCheckboxes(parentCheckbox.get(0));
        }
    }

    /**
     * Если у текущего чекбокса есть дочерние узлы - меняет их состояние
     * на состояние текущего чекбокса
     *
     * @param {Object} checkboxElement текущий чекбокс
     */
    function checkCheckbox(checkboxElement) {
        // чекнем/анчекнем нижележащие чекбоксы
        $(checkboxElement).closest('li').find('input:checkbox').each(function () {
            this.checked = checkboxElement.checked;
            setLabelClass(this);
        });
        checkParentCheckboxes(checkboxElement);
    };

    /**
     * Выставляет класс лейблу в зависимости от состояния чекбокса
     *
     * @param {Object} checkboxElement чекбокс
     */
    function setLabelClass(checkboxElement) {
        isChecked = checkboxElement.checked;

        if (isChecked) {
            $(checkboxElement).closest('label')
                .addClass(CLASS_JQUERY_TREE_CHECKED)
                .removeClass(CLASS_JQUERY_TREE_UNCHECKED)
                .removeClass(CLASS_JQUERY_TREE_CHECKED_PARTIAL);
        }
        else {
            $(checkboxElement).closest('label')
                .addClass(CLASS_JQUERY_TREE_UNCHECKED)
                .removeClass(CLASS_JQUERY_TREE_CHECKED)
                .removeClass(CLASS_JQUERY_TREE_CHECKED_PARTIAL);
        }
    };

    /**
     * Фильтрует листья дерева по совпадению по подстроке из параметра e
     */
    function filter(tree, e) {
        var node = "li";
        $(tree).find(node).show();

        if (e.length > 0) {
            $(tree).find(node).each(function () {
                var text = $(this).text().toLowerCase();

                if (text.indexOf(e) == -1) {
                    $(this).hide();
                }
                else {
                    if ($(this).parents(node).length > 0 && !$(this).parents(node).first().is(':visible')) {
                        $(this).parents(node).show();
                    }
                }
            });
        }
        else
    	{
        	$(tree).find("li:hidden").each(function () { $(this).show() });
    	}
    }
})
(jQuery);
