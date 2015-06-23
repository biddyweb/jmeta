/**********************************************************************************
 *
 * Meta Main class
 *
 *********************************************************************************/
var MetaJs = function(){
    this.pluginList = new Array(0);
    this.fetchPlugins();
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

MetaJs.prototype.handleJsonResponse = function(data){
    $.each( data,  this.handleJsonFragmentResponse.bind(this));
    this.drawPlugins();
}
MetaJs.prototype.handleJsonFragmentResponse = function(i, item){
    var plugin = new Plugin(item);
    this.pluginList.push(plugin);
}

/**
 * draw all the plugins
 */
MetaJs.prototype.drawPlugins = function(){
    $.each(this.pluginList, this.drawPlugin.bind(this));
}

/*
 * draw a plugin pointed by his index
 */
MetaJs.prototype.drawPlugin = function(i, item){
    var str = $('<li role="presentation">'
            + '<a href="#" tabindex="-1" role="menuitem">'
            + item.pluginName
            + '</a></li>');
    $("#navBar").append(str);
}

// This function is call when DOM is ready
$(function() {
    meta = new MetaJs();
});
