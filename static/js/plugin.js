/**********************************************************************************
 *
 * Plugin class
 * @param pluginName the plugin name
 *********************************************************************************/
var Plugin = function (pluginName){
    this.pluginName   = pluginName;
    this.commandList  = new Array(0);
    this.currentCmd   = null;
    this.divDisplay   = null;
    //get plugin list
    this.fetchCommands();
}
Plugin.prototype.commandList    = null;
Plugin.prototype.pluginName     = null;
Plugin.prototype.currentCmd     = null;
Plugin.prototype.divDisplay     = null;

/**
 * call the web service to get the command list
 */
Plugin.prototype.fetchCommands = function(){
    console.log("-> Plugin :"+this.pluginName+" fetchCommands");
     var pluginsNames = $.getJSON('interface/'+this.pluginName)
            .done(this.handleJsonResponse.bind(this))
            //in case of failure append an invisble message TODO
            .fail(function() {
                    $('#commands_menu').append("<li>Webservice Error !</li>");
            });
}

/**
 * parse json response after fetching succes
 * @param data json response
 */
Plugin.prototype.handleJsonResponse = function(data){
    console.log("-> Plugin :"+this.pluginName+" handleJsonResponse");
    //For each data parse it
    $.each( data,  this.handleJsonFragmentResponse.bind(this));
}

/**
 * parse a json fragment to get a command name
 * create a commandList wich contains a list of command names
 * @param i integer index
 * @param item a json data object
 */
Plugin.prototype.handleJsonFragmentResponse = function(i, item){
    console.log("-> Plugin :"+this.pluginName+" handleJsonFragmentResponse");
    var command = item;
    //Construct command list as String list
    this.commandList.push(command);
}
/**
 * display the plugin  into a specific div
 * @param div, a jquery div item already append somewhere in the dom
 */
Plugin.prototype.loadInto = function(div){
    console.log("-> Plugin :"+this.pluginName+" loadInto");
    //append in H2 the plugin name
    div.append("<h2>"+this.pluginName+"</h2>");
    //add a navBar for all the commands
    var divNavigation = $("<ul id='navBarCommands' class='nav nav-tabs'></ul>");
    div.append(divNavigation);
    //add a div to display the commands content
    this.divDisplay    =  $("<div class='container-fluid' style='clear:both'></div>");
    div.append(this.divDisplay);
    //for each commands, create one and display the first one
    for(var i=0; i<this.commandList.length;i++){
        //get command name from command name list
        var command = this.commandList[i];
        //add the command to the nav bar
        var liRole  = $('<li id="'+command+'" class="navcommand" role="presentation"></li>');
        divNavigation.append(liRole);
        var linkCom = $(
                '<a href="#" tabindex="-1" role="menuitem" class="commandLink">'
                + command
                + '</a></li>');
        liRole.append(linkCom);
        //attach displayCommand to the new link, giving it command name
        linkCom.click(this.displayCommand.bind(this, command));
        //for the first element, lets draw it
        if(i==0)
            linkCom.click();
    }
}
/**
 * switch the navbar to the command
 * and process the command drawing
 * @param command a command object
 * @param e a click event
 */
Plugin.prototype.displayCommand = function(commandName, e){
    console.log("-> Plugin :"+this.pluginName+" displayCommand");
    this.switchTo(commandName);
    this.currentCmd.process();
}
/**
 * when a command need to make a request it ask to his parent plugin to 
 * call the callback for him.
 * This is pretty usefull for command linkage.
 * @param strCommand the command name
 * @param data json response
 */
Plugin.prototype.handleCommandJsonResponse = function(strCommand, data){
    console.log("-> Plugin :"+this.pluginName+" handleCommandJsonResponse");
    //make the switch and let the command handle his response
    this.switchTo(strCommand);
    this.currentCmd.handleJsonResponse(true, data);
}
/**
 * kill all the timers running (1 if it's in a non buggy state)
 * remove the active class to all second navbar entry
 * add an active class to the current command
 * @param command a command objet
 */
Plugin.prototype.switchTo = function(commandName){
    console.log("-> Plugin :"+this.pluginName+" switchTo");
    //if currentCmd.commandName != commandName
    //then destroy precedent command and create the new one
    //if currentCmd is null, just create the new one
    var currCmdName  = this.currentCmd != null ? this.currentCmd.commandName : "";
    if(currCmdName != commandName){
        //if current command != null, destroy it
        this.destroyCurrent();
        this.currentCmd = new Command(commandName, this,this.divDisplay);
    }

    $("li.navcommand").each(function(i, item){$(item).removeClass("active")});
    $("#"+this.currentCmd.commandName).toggleClass("active");
}

/**
 * If exsist, detroy the current command
 */
Plugin.prototype.destroyCurrent = function(){
    if(this.currentCmd != null){
        this.currentCmd.destroy();
    }
}
