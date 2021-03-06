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
    console.log("-> fetchPlugins");
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
    console.log("-> handleJsonResponse");
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
    console.log("-> handleJsonFragmentResponse");
    var plugin = new Plugin(item);
    this.pluginList.push(plugin);
}

/**
 * draw all the plugins
 */
MetaJs.prototype.drawPlugins = function(){
    console.log("-> drawPlugins");
    $.each(this.pluginList, this.drawPlugin.bind(this));
    this.rebindLinkClicks();
}

/*
 * draw a plugin pointed by his index
 * @param i integer index
 * @param item a plugin object
 */
MetaJs.prototype.drawPlugin = function(i, item){
    console.log("-> drawPlugin");
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
    console.log("-> rebindLinkClicks");
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
    console.log("-> loadPlugin");
    e.preventDefault();
    //Destroy current command of the plugin
    for(var i=0; i<this.pluginList.length; i++)
        if(this.pluginList[i] !== undefined){
            this.pluginList[i].destroyCurrent();
        }
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
