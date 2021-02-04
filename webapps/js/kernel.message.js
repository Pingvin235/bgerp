// "use strict";

$$.message = new function() {
	const debug = $$.debug("message");

	const editorTypeChanged = (typeComboUiid, editorUiid) => {
		var typeId = $(`#${typeComboUiid}`).find('input[name=typeId]').val();
		var $selectedTypeLi = $(`#${typeComboUiid} ul.drop li[value=$typeId]`);

		var editor = $selectedTypeLi.attr('editor');
		var $activeEditor = $(`#${editorUiid}`);
		var $editorParent = $activeEditor.parent();

		if (editor) {
			$activeEditor = $('form[id=\'${editorUiid}-' + editor + '\']');
		} else {
			$('#${editorUiid} div#subject').toggle( $selectedTypeLi.attr( 'subject' ) == 'true' );
			$('#${editorUiid} div#address').toggle( $selectedTypeLi.attr( 'address' ) == 'true' );
			$('#${editorUiid} div#attach').toggle( $selectedTypeLi.attr( 'attach' ) == 'true' );
		}

		$editorParent.find('>form').hide();
		$activeEditor.show();

		$('#${typeComboUiid}').detach().appendTo($activeEditor.find('#typeSelectContainer'));
	}

	// public functions
	this.editorTypeChanged = editorTypeChanged;
}

