$(document).ready(function() {
    $("#email").focus();
    // insert here
    renderView();
});

function renderView() {
    const myDiv = $("#myDiv");
    $.ajax({
        type: "GET",
        url: "/dynamicPage",
        success: function(response) {
            myDiv.append(response);
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}



