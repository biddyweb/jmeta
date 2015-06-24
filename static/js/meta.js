/**********************************************************************************
 *
 * Meta Main class
 *
 *********************************************************************************/
var MetaJs = function(){
    this.pluginList = new Array(0);
    this.fetchPlugins();
    $.ajaxSetup({ cache: false });
}

//Attributes
MetaJs.prototype.pluginList   = null;

//Methods

/**
 * fetchPlugins : get plugins list on web service
 *                instantiate a Plugin objet per plugin
 */
MetaJs.prototype.fetchPlugins = function(){
     var pluginsNames = $.getJSON('getPluginsList')
            .done(this.handleJsonResponse.bind(this))
            .fail(function() {
                    $('#commands_menu').append("<li>Webservice Error !</li>");
            });
}

/**
 * Handle json response and draw the navbar
 * @param data a json data
 */
MetaJs.prototype.handleJsonResponse = function(data){
    //for each data, parse a plugin
    $.each( data,  this.handleJsonFragmentResponse.bind(this));
    this.drawPlugins();
}
/**
 * parse a plugin from a json fragment
 * @param i an integer index
 * @param item a string containig the plugin name
 */
MetaJs.prototype.handleJsonFragmentResponse = function(i, item){
    var plugin = new Plugin(item);
    this.pluginList.push(plugin);
}

/**
 * draw all the plugins
 */
MetaJs.prototype.drawPlugins = function(){
    $.each(this.pluginList, this.drawPlugin.bind(this));
    this.rebindLinkClicks();
}

/*
 * draw a plugin pointed by his index
 * @param i integer index
 * @param item a plugin object
 */
MetaJs.prototype.drawPlugin = function(i, item){
    var str = $('<li role="presentation" class="plugins" id="'+item.pluginName+'">'
            + '<a href="#" tabindex="-1" role="menuitem" class="pluginLink">'
            + item.pluginName
            + '</a></li>');
    $("#navBar").append(str);
}
/**
 * bind a click function on main navbar link
 */
MetaJs.prototype.rebindLinkClicks = function(){
    var pluginlinks = $(".pluginLink");
    for(var i=0; i<pluginlinks.length;i++){
        var plugin = this.pluginList[i];
        var link   = $(pluginlinks[i]);
        //call loadPlugin on click
        link.click(this.loadPlugin.bind(this, plugin));
    }
}

/**
 * display the plugin in the main zone
 * @param plugin a plugin objet
 * @param e a click event
 */
MetaJs.prototype.loadPlugin = function(plugin, e){
    e.preventDefault();
    //in case of plugin change, kill all the timers
    for(var i=0; i<this.pluginList.length; i++)
        if(this.pluginList[i] !== undefined)
            this.pluginList[i].killAllCommandsTimer();
    var main = $("#main");
    //flush zone
    main.html("");
    //add a div containerFluid for bootstrap
    var divContainerFluid = $("<div class='container-fluid'></div>");
    main.append(divContainerFluid);
    plugin.loadInto(divContainerFluid);

    //add an active class for location prurposes
    $("li.plugins").each(function(i, item){$(item).removeClass("active")});
    $("#"+plugin.pluginName).toggleClass("active");
}

/**
 * launch the app
 */
$(function() {
    meta = new MetaJs();
});
