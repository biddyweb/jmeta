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
    this.rebindLinkClicks();
}

/*
 * draw a plugin pointed by his index
 */
MetaJs.prototype.drawPlugin = function(i, item){
    var str = $('<li role="presentation" class="plugins" id="'+item.pluginName+'">'
            + '<a href="#" tabindex="-1" role="menuitem" class="pluginLink">'
            + item.pluginName
            + '</a></li>');
    $("#navBar").append(str);
}
MetaJs.prototype.rebindLinkClicks = function(){
    var pluginlinks = $(".pluginLink");
    for(var i=0; i<pluginlinks.length;i++){
        var plugin = this.pluginList[i];
        var link   = $(pluginlinks[i]);
        link.click(this.loadPlugin.bind(this, plugin));
    }
}

MetaJs.prototype.loadPlugin = function(plugin, e){
    e.preventDefault();
    var main = $("#main");
    main.html("");
    var divContainerFluid = $("<div class='container-fluid'></div>");
    main.append(divContainerFluid);
    plugin.loadInto(divContainerFluid);

    $("li.plugins").each(function(i, item){$(item).removeClass("active")});
    $("#"+plugin.pluginName).toggleClass("active");
}

// This function is call when DOM is ready
$(function() {
    meta = new MetaJs();
});
