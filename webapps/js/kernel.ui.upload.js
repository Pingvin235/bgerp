/*
 * Temporary files upload UI.
 */
"use strict";

$$.ui.upload = new function () {
	/**
	 * File upload handler.
	 * @param {*} form editor form with files list.
	 * @param {*} uploadFormId file upload form ID.
	 */
	const add = (form, uploadFormId) => {
		const uploadList = uploadListSelect(form);

		const id = uploadList.parentNode.querySelector("input[type='hidden'][name='addFileId']").value;

		// upload
		if (id == 0) {
			$$.ajax
				.fileUpload(document.getElementById(uploadFormId))
				.done((files) => uploadListAdd(form, files));
		}
		// already uploaded or announced
		else{
			const paramName = id > 0 ? "fileId" : "announcedFileId";

			const li = form.querySelector("li[value='" + id + "']");

			const $div = $("<div>" +
					"<input type='hidden' name='" + paramName + "' value='" + id + "'/>" +
					"<button type='button' class='btn-white btn-small icon mr05' onclick=''><span class='ti-trash'></span></button>" +
					li.textContent +
				"</div>");
			uploadList.append($div[0]);

			$(li).hide();
			$(form.querySelector("li[value='0']")).click();

			$div.find("button").click(() => {
				$div.remove();
				$(li).show();
			});
		}
	}

	/**
	 * Listener for paste events for attachments.
	 * @param form editor form with files list.
	 * @param uploadFormId file upload form ID.
	 */
	const pasteListener = (form, uploadFormId) => {
		form.addEventListener('paste', (e) => {
			if (e && e.clipboardData && e.clipboardData.files && e.clipboardData.files.length)
				filesUpload(form, document.getElementById(uploadFormId), Array.from(e.clipboardData.files));
		});
	}

	/**
	 * Listener for drop events for attachments.
	 * @param form editor form with files list.
	 * @param uploadFormId file upload form ID.
	 */
	const dropListener = (form, uploadFormId) => {
		form.addEventListener('drop', (e) => {
			e.preventDefault();

			const files = [];
			if (e.dataTransfer.items)
				Array.from(e.dataTransfer.items).forEach((item) => {
					if (item.kind === "file")
						files.push(item.getAsFile());
				});
			else
				Array.from(e.dataTransfer.files).forEach((file) => {
					files.push(file);
				});

			filesUpload(form, document.getElementById(uploadFormId), files);
		})
	}

	const filesUpload = (form, uploadForm, files) => {
		// array with uploaded files: id and title
		const uploadedFiles = [];

		const sendNextFile = (file) => {
			if (file)
				$$.ajax
					.fileSend(uploadForm, file)
					.done((response) => {
						if (response.data && response.data.file)
							uploadedFiles.push(response.data.file);
					})
					.always(() => sendNextFile(files.shift()));
			else
				uploadListAdd(form, uploadedFiles);
		}

		sendNextFile(files.shift());
	}

	const uploadListAdd = (form, fileIdTitleList) => {
		const uploadList = uploadListSelect(form);
		fileIdTitleList.forEach((file) => {
			const deleteCode = "$$.ajax.post('/user/file.do?action=temporaryDelete&id=" + file.id + "').done(() => {$(this.parentNode).remove()})";
			$(uploadList).append(
				"<div>" +
					"<input type=\"hidden\" name=\"tmpFileId\" value=\"" + file.id + "\"/>" +
					"<button class=\"btn-white btn-small mr05 icon\" type=\"button\" onclick=\"" + deleteCode + "\"><i class=\"ti-trash\"></i></button> " + file.title +
				"</div>");
		});
	}

	const uploadListSelect = (form) => {
		return form.querySelector(".upload-list");
	}

	// public objects
	this.add = add;
	this.pasteListener = pasteListener;
	this.dropListener = dropListener;
}
