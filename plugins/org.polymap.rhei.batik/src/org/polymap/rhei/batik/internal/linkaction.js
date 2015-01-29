function sendServiceHandlerRequest( handlerId, id ) {
    var xhr = (window.XMLHttpRequest)
            ? new XMLHttpRequest()  // code for IE7+, Firefox, Chrome, Opera, Safari
            : new ActiveXObject( 'Microsoft.XMLHTTP' ); // code for IE6, IE5
    var url = '?servicehandler=' + handlerId + "&id=" + id;
    //xhr.open( 'GET', url, true );
    //xhr.setRequestHeader( 'Content-type', 'application/x-www-form-urlencoded' ); 
    //xhr.setRequestHeader( 'Connection', 'close' );
    //xhr.send();    
    
    new top.rwt.remote.Request( url, 'POST', 'text/html' ).send();
    
    /*
    qx.ui.core.Widget.flushGlobalQueues();
    if (!org.eclipse.swt.EventUtil.getSuspended()) {
        var req = org.eclipse.swt.Request.getInstance();
        req.addParameter( 'custom_service_handler', handlerId );
        req.addParameter( 'id', id );
        req.send();
    }
    */
}

