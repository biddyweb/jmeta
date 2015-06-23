/**********************************************************************************
 *
 * Plugin class
 *
 *********************************************************************************/
var Plugin = function (pluginName){
    this.pluginName   = pluginName;
    this.commandList = new Array(0);

    this.fetchCommands();
}
Plugin.prototype.commandList = null;
Plugin.prototype.pluginName  = null;

Plugin.prototype.fetchCommands = function(){
     var pluginsNames = $.getJSON('interface/'+this.pluginName)
            .done(this.handleJsonResponse.bind(this))
            .fail(function() {
                    $('#commands_menu').append("<li>Webservice Error !</li>");
            });
}

Plugin.prototype.handleJsonResponse = function(data){
    $.each( data,  this.handleJsonFragmentResponse.bind(this));
}
Plugin.prototype.handleJsonFragmentResponse = function(item){
    var command = item;
    this.commandList.push(command);
}


