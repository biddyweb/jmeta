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
Plugin.prototype.handleJsonFragmentResponse = function(i, item){
    var command = item;
    this.commandList.push(command);
}
Plugin.prototype.loadInto = function(div){
    div.append("<h2>"+this.pluginName+"</h2>");
    var divNavigation = $("<ul id='navBarCommands' class='nav nav-tabs'></ul>");
    div.append(divNavigation);
    var divDisplay    =  $("<div class='container-fluid' style='clear:both'></div>");
    div.append(divDisplay);
    for(var i=0; i<this.commandList.length;i++){
        var command = this.commandList[i];
        var liRole  = $('<li id="'+command+'" class="navcommand" role="presentation"></li>');
        divNavigation.append(liRole);
        var linkCom = $(
                '<a href="#" tabindex="-1" role="menuitem" class="commandLink">'
                + command
                + '</a></li>');
        liRole.append(linkCom);
        var command = new Command(command, this, divDisplay);
        linkCom.click(this.displayCommand.bind(this, command));
        if(i==0)
            linkCom.click();
    }
}
Plugin.prototype.displayCommand = function(command, e){
    $("li.navcommand").each(function(i, item){$(item).removeClass("active")});
    $("#"+command.commandName).toggleClass("active");
    command.process();
}

