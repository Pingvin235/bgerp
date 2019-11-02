function fias_clearStreetInput()
{
	fias_searchAddressStreetByTerm();
	fias_searchSimilarStreet($('select[name=streetId]').find('option:selected').text());
	openUrlTo(formUrl($('#linkStreetForm')),$('#linkStreetForm').next());
}

function fias_searchFiasStreetByTerm()
{
	var titleTerm = $("input[name=fiasStreet]").val();
	if(titleTerm.length > 1)
	{
		var selector = $("select[name=fiasStreetId]");
		var cityId = $('select[name=cityId]').find('option:selected').val();
		var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/fias.do?", { "action" : "searchStreetByTerm", "titleTerm": titleTerm , "cityId": cityId } );
		if( ajaxResponse )
		{ 
			selector.find('option').remove();
			var streetList = ajaxResponse.data.list;
			for(var i=0; i<streetList.length ; i++)
			{
				selector.append("<option value="+ streetList[i].id +">" + streetList[i].crmCitytitle + " - " + streetList[i].title + " ("+streetList[i].shortName+")" + "</option>");
			}
		}
	}
}

function fias_searchSimilarStreet(title)
{
	var selector = $("select[name=fiasStreetId]");
	var cityId = $('select[name=cityId]').find('option:selected').val();
	var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/fias.do?", { "action" : "searchSimilarStreet", "title":  title, "cityId": cityId } );
	if( ajaxResponse )
	{ 
		selector.find('option').remove();
		var streetList = ajaxResponse.data.list;
		for(var i=0; i<streetList.length ; i++)
		{
			selector.append("<option value="+ streetList[i].id +">" + streetList[i].crmCitytitle + " - " + streetList[i].title + " ("+streetList[i].shortName+")" + "</option>");
		}
	}
}

function fias_searchAddressStreetByTerm()
{
	var titleTerm = $("input[name=crmStreet]").val();
	if(titleTerm.length > 1)
	{
		var selector = $("select[name=crmStreetId]");
		var cityId = $('select[name=cityId]').find('option:selected').val();
		var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/fias.do?", { "action" : "searchAddressStreet", "title": titleTerm, "cityId": cityId } );
		if( ajaxResponse )
		{ 
			selector.find('option').remove();
			var streetList = ajaxResponse.data.list;
			for(var i=0; i<streetList.length ; i++)
			{
				selector.append("<option value="+ streetList[i].id +">" + streetList[i].addressCity.title + " - " + streetList[i].title + "</option>");
			}
		}
	}
}

function fias_fillLinkStreetList(cityId,noLimit)
{
	var selector = $('select[name=streetId]');
	var ajaxResponse = sendAJAXCommandWithParams( "/user/plugin/fias.do?", { "action" : "linkStreetList", "cityId": cityId, "noLimit": noLimit } );
	if( ajaxResponse )
	{ 
		selector.find('option').remove();
		var streetList = ajaxResponse.data.list;
		for(var i=0; i<streetList.length ; i++)
		{
			selector.append("<option value="+ streetList[i].id +">" + streetList[i].title + " ("+streetList[i].shortName+")" + "</option>");
		}
	}
}
