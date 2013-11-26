var resizeTimeout;

// init event handlers
if (window.addEventListener) {
    window.addEventListener( "resize", function( ev ) {
        if (resizeTimeout) {
            clearTimeout( resizeTimeout );
        }
        setTimeout( browserWindowResized, 750 );
    });
}
   
function browserWindowResized( ev ) {
    var xhr = (window.XMLHttpRequest)
            ? new XMLHttpRequest()  // code for IE7+, Firefox, Chrome, Opera, Safari
            : new ActiveXObject( 'Microsoft.XMLHTTP' ); // code for IE6, IE5
    var url = '?custom_service_handler=org.polymap.rhei.batik.BrowserSizeServiceHandler';
    xhr.open( 'POST', url, true );
    xhr.setRequestHeader( 'Content-type', 'application/x-www-form-urlencoded' ); 
    xhr.setRequestHeader( 'Connection', 'close' );
    var params = 'width=' + window.outerWidth + '&height=' + window.outerHeight;
    xhr.send( params );    

    // force UI update?
//    qx.ui.core.Widget.flushGlobalQueues();
//    var req = org.eclipse.swt.Request.getInstance();
//    req.enableUICallBack();
}

/*
function showWaitHint() {
    var doc = qx.ui.core.ClientDocument.getInstance();
    doc.setGlobalCursor( qx.constant.Style.CURSOR_PROGRESS );
}

function hideWaitHint() {
    var doc = qx.ui.core.ClientDocument.getInstance();
    doc.setGlobalCursor( null );
}
    
function noopHandler( ev ) {
    ev.stopPropagation();
    ev.preventDefault();
}
*/