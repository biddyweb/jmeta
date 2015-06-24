/**********************************************************************************
 *
 * Command class
 *
 *********************************************************************************/
var Command = function (commandName, plugin, div){
    this.commandName   = commandName;
    this.toCommand     = commandName;
    this.plugin        = plugin     ;
    this.div           = div        ;
    this.data          = null;
}
Command.prototype.commandName  = null;
Command.prototype.plugin       = null;
Command.prototype.div          = null;
Command.prototype.processHtml  = null;
Command.prototype.toCommand    = null;
Command.prototype.timer        = null;

Command.prototype.startRetrievingUpdate = function(){
    this.timer = window.setInterval(this.retrieveUpdate.bind(this), 500);
}
Command.prototype.stopTimer = function(){
    window.clearInterval(this.timer);
}

Command.prototype.fetchInterface = function(){
     var pluginsNames =
         $.getJSON('interface/'+this.plugin.pluginName+'/'+this.commandName)
            .done(this.handleJsonResponse.bind(this, false))
            .fail(function(data) {
                this.div.html("");
                this.div.append(data["responseText"]);
            }.bind(this));
}
Command.prototype.retrieveUpdate = function(){
     var parameter = $("#formCommand").serialize();
     var pluginsNames =
         $.getJSON('retrieveUpdate/'+this.plugin.pluginName+'/'+this.commandName+
                 '?'+parameter)
            .done(this.handleJsonResponse.bind(this, false))
            .fail(function(data) {
                this.div.html("");
                this.div.append(data["responseText"]);
            }.bind(this));
}

Command.prototype.handleJsonResponse = function(launchTimer, data){
    this.processHtml = $("<form id='formCommand'></form>");
    this.processHtml.submit(this.submit.bind(this));
    var tempHtml = $("<input type='hidden' id='idCommand' name='idCommand' value='"+data["idCommand"]+"' />");
    this.processHtml.append(tempHtml);
    truc = data;
    this.decodeJsonToHtml(data, this.processHtml);
    this.draw();
    if(launchTimer)
        this.startRetrievingUpdate();
}

Command.prototype.draw = function(){
    this.div.html("");
    this.div.append(this.processHtml);
}
Command.prototype.process = function(){
    this.fetchInterface();
}
Command.prototype.decodeJsonToHtml = function(data, parentHtml, colsm){
    var divGroup = $("<div class='form-group col-sm col-sm-"+colsm+"'>");
    parentHtml.append(divGroup);
    parentHtml = divGroup;
    var type        = data["type"];
    var currentHtml = null;
    if(data["value"]===undefined)
        data["value"]="";
    //write html
    switch(type){

        case "checkBox" :
                currentHtml = $(
                            "<div class='checkbox'>"+
                            "<label>"+
                              "<input type='checkbox' name='"+
                              data["id"]+ "' value='"+ data["id"]+
                              "' id='"+ data["id"]+"' />"+data["label"]+
                            "</label>");
            break;

        case "checkBoxList" :
                 currentHtml =
                         $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>");
                    divGroup.append(currentHtml);
                var boxes = data["content"];
                var parentData = data;
                for(var i=0; i<boxes.length; i++){
                    data = boxes[i];
                    var checked = data["checked"] ? "checked=checked" : "";
                    currentHtml = $(
                            "<div class='checkbox'>"+
                            "<label>"+
                              "<input type='checkbox' name='"+
                              parentData["id"]+ "' value='"+ data["id"]+
                              "' id='"+ data["id"]+"' "+checked+"/>"+data["label"]+
                            "</label>");
                    divGroup.append(currentHtml);
                    currentHtml = "";
                }
                data = parentData;
            break;

        case "radio" :
                currentHtml = $(
                            "<div class='radio>"+
                            "<label>"+
                              "<input type='radio' name='"+
                              data["id"]+ "' value='"+ data["id"]+
                              "' id='"+ data["id"]+"' />"+data["label"]+
                            "</label>");
            break;

        case "radioList" :
                 currentHtml =
                         $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>");
                    divGroup.append(currentHtml);
                var boxes = data["content"];
                var parentData = data;
                for(var i=0; i<boxes.length; i++){
                    data = boxes[i];
                    var checked = data["selected"] ? "checked=checked" : "";
                    currentHtml = $(
                            "<div class='radio'>"+
                            "<label>"+
                              "<input type='radio' name='"+
                              parentData["id"]+ "' value='"+ data["id"]+
                              "' id='"+ data["id"]+"' "+checked+" />"+data["label"]+
                            "</label>");
                    divGroup.append(currentHtml);
                    currentHtml = "";
                }
                data = parentData;
            break;

        case "selectList" :
                currentHtml =
                         $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>");
                divGroup.append(currentHtml);
                currentHtml =
                         $("<select class='form-control' name='"+ data["id"]
                                 + "' id='"+ data["id"]+"'></select>");
                divGroup.append(currentHtml);
                divGroup = currentHtml;

                var boxes = data["content"];
                var parentData = data;
                for(var i=0; i<boxes.length; i++){
                    data = boxes[i];
                    var checked = data["selected"] ? "selected=selected" : "";
                    currentHtml = $(
                              "<option "+checked+"  value='"+ data["id"]+ "'>"+data["label"]+"</option>");
                    divGroup.append(currentHtml);
                    currentHtml = "";
                }
                data = parentData;
            break;

        case "DateInput" :
                currentHtml = $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>"+
                        "<input class='form-control' type='date' name='"+
                         data["id"]+ "' value='"+
                        data["value"]+ "' id='"+ data["id"]+ "' />");
            break;

        case "TextInput" :
                currentHtml = $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>"+
                        "<input class='form-control' type='text' name='"+
                         data["id"]+ "' value='"+
                        data["value"]+ "' id='"+ data["id"]+ "' />");
            break;

        case "TextOutput" :
                if(data["message"]!=="")
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
        case "Line":
                currentHtml = $("<div class='col-sm-12'></div>");
                var content = data["content"];
                if(content !== undefined){
                    var colSm = content.length < 12 ? 12 / content.length : 1;
                    for(var i=0; i<content.length; i++){
                        this.decodeJsonToHtml(content[i],currentHtml,colSm);
                    }

                }
            break;

        case "selfSubmitButton" :
                 currentHtml = $("<input class='btn btn-default' type='submit' name='"+ data["id"]
                         + "' value='"+
                        data["label"]+ "' id='"+ data["id"]+ "' />");
            break;
    
        case "submitToButton" :
                var destination = data["destination"];
                 currentHtml = $("<input class='btn btn-default' type='submit' name='"+ data["id"]
                         + "' value='"+
                        data["label"]+ "' id='"+ data["id"]+ "' />");
                 currentHtml.click(function(destination, e){
                    this.toCommand = destination;
                 }.bind(this, destination));
            break;
    }
    parentHtml.append(currentHtml);
}

Command.prototype.submit = function (e){
    e.preventDefault();
    //if another destination, terminate instance on server
    if(this.toCommand !== this.commandName){
        this.stopTimer();
        var parameter = $(e.target).serialize();
        $.ajax({
            url: "terminate/"+this.plugin.pluginName+"/"+this.commandName+"?"+parameter
        });
        $("#idCommand").val("");
    }
    var parameter = $(e.target).serialize();
    $.ajax({
        url: "execute/"+this.plugin.pluginName+"/"+this.toCommand+"?"+parameter
    }).done(this.plugin.handleCommandJsonResponse.bind(this.plugin, this.toCommand))
    .fail(function(data) {
            this.div.html("");
            this.div.append(data["responseText"]);
    }.bind(this));

}
