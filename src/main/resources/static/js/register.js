$(document).ready(function() {
     $("#email").focus();


     $("#appendBtn").on("click", function() {
        generateCommentBlock();
        // Add onclick in replyBtn here
     });
     // insert here
});

function generateCommentBlock() {
        // The actual html structure of this the comment-body in /templates/fragments/comment-body
        var parentElement = $("#parent");

        var container = $("<div>")
            .attr("class", "container")
            .appendTo(parentElement);

        var row1 = $("<div>")
            .attr("class", "row mb-2")
            .appendTo(container);

        var row1Col1 = $("<div>")
            .attr("class", "md-col")
            .appendTo(row1);

        var commenterImage = $("<img>").attr({
                "class": "rounded-circle shadow-4-strong",
                "height": "50px",
                "width": "50px",
                "src": "https://mdbcdn.b-cdn.net/img/new/avatars/1.webp"
        }).appendTo(row1Col1);

        var commenterName = $("<span>")
            .attr("class", "mb-5")
            .text("Commenter Name")
            .appendTo(row1Col1);

        var row2 = $("<div>")
            .attr("class", "row")
            .appendTo(container);

        var row2Col1 = $("<div>")
            .attr("class", "md-col")
            .appendTo(row2);

        var commenterMessageBody = $("<p>")
            .attr("class", "mt-2")
            .text("This is my comment")
            .appendTo(row2);

        var row3 = $("<div>")
            .attr("class", "row")
            .appendTo(container);

        var row3Col1 = $("<div>")
            .attr("class", "md-col")
            .appendTo(row3);

        var replyBtn = $("<button>").attr({
            "type": "button",
            "id": "replyBtn",
            "class": "btn btn-primary me-1",
        }).text("Reply").appendTo(row3Col1);

        var timeCommented = $("<span>")
            .text("Time Here")
            .appendTo(row3Col1);

        var hr = $("<hr>").appendTo(parentElement);
}

