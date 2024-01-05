/**
 * Super-Easy DimensioneX Ajax Library
 *
 * v. 1.4
 *
 * http://www.dimensionex.net/
 *
 */

var loadingimg = "https://www.dimensionex.net/loading/loading1.gif"; //Prova anche loading2.gif loading3.gif ...

/**
 * Funzione che istanzia un oggetto XMLHttpRequest usando un meccanismo cross browser.
 *
 * @return   restituisce un'istanza di XMLHttpRequest oppure il valore false in caso
 *           di errori.
 */

function getXMLHttpRequestInstance() {

    var xmlhttp;

    // Prova il metodo Microsoft usando la versione più recente:
    try {
        xmlhttp = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {

        try {
            xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
        } catch (E) {
            xmlhttp = false;
        }
    }

    // Se non è stato possibile istanziare l'oggetto forse siamo
    // su Mozilla/FireFox o su un altro browser compatibile:
    if (!xmlhttp && typeof XMLHttpRequest != 'undefined') {
        try {
            xmlhttp = new XMLHttpRequest();
        } catch (e) {
            xmlhttp = false;
        }
    }

    // Restituisce infine l'oggetto:
    return xmlhttp;
}

/**
 * Funzione che sostituisce il contenuto HTML di un nodo della pagina.
 *
 * @param    nodeId ID del nodo
 * @param    html   codice HTML da sostituire a quello del nodo
 */
function updateContent(nodeId, html) {
   
    var node = document.getElementById(nodeId);
    if(null == node) {
		//alert("[ERROR] page does not have any DIV with ID " + nodeId);
		return;
    }
    node.innerHTML = html;
    node.style.visibility = "visible";
}

/**
 * Richiede al web server un contenuto in maniera asincrona.
 * @param    nodeId    ID dell'elemento della pagina che conterrà il contenuto
 * @param    url       URL del contenuto (deve essere sullo stesso server per motivi di sicurezza)
 * @param    url       Opzionale, URL dell'immagine/animazione "loading"
 */
function ajax(nodeId, url, loadimg) {
    loadimg = (typeof loadimg == "undefined")?loadingimg:loadimg;

    var xmlhttp = getXMLHttpRequestInstance();
    if(!xmlhttp) {
        alert("Il browser non supporta l'oggetto XMLHttpRequest");
        return false;
    }
    if (loadimg!=null){
	    updateContent(nodeId,"");
	    updateContent(nodeId,"<img alt='...' src='"+loadimg+"' />");
    }
    
    if (url.indexOf('?')>=0) {
    	url = url+'&tid='+Math.random();
    } else {
    	url = url+'?tid='+Math.random();
    }
    
    //alert(url);

    xmlhttp.open("GET", url,true);
    xmlhttp.onreadystatechange=function() {
        if (xmlhttp.readyState==4) {
            if (xmlhttp.status==200) {
                updateContent(nodeId, xmlhttp.responseText);
                //alert (xmlhttp.responseText);
            } else if (xmlhttp.status==404) {
                alert("[ERROR] un-existing URL: "+url);
            } else {
                alert("[ERROR] un-handled error (" + xmlhttp.status + ")");
            }
        }
    }

    xmlhttp.send(null);
}


