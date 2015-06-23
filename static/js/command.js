/**********************************************************************************
 *
 * Command class
 *
 *********************************************************************************/
var Command = function (commandName, plugin, div){
    this.commandName   = commandName;
    this.plugin        = plugin     ;
    this.div           = div        ;
    this.data          = null;
}
Command.prototype.commandName  = null;
Command.prototype.plugin       = null;
Command.prototype.div          = null;
Command.prototype.processHtml  = null;

Command.prototype.fetchInterface = function(){
     var pluginsNames =
         $.getJSON('interface/'+this.plugin.pluginName+'/'+this.commandName)
            .done(this.handleJsonResponse.bind(this))
            .fail(function() {
                    $('#commands_menu').append("<li>Webservice Error !</li>");
            });
}

Command.prototype.handleJsonResponse = function(data){
    this.processHtml = $("<form></form>");
    var tempHtml = $("<input type='hidden' name='idCommand' value='"+data["idCommand"]+"' />");
    this.processHtml.append(tempHtml);
    truc = data;
    this.decodeJsonToHtml(data, this.processHtml);
    this.draw();
}

Command.prototype.draw = function(){
    this.div.html("");
    this.div.append(this.processHtml);
}
Command.prototype.process = function(){
    this.fetchInterface();
}
Command.prototype.decodeJsonToHtml = function(data, parentHtml, colsm){
    var divGroup = $("<div class='form-group'>");
    parentHtml.append(divGroup);
    parentHtml = divGroup;
    var type        = data["type"];
    var currentHtml = null;
    //write html
    switch(type){

        case "checkBox" :
                currentHtml = $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>"+
                        "<input type='checkbox' name='"+ data["id"]+ "' value='"+
                        data["value"]+ "' id='"+ data["id"]+ "' />");
            break;

        case "radioButton" :
                currentHtml = $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>"+
                        "<input type='radio' name='"+ data["id"]+ "' value='"+
                        data["value"]+ "' id='"+ data["id"]+ "' />");
            break;

            //TODO SELECT
            //TODO CheckBoxList
            //TODO radiobutton list

        case "DateInput" :
                currentHtml = $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>"+
                        "<input type='date' name='"+ data["id"]+ "' value='"+
                        data["value"]+ "' id='"+ data["id"]+ "' />");
            break;

        case "TextInput" :
                currentHtml = $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>"+
                        "<input type='text' name='"+ data["id"]+ "' value='"+
                        data["value"]+ "' id='"+ data["id"]+ "' />");
            break;

        case "TextOutput" :
                currentHtml = $("<h3>"+data["label"]+"</h3><p>"+data["message"]+"</p>");
            break;

        case "Column" :
                currentHtml = $("<div class='col-sm-12'></div>");
                var content = data["content"];
                if(content !== undefined){
                    for(var i=0; i<content.length; i++){
                        this.decodeJsonToHtml(content[i],currentHtml,12);
                    }
                }
            break;

        case "Line" :
                currentHtml = $("<div class='col-sm-12'></div>");
                var content = data["content"];
                if(content !== undefined){
                    var colSm = content.lenth < 12 ? content.length / 12 : 1;
                    for(var i=0; i<content.length; i++){
                        this.decodeJsonToHtml(content[i],currentHtml,colSm);
                    }

                }
            break;
        case "selfSubmitButton" :
                 currentHtml = $("<input type='submit' name='"+ data["id"]
                         + "' value='"+
                        data["label"]+ "' id='"+ data["id"]+ "' />");
            break;
    }
    parentHtml.append(currentHtml);
    //if an organizer recursive call
    
    //hidden for keeping the same command id

}
