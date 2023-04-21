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
			var ajaxResponse = sendAJAXCommandWithParams( "/user/directory/address.do?", { "addressCountryId" : "1", "searchMode": "city","addressCityTitle":request.term } );
			if( ajaxResponse )
			{
				response( $.map( ajaxResponse.data.list, function( item )
				{
					return { value: item.title, id: item.id };
				}));
			};
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
			var ajaxResponse = sendAJAXCommandWithParams( "/user/directory/address.do?", {"action":"address", "addressCountryId": "1", "selectTab" : "quarter", "addressCityId" : cityId, "searchMode": "item", "addressItemTitle": request.term} );
			if( ajaxResponse )
			{
				response( $.map( ajaxResponse.data.list, function( item )
				{
					return { value: item.title, id: item.id };
				}));
			};
		},
		select: function( event, ui )
		{
			$( areaIdSelector ).val( ui.item.id ).change();;
		}
	});
}


// добавляет контекстный поиск по улице инпуту
function addStreetSearch( selector )
{
	// список с улицами
	$( selector + " input[name='street']" ).autocomplete({
		minLength: 3,
		source: function( request, response )
		{
			var ajaxResponse = sendAJAXCommandWithParams( "/user/directory/address.do?", { "action" : "streetSearch", "title": request.term, "page.pageIndex": "0" } );
			if( ajaxResponse )
			{
				response( $.map( ajaxResponse.data.list, function( item )
				{
					return { label: item.addressCity.title + " - " + item.title, value: item.addressCity.title + " - " + item.title, id: item.id };
				}));
			};
		},
		select: function( event, ui )
		{
			this.form.elements['streetId'].value = ui.item.id;
		}
	});
}

//добавляет контекстный поиск по улице инпуту без формы
function addCustomStreetSearch( selector, streetIdSelector )
{
	// список с улицами
	$( selector ).autocomplete({
		minLength: 3,
		source: function( request, response )
		{
			var ajaxResponse = sendAJAXCommandWithParams( "/user/directory/address.do?", { "action" : "streetSearch", "title": request.term } );
			if( ajaxResponse )
			{
				response( $.map( ajaxResponse.data.list, function( item )
				{
					return { label: item.addressCity.title + " - " + item.title, value: item.addressCity.title + " - " + item.title, id: item.id };
				}));
			};
		},
		select: function( event, ui )
		{
			$( streetIdSelector ).val( ui.item.id );
		}
	});
}

//добавляет контекстный поиск по дому инпуту
function addHouseSearch( formSelector )
{
	const form = document.querySelector(formSelector);

	form.house.addEventListener("keyup", () => form.houseId.value = "");

	$(form.house).autocomplete({
		minLength: 0,
		source: function( request, response )
		{
			const streetId = form.streetId.value;
			if( streetId > 0 )
			{
				var ajaxResponse = sendAJAXCommandWithParams( "/user/directory/address.do?", { "action" : "houseSearch", "streetId" : streetId, "house": request.term } );
				if( ajaxResponse )
				{
					response( $.map( ajaxResponse.data.list, function( item )
					{
						return { label: item.houseAndFrac, value: item.houseAndFrac, id: item.id };
					}));
				};
			}
		},
		select: function( event, ui )
		{
			form.houseId.value = ui.item.id;
		}
	});
}

//добавляет контекстный поиск по дому инпуту
function addCustomHouseSearch( selector, streetIdSelector, houseIdSelector )
{
	$( selector ).autocomplete({
		minLength: 1,
		source: function( request, response )
		{
			var streetId = $( streetIdSelector ).val();
			if( streetId > 0 )
			{
				var ajaxResponse = sendAJAXCommandWithParams( "/user/directory/address.do?", { "action" : "houseSearch", "streetId" : streetId, "house": request.term } );
				if( ajaxResponse )
				{
					response( $.map( ajaxResponse.data.list, function( item )
					{
						return { label: item.houseAndFrac, value: item.houseAndFrac, id: item.id };
					}));
				};
			}
		},
		select: function( event, ui )
		{
			$( houseIdSelector ).val( ui.item.id );
		}
	});
}

// добавляет контекстный поиск по улице и дому
function addAddressSearch( selector )
{
	addStreetSearch( selector );
	addHouseSearch( selector );
}

//
// адресный справочник
//
function addressSearchCountry( f, e )
{
	f.form.elements['searchMode'].value = 'country';
	if ( e == undefined || enterPressed( e ) )
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
	if ( e == undefined || enterPressed( e ) )
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
	if ( e == undefined || enterPressed( e ) )
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
	if ( e == undefined || enterPressed( e ) )
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
