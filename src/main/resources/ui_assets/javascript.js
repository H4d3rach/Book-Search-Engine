$( document ).ready(function() {
    console.log( "ready!" );

    var button = $("#submit_button");   
    var searchBox = $("#search_text"); 
    var resultsTable = $("#results table tbody"); 
    var resultsWrapper = $("#results"); 

    button.on("click", function(){

        $.ajax({
          method : "POST",
          contentType: "application/json",
          data: createRequest(),
          url: "procesar_datos",
          dataType: "json",
          success: onHttpResponse
          });
      });

    function createRequest() {
        var searchQueryTmp = searchBox.val();

        var frontEndRequest = {
            searchQuery: searchQueryTmp,
        };
        
        return JSON.stringify(frontEndRequest);
    }

    function onHttpResponse(data, status) {
        if (status === "success" ) {
            console.log(data);
            addResults(data);
        } else {
            alert("Error al conectarse al servidor: " + status);
        }
    }

    function addResults(data) {
        resultsTable.empty();
        resultsWrapper.show();
        for (let i=0; i<data.lista.length; i++){
        let lib = "Libro " + (i+1);
        let dir = "/ui_assets/imagenes/" + encodeURIComponent(data.lista[i]);
        console.log(lib);
        console.log(dir);
        resultsTable.append("<tr><td>"+lib+"</td><td><img src='"+dir+"' alt='portada'</td></tr>");
        }
        console.log(resultsTable);
    }
});

