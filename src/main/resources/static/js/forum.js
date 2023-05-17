'use strict';

var stompClient = null;
var socket = null;
var subscription = null;

var commentURI = null;

$(document).ready(function() {
    var commentSection = $("#commentSection");

    connect();

    $(".card-body #commentBtn").on("click", function(event) {
        commentURI = $(this).attr("href"); // The API URI to be used when saving a comment and getting all comments

        // SendTo URI
        subscription = stompClient.subscribe("/discussion" + commentURI, function(commentResponse) {
            var json = JSON.parse(commentResponse.body);
            var body = json.body;
            commentSection.append("<li>" + body + "</li>");
        });

        getAllCommentsOf(); // Get all comments of selected post
        event.preventDefault();
    });

    $("#commentModal").on("hidden.bs.modal", function() {
        subscription.unsubscribe();
    });

    $("#createPostBtn").on("submit", function() {
        event.preventDefault();
        var body = $("#postBody").val();

        createPost(body);
    });

    $(".commentModal #commentForm").on("submit", function(event) {
        event.preventDefault();

        var body = $(".commentModal #commentBody").val();
        if (body.isBlank() || body === null) return;
        addComment(body);

        // MessageMapping URI
        stompClient.send("/app" + commentURI, {}, JSON.stringify({
            body: body
        })); // Sets when user View Comments
        $("#commentBody").val("");
    });

    // Below this making sure that socket and stompClient is closed
    $("#logoutBtn").on("click", function() {
        disconnect()
    });

    $(window).on('beforeunload', function() {
        disconnect()
    });

    $(document).on('close', function() {
        disconnect()
    });

    $(window).on('unload', function() {
        disconnect()
    });
    // insert here
});

function disconnect() {
    if (stompClient) {
        stompClient.disconnect();
    }
    if (socket) {
        socket.close();
    }
}

function connect() {
    socket = new SockJS("/websocket");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    console.log("Web Socket Connected!!!");
}

function onError() {
    console.log("Could not connect to WebSocket server. Please refresh this page to try again!");
}

function createPost(body) {
    $.ajax({
        type: "POST",
        url: "/forum/api/posts",
        data: {
            body: body
        },
        success: function(response, status, xhr) {
            console.log(xhr.responseText);
            window.location.href = "/forum";
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}

function getAllCommentsOf() {
    $.ajax({
        type: "GET",
        url: "/forum/api" + commentURI,
        success: function(commentDTOs, response) {
            var commentSection = $(".modal-body #commentSection");
            commentSection.empty(); // Removes the recent comments in the modal
            $.each(commentDTOs, function(index, commentDto) {
                generateCommentBlock(commentDto);


            });
        },
        error: function(xhr, status, error) {
            alert("Getting all comments failed!");
        }
    });
}

function generateCommentBlock(commentDto) {
    // The actual html structure of this the comment-body in /templates/fragments/comment-body
    var commentSection = $(".modal-body #commentSection");
    var container = $("<div>")
        .attr("class", "container")
        .appendTo(commentSection);

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
        .text(commentDto.commenterName)
        .appendTo(row1Col1);

    var row2 = $("<div>")
        .attr("class", "row")
        .appendTo(container);

    var row2Col1 = $("<div>")
        .attr("class", "md-col")
        .appendTo(row2);

    var commenterMessageBody = $("<p>")
        .attr("class", "mt-2")
        .text(commentDto.body)
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
        "href": "/forum/api/posts/comments/" + commentDto.id + "/replies",
        "class": "btn btn-primary me-1"
    }).text("Reply").appendTo(row3Col1);

    var timeCommented = $("<span>")
        .text(" at " + commentDto.formattedTime + " on " + commentDto.formattedDate)
        .appendTo(row3Col1);

    var hr = $("<hr>").appendTo(commentSection);

    replyBtn.on("click", function() {
        var replyURI = $(this).attr("href");
        alert(replyURI);
    });
}

function addComment(body) {
    $.ajax({
        type: "POST",
        url: "/forum/api" + commentURI,
        data: {
            body: body
        },
        success: function(response, status, xhr) {
            console.log(xhr.responseText);
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}