// "use strict";

$$.param.address = new function() {
	const debug = $$.debug("param.address");

	// TODO: Move here all the functions below.
}

// добавляет контекстный поиск по улице городу
function addCustomCitySearch( selector ,cityIdSelector)
{
	// список с городами
	$( selector ).autocomplete({
		minLength: 1,
		source: function( request, response )
		{
			const url = "/user/directory/address.do?" + $$.ajax.requestParamsToUrl({ "addressCountryId": "1", "searchMode": "city", "addressCityTitle": request.term });

			$$.ajax.post(url).done((ajaxResponse) => {
				response($.map(ajaxResponse.data.list, function (item) {
					return { value: item.title, id: item.id };
				}));
			});
		},
		select: function( event, ui )
		{
			$( cityIdSelector ).val( ui.item.id ).change();
		}
	});
}

function addCustomQuarterSearch( selector , areaIdSelector ,cityId)
{
	// список с кварталами
	$( selector ).autocomplete({
		minLength: 1,
		source: function( request, response )
		{
			const url = "/user/directory/address.do?" + $$.ajax.requestParamsToUrl({
				"action": "address", "addressCountryId": "1", "selectTab": "quarter", "addressCityId": cityId, "searchMode": "item", "addressItemTitle": request.term
			});

			$$.ajax.post(url).done((ajaxResponse) => {
				response($.map(ajaxResponse.data.list, function (item) {
					return { value: item.title, id: item.id };
				}));
			});
		},
		select: function( event, ui )
		{
			$( areaIdSelector ).val( ui.item.id ).change();;
		}
	});
}

/**
 * Add context street search
 * @param {String} selector
 * @param {String} streetIdSelector
 */
function addCustomStreetSearch(selector, streetIdSelector) {
	$(selector).autocomplete({
		minLength: 3,
		source: function (request, response) {
			const url = "/user/directory/address.do?" + $$.ajax.requestParamsToUrl({ "method": "streetSearch", "page.pageSize": 100, "title": request.term });
			$$.ajax.post(url).done((ajaxResponse) => {
				response($.map(ajaxResponse.data.list, function (item) {
					return { label: item.addressCity.title + " - " + item.title, value: item.addressCity.title + " - " + item.title, id: item.id };
				}));
			});
		},
		select: function (event, ui) {
			$(streetIdSelector).val(ui.item.id);
		}
	});
}

/**
 * Add context house search
 * @param {String} selector CSS selector of house input
 * @param {String} streetIdSelector CSS selector of street ID input
 * @param {String} houseIdSelector CSS selector of house ID input
 */
function addCustomHouseSearch(selector, streetIdSelector, houseIdSelector) {
	$(selector).on("keyup", () => $(houseIdSelector).val(""));

	$(selector).autocomplete({
		minLength: 0,
		source: function (request, response) {
			const streetId = $(streetIdSelector).val();
			if (streetId > 0) {
				const url = "/user/directory/address.do?" + $$.ajax.requestParamsToUrl({ "method": "houseSearch", "streetId": streetId, "house": request.term });
				$$.ajax.post(url).done((ajaxResponse) => {
					response($.map(ajaxResponse.data.list, function (item) {
						return { label: item.houseAndFrac, value: item.houseAndFrac, id: item.id };
					}));
				});
			}
		},
		select: function (event, ui) {
			$(houseIdSelector).val(ui.item.id);
		}
	});
}

/**
 * Add context street and house searches
 * @param {String} containerSelector CSS selector of inputs container
 */
function addAddressSearch(containerSelector) {
	addCustomStreetSearch(containerSelector + " input[name=street]", containerSelector + " input[name=streetId]");
	addCustomHouseSearch(containerSelector + " input[name=house]", containerSelector + " input[name=streetId]", containerSelector + " input[name=houseId]");
}

//
// адресный справочник
//
function addressSearchCountry( f, e )
{
	f.form.elements['searchMode'].value = 'country';
	if (e == undefined || $$.keys.enterPressed(e))
	{
		f.form.elements['addressCountryId'].value = '';
		addressClearCity( f );
		$$.ajax.loadContent(f.form);
	}
	return false;
}

function addressSearchCity( f, e )
{
	f.form.elements['searchMode'].value = 'city';
	if (e == undefined || $$.keys.enterPressed(e))
	{
		f.form.elements['addressCityId'].value = '';
		addressClearItem( f );
		$$.ajax.loadContent(f.form);
	}
	return false;
}

function addressSearchItem( f, e )
{
	f.form.elements['searchMode'].value = 'item';
	if (e == undefined || $$.keys.enterPressed(e))
	{
		f.form.elements['addressItemId'].value = '';
		if ( f.form.elements['addressHouse'] )
		{
			addressClearHouse( f );
		};
		$$.ajax.loadContent(f.form);
	}
	return false;
}

function addressSearchHouse( f, e )
{
	f.form.elements['searchMode'].value = 'house';
	if (e == undefined || $$.keys.enterPressed(e))
	{
		$$.ajax.loadContent(f.form);
	}
	return false;
}


function addressClearCountry( f )
{
	f.form.elements['addressCountryId'].value = '';
	f.form.elements['addressCountryTitle'].value = '';
	addressClearCity( f );
	return true;
}

function addressClearCity( f )
{
	f.form.elements['addressCityId'].value = '';
	f.form.elements['addressCityTitle'].value = '';
	addressClearItem( f );
	return true;
}

function addressClearItem( f )
{
	f.form.elements['addressItemId'].value = '';
	f.form.elements['addressItemTitle'].value = '';
	if ( f.form.elements['addressHouse'] )
	{
		addressClearHouse( f );
	};
	return true;
}

function addressClearHouse( f )
{
	f.form.elements['addressHouseId'].value = '';
	f.form.elements['addressHouse'].value = '';
	return true;
}
