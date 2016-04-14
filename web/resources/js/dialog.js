/* 
 * Created by Jared Schwalbe on 2016.03.09  * 
 * Copyright © 2016 Osman Balci. All rights reserved. * 
 */

$(function () {
    // Disable focus
    $.ui.dialog.prototype._focusTabbable = function () {};

    // Open the dialog when the "choose photo" button is clicked
    $(".add-pin-button").on("click", function () {
        
        if($("#header-links-form").text().indexOf("Sign In") >= 0) {
            showMapMessage("You must sign in to create a pin.", 5000);
        } else {
            $("#create-pin-dialog").dialog("open");
        }
    });

    // Dialog properties
    $("#create-pin-dialog").dialog({
        autoOpen: false,
        width: 300,
        resizable: false,
        draggable: false,
        modal: true
    });
});

function closeDialog() {
    $("#create-pin-dialog").dialog("close");
    
    setTimeout(function() {
        // Reset fields
        $("#create-pin-form\\:title").val("");
        $("#create-pin-form\\:title").css("background-color", "#ffffff");
        $("#create-pin-form\\:description").val("");
        $("#create-pin-form\\:description").css("background-color", "#ffffff");
        $("#create-pin-form\\:browse-btn").attr("value", "");
        $("#create-pin-form\\:anonymous").attr('checked', false);
    }, 500);
}