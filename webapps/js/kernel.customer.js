/*
 * Customers. 
 */
$$.customer = new function() {
	const open = (id) => {
		$$.shell.contentLoad("customer#" + id);
	};
	
	// public functions
	this.open = open;
};


function openCustomer(id) {
	console.warn("Deprecated");
	$$.customer.open(id);
}


// создаёт контрагента и открывает его редактор
function createCustomerAndEdit( sender )
{
	var url = "customer.do?action=customerCreate";
	sendAJAXCommandAsync( url, null,
		function(result)
		{
			if( result )
			{
				var customerId = result.data.customer.id;
				openCustomer( customerId );
				
				url = "customer.do?action=customerGet&id=" + customerId + "&returnUrl=" + encodeURIComponent( "customer.do?id=" + customerId );
				openUrlContent( url );
			}
		},
		sender,
		500 );
}

function addCustomerSearch( $selector )
{
	// контекстный поиск по наименованию
	$selector.autocomplete({
		minLength: 5,
		delay: 1000,
		source: function( request, response ) 
		{
			var ajaxResponse = sendAJAXCommandWithParams( "/user/customer.do?", { "action" : "customerTitleList", "title": request.term } );
			
			if( ajaxResponse )
			{
				response( $.map( ajaxResponse.data.list, function( item ) 
				{
					return { label: item, value: item, id: item };
				}));				
			};										
		}
	});
}

function buildOpenedCustomerList( $selector, currentCustomer )
{
	var $ul = $selector.find( 'ul.drop' );
	
	$ul.html( "" );
	
	var options;
	
	if( currentCustomer && currentCustomer.id > 0 )
	{	
		options = "<li value='-1'>----</li>";
		/*options += "<li value='" + currentCustomer.id + "'>" + currentCustomer.title + "</li>";*/
		
		$selector.find( 'input[type=hidden]' ).attr( "value", currentCustomer.id );
		$selector.find( '.text-value' ).text( currentCustomer.title );
	}
	else
	{
		options = "";
		$selector.find( 'input[type=hidden]' ).attr( "value", 0 );
		/*options = "<li value='-1'>----</li>";*/
	}
	
	var openedCustomers = openedObjectList( { "typesInclude" : ["customer"] } );
	for( var c in openedCustomers )
	{
		if( currentCustomer == undefined || openedCustomers[c].id != currentCustomer.id )
		{
			options += "<li value='" + openedCustomers[c].id + "'>" + openedCustomers[c].title + "</li>";
		}
	}
	
	$ul.html( options );	
}

// обработка клиентских событий
addEventProcessor( 'ru.bgcrm.event.client.CustomerTitleChangedEvent', processCustomerClientEvents );
addEventProcessor( 'ru.bgcrm.event.client.CustomerOpenEvent', processCustomerClientEvents );

function processCustomerClientEvents( event )
{
	if( event.className == 'ru.bgcrm.event.client.CustomerTitleChangedEvent' )
	{
		customerChangeTitle( event.id, event.title );
	}
	else if( event.className == 'ru.bgcrm.event.client.CustomerOpenEvent' )
	{
		openCustomer( event.id );
	}
}

// изменяет название контрагента на странице
function customerChangeTitle( customerId, customerTitle )
{
 /* 	var $a = $( "a[href=#customerTabs-" + customerId + "]" );
  	$a.text( truncValue( customerTitle, 20 ) );
  	$a.attr( "fullTitle", customerTitle );*/
  	
	/*$("button#customer-" + customerId + " span").text( customerTitle );*/
  	$("#customer_title_" + customerId).text( customerTitle );
}

// запросы на сервер
function addCustomerLink( customerId, linkedObjectType, linkedObjectId, linkedObjectTitle )
{
	var url = "/user/link.do?action=addLink&id=" + customerId;
	return sendAJAXCommandWithParams( url, { "objectType" : "customer", "linkedObjectType" : linkedObjectType, "linkedObjectId" : linkedObjectId, "linkedObjectTitle" : linkedObjectTitle });
}

function deleteCustomerLink( customerId, linkedObjectType, linkedObjectId )
{
	var url = "/user/link.do?action=deleteLink&id=" + customerId;
	return sendAJAXCommandWithParams( url, { "objectType" : "customer", "linkedObjectType" : linkedObjectType, "linkedObjectId" : linkedObjectId });
}

function deleteCustomerLinkTo( linkedObjectType, linkedObjectId )
{
	var url = "/user/link.do?action=deleteLinksTo";
	return sendAJAXCommandWithParams( url, { "objectType" : "customer", "linkedObjectType" : linkedObjectType, "linkedObjectId" : linkedObjectId });
}
