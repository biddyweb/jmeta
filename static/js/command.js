/**********************************************************************************
 *
 * Command class
 * @param commandName the command name
 * @param plugin the plugin name
 * @param div a div already added somewhere into the dom to write the calculated
 *        output
 *********************************************************************************/
var Command = function (commandName, plugin, div){
    this.commandName   = commandName;
    this.toCommand     = commandName;
    this.plugin        = plugin     ;
    this.div           = div        ;
    this.data          = null;
    this.previousData  = null;
}
Command.prototype.commandName           = null;
Command.prototype.plugin                = null;
Command.prototype.div                   = null;
Command.prototype.processHtml           = null;
Command.prototype.toCommand             = null;
Command.prototype.timer                 = null;
Command.prototype.previousData          = null;
Command.prototype.executeTimerCallback  = null;

/**
 * strat a timer to retrieve regular updates on the server (every
 * 500 harcoded ms)
 */
Command.prototype.startRetrievingUpdate = function(){
    this.executeTimerCallback = true;
    this.timer = window.setInterval(this.retrieveUpdate.bind(this),  500);
}
/**
 * stop the current timer
 */
Command.prototype.stopTimer = function(){
    this.executeTimerCallback = false;
    window.clearInterval(this.timer);
}

/**
 * Destroy the current command by stop timer and put all the attributes
 * to void, and all prototype to new function
 */
Command.prototype.destroy = function(){
    this.stopTimer();
    this.commandName           = null;
    this.plugin                = null;
    this.div                   = null;
    this.processHtml           = null;
    this.toCommand             = null;
    this.timer                 = null;
    this.previousData          = null;
    this.executeTimerCallback  = null;
    this.fetchInterface        = function(){}
    this.retrieveUpdate        = function(){}
    this.handleJsonResponse    = function(){}
    this.firstDraw             = function(){}
    this.process               = function(){}
    this.decodeJsonToHtml      = function(){}
    this.submit                = function(){}
    this.updateHtml            = function(){}
    this.getOtherChild         = function(){}
}

/**
 * fetch the standard interface as JSON and execute a handleJsonResponse to
 * do something with it
 */
Command.prototype.fetchInterface = function(){
    console.log("-> Command : "+this.commandName+" fetchInterface");
    this.previousData = null;
    var pluginsNames =
        $.getJSON('interface/'+this.plugin.pluginName+'/'+this.commandName)
           .done(this.handleJsonResponse.bind(this, false))
           .fail(function(data) {
               this.div.html("");
               this.div.append(data["responseText"]);
           }.bind(this));
    console.log("<- Command : "+this.commandName+" fetchInterface");
}
/**
 * launch a retrieveupdate (called by the timer) to get the last state of the
 * severside command
 */
Command.prototype.retrieveUpdate = function(){
    console.log("-> Command :"+this.commandName+" retrieveUpdate");
    if(this.executeTimerCallback){
        //get the main form parameters
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
}

/**
 * parse a json response and draw the interface
 * @param launchtimer true if the timer has to be started false otherwise
 * @param data        data to parse to draw the interface
 */
Command.prototype.handleJsonResponse = function(launchTimer, data){
    console.log("-> Command :"+this.commandName+" -> handleJsonResponse");
    //add a form to the main div
    this.processHtml = $("<form id='formCommand'></form>");
    //bind is submit to a custom proper submit function
    this.processHtml.submit(this.submit.bind(this));
    //add idCommand hidden input wich allow us to make several call to the same command
    var tempHtml = $("<input type='hidden' id='idCommand' name='idCommand' value='"+data["idCommand"]+"' />");
    this.processHtml.append(tempHtml);
    //decode json
    if(this.previousData === null){
        vache = data;
        this.decodeJsonToHtml(data, this.processHtml);
        //draw that all for the first run
        this.firstDraw();
        this.previousData = data;
    }
    else{
        this.updateHtml(data, this.previousData);
        this.previousData = data;
    }
    //if needed launch the timer
    if(launchTimer){
        this.toCommand = this.commandName;
        this.startRetrievingUpdate();
    }
}

/**
 * flush the div html output and drawn processHtml in it
 */
Command.prototype.firstDraw = function(){
    console.log("-> Command :"+this.commandName+" firstDraw");
    this.div.html("");
    this.div.append(this.processHtml);
}
/**
 * process -> generally called by the plugin, 
 * fetch the standard command interface and make a life cycle
 */
Command.prototype.process = function(){
    console.log("-> Command :"+this.commandName+" process");
    this.previousData == null;
    this.fetchInterface();
}
/**
 * decode json to html -> recursive
 * @param data          a well formated data json object
 * @param parentHtml    the parent div to draw all that stuff
 * @param colsm         width (in a bootstrap way) of that stuff
 */
Command.prototype.decodeJsonToHtml = function(data, parentHtml, colsm){
    console.log("-> Command :"+this.commandName+" decodeJsonToHtml");
    //for style purposes add a div form group
    var divGroup = $("<div class='form-group col-sm col-sm-"+colsm+"'>");
    parentHtml.append(divGroup);
    parentHtml = divGroup;
    //extract the data type (which is usefull for the sitchw case bellow)
    var type        = data["type"];
    var currentHtml = null;
    //in case of the real common valu undefined, make it value an empty string
    if(data["value"]===undefined)
        data["value"]="";
    //write html
    switch(type){
        //if a single checkboc
        case "checkBox" :
                currentHtml = $(
                            "<div class='checkbox'>"+
                            "<label>"+
                              "<input type='checkbox' name='"+
                              data["id"]+ "' value='"+ data["id"]+
                              "' id='"+ data["id"]+"' />"+data["label"]+
                            "</label>");
            break;
        //if a checkbox list, all the contained checkboxes will share the same name
        //wich is the checkboxlist oject id
        case "checkBoxList" :
                 currentHtml =
                         $("<label for='"+ data["id"]+ "' id='"+data["id"]+"'>"+ data["label"]+ "</label>");
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
        //draw a single radio button, if that is usefull to someone...
        case "radio" :
                currentHtml = $(
                            "<div class='radio>"+
                            "<label>"+
                              "<input type='radio' name='"+
                              data["id"]+ "' value='"+ data["id"]+
                              "' id='"+ data["id"]+"' />"+data["label"]+
                            "</label>");
            break;
        //if a radio list, all the contained radio buttons  will share the same name
        //wich is the radio list oject id
        case "radioList" :
                 currentHtml =
                         $("<label for='"+ data["id"]+ "' id='"+data["id"]+"'>"+ data["label"]+ "</label>");
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
        //draw a select 
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
        //draw a date input, quite the same as a textinput in fact
        case "DateInput" :
                currentHtml = $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>"+
                        "<input class='form-control' type='date' name='"+
                         data["id"]+ "' value='"+
                        data["value"]+ "' id='"+ data["id"]+ "' />");
            break;
        //draw a text input
        case "TextInput" :
                currentHtml = $("<label for='"+ data["id"]+ "'>"+ data["label"]+ "</label>"+
                        "<input class='form-control' type='text' name='"+
                         data["id"]+ "' value='"+
                        data["value"]+ "' id='"+ data["id"]+ "' />");
            break;
        //draw a simple text output replacing \n to <br />
        case "TextOutput" :
                if(data["message"]!=="")
                    currentHtml = $("<h3>"+data["label"]+"</h3><p id='"+data["id"]+"'>"+data["message"].replace(/\n/g, "<br />")+"</p></p>");
            break;
        //draw a column object wich is a div who's contain
        //his children one under another
        case "Column" :
                currentHtml = $("<div class='metaCol col-sm-12' id='"+data["id"]+"'></div>");
                var content = data["content"];
                if(content !== undefined){
                    for(var i=0; i<content.length; i++){
                        this.decodeJsonToHtml(content[i],currentHtml,12);
                    }
                }
            break;
        //draw a line object wich is a div who's contain his children side by side
        case "Line":
                currentHtml = $("<div class='metaLine col-sm-12' '"+data["id"]+"'></div>");
                var content = data["content"];
                if(content !== undefined){
                    var colSm = content.length < 12 ? 12 / content.length : 1;
                    for(var i=0; i<content.length; i++){
                        this.decodeJsonToHtml(content[i],currentHtml,colSm);
                    }

                }
            break;
        //draw a self submit button who's allow the user to submit the form to 
        //himslef
        case "selfSubmitButton" :
                 currentHtml = $("<input class='btn btn-default' type='submit' name='"+ data["id"]
                         + "' value='"+
                        data["label"]+ "' id='"+ data["id"]+ "' />");
            break;
        //draw a button to submit to another command wich force the user to get 
        //the result from another command 
        case "submitToButton" :
                var destination = data["destination"];
                 currentHtml = $("<input class='btn btn-default' type='submit' name='"+ data["id"]
                         + "' value='"+
                        data["label"]+ "' id='"+ data["id"]+ "' />");
                 //bind to the button a special click function to force a
                 //workflow
                 //toCommand becomme the destination attribute of the destination
                 currentHtml.click(function(destination, e){
                    this.toCommand = destination;
                 }.bind(this, destination));
            break;
    }
    //finally add the content to the parent
    parentHtml.append(currentHtml);
    if(data["description"] !== undefined && data["description"] !== "" ){
        parentHtml.append($('<p class="help-block '+data["id"]+'">'
                    +data["description"]+'</p>'));
    }
}

/**
 * custom submit for the main form
 * prevent default usage
 *
 * if redirected to another command, to have a clean state:
 *      kill the timer
 *      terminate the object on server
 *      remove the id command form hidden input
 *
 * execute the form on the right command
 */
Command.prototype.submit = function (e){
    console.log("-> Command :"+this.commandName+" submit");
    this.previousData = null;
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
    //toCommand may point on the same command or another
    //TODO what if an error ?
    $.ajax({
        url: "execute/"+this.plugin.pluginName+"/"+this.toCommand+"?"+parameter
    }).done(this.plugin.handleCommandJsonResponse.bind(this.plugin, this.toCommand))
    .fail(function(data) {
            this.div.html("");
            this.div.append(data["responseText"]);
    }.bind(this));

}

/**
 * Calculate differnces between oldDatas and new Datas
 * and uptade only needed dom element
 */
Command.prototype.updateHtml = function(data, oldData, elementParent, colsm){
    console.log("-> Command :"+this.commandName+" updateHtml");
    //try to get html element with an equal id than new data
    var elementToUpdate = $("#"+data["id"]);
    //if this element does not exist, create
    if(document.getElementById(data["id"])==null){
        this.decodeJsonToHtml(data, elementParent, 12);//TODO col-sm
    }
    //otherwise update him following rules
    else{
        //There is no need to update directly attributes from them
        if(data["type"] != "Column" && data["type"] != "Line"){

            $(".help-block."+data["id"]).html(data["description"]);
 
            var type = data["type"];
            switch(type){
                case "radio" :
                case "checkBox" :
                    var elementchecked = elementToUpadte.is(":checked");
                    var wasChecked     = oldData["checked"];
                    var wantChecked    = data["checked"];

                    //if previous data was checked and element has no changes
                    //it's ok to take the new value, otherwhise, juste do no
                    //touch it
                    if(elementchecked && wasChecked)
                        elementToUpdate.prop("checked", wantChecked)
                    break;

                //just update the label
                case "radioList" :
                case "checkBoxList" :
                         $("label[for='"+data["id"]+"']").text(data["label"]);
                    break;

                case "selectList" :
                         $("label[for='"+data["id"]+"']").text(data["label"])

                        var boxes = data["content"];
                        for(var i=0; i<boxes.length; i++){
                            var elementchecked = elementToUpdate.is(":selected");
                            var wasChecked     = oldData["selected"];
                            var wantChecked    = data["selected"];

                            //if previous data was checked and element has no changes
                            //it's ok to take the new value, otherwhise, juste do no
                            //touch it
                            if(elementchecked && wasChecked)
                                elementToUpdate.prop("checked", wantChecked)
                        }
                        for(var option in data["content"]){
                            if($("#"+option["id"]) == undefined){
                                var checked = option["selected"] ? "selected=selected" : "";
                                currentHtml = $(
                                          "<option "+checked+"  value='"+ option["id"]+ "'>"+option["label"]+"</option>");
                                elementToUpdate.append(currentHtml);
                            }
                        }
                    break;
                case "selfSubmitButton" :
                case "TextInput" :
                case "DateInput" :
                        var valueElement   = elementToUpdate.val();
                        var wasValue       = oldData["value"];
                        var wantValue      = data["value"];

                        //if previous value is the same than element valuye
                        //its ok to change it, otherwise, do not touch it
                        if(valueElement == wasValue)
                            elementToUpdate.val(wantValue);
                    break;
                //draw a simple text output replacing \n to <br />
                case "TextOutput" :
                        if(data["message"]!=="")
                            elementToUpdate.html(data["message"].replace(/\n/g, "<br />"));
                    break;
                //draw a line object wich is a div who's contain his children side by side
                case "Line":
                    /* TODO col-sm
                        currentHtml = $("<div class='metaLine col-sm-12'></div>");
                        var content = data["content"];
                        if(content !== undefined){
                            var colSm = content.length < 12 ? 12 / content.length : 1;
                            for(var i=0; i<content.length; i++){
                                this.decodeJsonToHtml(content[i],currentHtml,colSm);
                            }

                        }*/
                    break;
                case "submitToButton" :
                         elementToUpdate.click(function(destination, e){
                            this.toCommand = destination;
                         }.bind(this, data["destination"]));
                    break;
            }

        }
        //but we need to update there children
        else{
            var childrenSet = data["content"];
            //for each child make a recursive call
            if(childrenSet != undefined)
                for(var i=0; i<childrenSet.length; i++){
                    var child   = childrenSet[i];
                    var childId = child["id"];
                    var childOld = this.getOtherChild(childId, oldData["content"]);
                    //doesn't matter if childOld doesn't exist here
                    this.updateHtml(child, childOld, elementToUpdate)//TODO col-sm
                }
        }
        //now we need to loop on the oldData to remove useless child
        if(oldData !== null && oldData !== undefined && oldData["content"] !== undefined){
            for(var i=0; i<oldData["content"].length; i++){
                var child   = oldData["content"][i];
                if(this.getOtherChild(child["id"], data["content"]) == null){
                    var elementToDelete = $("#"+child["id"]);
                    elementToUpdate.remove();
                }
            }
        }
    }
}
Command.prototype.getOtherChild = function(id, childrenSet){
    console.log("-> Command :"+this.commandName+" getOtherChild");
    var found = null;
    for(var i=0; i<childrenSet.length; i++){
        var child   = childrenSet[i];
        if(child["id"] == id)
            found = child;
     }
    return found;
}
