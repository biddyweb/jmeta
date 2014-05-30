function refreshInterface(event) {
	var command = event.currentTarget.textContent;
	alert(command);
}

// Helper function, generate html String to appends in a Dropdown menu.
function hMenuitem(item,callback){
		var str = $('<li role="presentation">'
				+ '<a href="#" tabindex="-1" role="menuitem">'
				+ item
				+ '</a></li>');
		str.on("click", callback);
		return str;
}

// Refresh command list menu.
function refreshCommandMenu() {
		var jqxhr = $.getJSON('getCommandList')
				.done(function(data) {
						$.each( data, function( i, item ) {
								element = hMenuitem(item, refreshInterface);
								$('#commands_menu').append(element);
						});
				})
		.fail(function() {
				$('#commands_menu').append("<li>Webservice Error !</li>");
		})
}

// This function is call when DOM is ready
$(function() {
		refreshCommandMenu();
});
