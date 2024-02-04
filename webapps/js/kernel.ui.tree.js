/*
 * Tree UI elements.
 */
"use strict";

$$.ui.tree = new function () {
	// $$.ui.tree.single
	this.single = new function () {
		const treeSelector = ".tree-single";
		const itemSelector = ".item";
		const hiddenSelector = "input[type='hidden']";
		const titleSelector = ".title";
		const expanderSelector = ".expander";
		const childrenSelector = ">.children";

		const selectedClass = "selected";
		const openClass = "open";

		const hiddenNameKey = "hiddenName";
		const hiddenNameTitleKey = "hiddenNameTitle";

		/**
		 * Initializes a tree.
		 * @param {String} id container element' ID.
		 * @param {String} value current value's ID.
		 * @param {String} hiddenName name of hidden input with value's ID.
		 * @param {String} hiddenNameTitle name of hidden input with value's title.
		 */
		const init = (id, value, hiddenName, hiddenNameTitle) => {
			const tree = document.getElementById(id);

			$(tree)
				.data(hiddenNameKey, hiddenName)
				.data(hiddenNameTitleKey, hiddenNameTitle);

			const title = tree.querySelector("#title-" + value.replaceAll(".", "\\."));
			if (title) {
				select(title, value);
				open(title);
			}
		}

		/**
		 * Handles title click.
		 * @param {HTMLElement} title DOM element with title.
		 * @param {Number} value set value.
		 */
		const select = (title, value) => {
			if (!value)
				return;

			const $tree = $(title.closest(treeSelector));

			// mark selection
			$tree.find(titleSelector + "." + selectedClass).removeClass(selectedClass);
			$(title).addClass(selectedClass);

			const hiddenName = $tree.data(hiddenNameKey);
			const hiddenNameTitle = $tree.data(hiddenNameTitleKey);

			const $hiddenInputs = $tree.find(hiddenSelector)
			$hiddenInputs.filter("[name='" + hiddenName + "']").val(value);
			if (hiddenNameTitle)
				$hiddenInputs.filter("[name='" + hiddenNameTitle + "']").val(title.querySelector(".text").innerText);
		}

		/**
		 * Toggles visibility of child nodes.
		 * @param {HTMLElement} title title element.
		 */
		const expand = (title) => {
			$(title).find(expanderSelector).toggleClass(openClass);
			$(title).closest(itemSelector).find(childrenSelector).toggle();
		}

		/**
		 * Opens a node and all parents.
		 * @param {HTMLElement} title title element.
		 */
		const open = (title) => {
			$(title).find(expanderSelector).addClass(openClass);
			$(title).closest(itemSelector).find(childrenSelector).show();

			const parent = title.closest(itemSelector).parentElement.closest(itemSelector, treeSelector);
			if (parent && parent.matches(itemSelector))
				open(parent.querySelector(titleSelector));
		}

		/**
		 * Opens the root node.
		 * @param {String} id tree element id.
		 */
		const openRoot = (id) => {
			open(document.getElementById(id).querySelector(titleSelector));
		}

		// public functions
		this.init = init;
		this.expand = expand;
		this.select = select;
		this.openRoot = openRoot;
	}
}
