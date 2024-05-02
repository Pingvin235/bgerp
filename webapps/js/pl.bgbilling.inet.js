/*
 * Plugin BGBilling. Module Inet.
 */
"use strict";

$$.bgbilling.inet = new function () {
	const serviceTypeChanged = (id) => {
		const item = document.getElementById(id).querySelector("li[selected]");
		const form = item.closest("form");

		// show related inputs
		$.each(item.attributes, function (index, attr) {
			$(form.querySelector("#" + attr.name)).toggle(attr.value === '1');
		});

		// device filter params
		form.deviceTypeIds.value = item.getAttribute('deviceTypeIds');
		form.deviceGroupIds.value = item.getAttribute('deviceGroupIds');
	}

	const ifaces = (form) => {
		const request = "/user/plugin/bgbilling/proto/inet.do?action=interfaceListGet" +
			"&billingId=" + form.billingId.value +
			"&moduleId=" + form.moduleId.value +
			"&deviceId=" + form.deviceId.value;
		$$.ajax.load(request, $(form).find(".deviceEdit"))
	}

	const setIface = ( port, title) => {
		const uid = $(".ifaceEditor").parent().attr('id').split('-')[0];
		$('#' + uid + '-ifaceId').attr('value', port);
		$('#' + uid + '-ifaceTitle').attr('value', title);
		$(".ifaceEditor").parent().text('');
	}

	const vlans = (form) => {
		const request = "/user/plugin/bgbilling/proto/inet.do?action=getFreeVlan" +
			"&billingId=" + form.billingId.value +
			"&moduleId=" + form.moduleId.value +
			"&dateFrom=" + form.dateFrom.value +
			"&dateTo=" + form.dateTo.value +
			"&deviceId=" + form.deviceId.value;
		$$.ajax.post(request).done((result) => {
			const vlan = result.data.vlan;
			if (vlan)
				form.vlan.value = vlan;
		})
	}

	const devices = (form) => {
		const request = "/user/plugin/bgbilling/proto/inet.do?action=devicesGet" +
			"&billingId=" + form.billingId.value +
			"&moduleId=" + form.moduleId.value +
			"&deviceId=" + form.deviceId.value +
			"&deviceTypeIds=" + encodeURIComponent(form.deviceTypeIds.value) +
			"&deviceGroupIds=" + encodeURIComponent(form.deviceGroupIds.value);
		$$.ajax.load(request, $(form).find(".deviceEdit"))
	}

	const setDevice = (form) => {
		let id = form.deviceIdSelect.value;
		let title = form.deviceTitleSelect.value;
		$(form).find(".deviceId").attr('value', id);
		$(form).find(".deviceTitle").attr('value', title);
		$(form).find(".deviceEdit").text('');
	}

	/**
	 * Shows device manage command execution result on a dialog
	 * @param {String} dialogId dialog div element ID
	 * @param {String} title command title
	 * @param {String} response command execution result
	 */
	const deviceManageResponse = (dialogId, title, response) => {
		/* from ToolDialog.java in BGBilling
			if ( result.startsWith( "browse:" ) )
			{
				String result2 = result.substring( 7 );
				browse( result2 );
				this.dispose();
			}
			else if ( result.startsWith( "telnet:" ) )
			{
				String result2 = result.substring( 7 );
				telnet( result2 );
				this.dispose();
			}
			else if ( result.startsWith( "ping:" ) )
			{
				String result2 = result.substring( 5 );
				ping( result2 );
				this.dispose();
			}
			else if( result.startsWith( COMMAND_EXEC ) )
			{
				String command = result.substring( COMMAND_EXEC.length() );
				exec( command );
				this.dispose();
			}
			else
			{
				if ( result.startsWith( "<html>" ) )
				{
					textArea.setContentType( "text/html" );
				}
				textArea.setText( result );
				if ( !this.isShowing() )
				{
					this.showDialog();
				}
			}
		*/
		if (response.indexOf('telnet:') >= 0) {
			response = response.replace(':', ':///').replace(' ', ':');
			window.open(response);
		} else {
			// no html markup
			if (response.indexOf('<') < 0)
				response = "<pre style='white-space: break-spaces; font-family: monospace;'>" + response.replaceAll('\t', '    ') + "</pre>";

			$(document.getElementById(dialogId)).html(response).dialog({
				modal: true,
				width: 600,
				height: 400,
				draggable: true,
				title: "Результат '" + title + "'",
				position: { my: "center top", at: "center top+100px", of: window }
			});
		}
	}

	// public functions
	this.serviceTypeChanged = serviceTypeChanged;
	this.vlans = vlans;
	this.ifaces = ifaces;
	this.setIface = setIface;
	this.devices = devices;
	this.setDevice = setDevice;
	this.deviceManageResponse = deviceManageResponse;
}
