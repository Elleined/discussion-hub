'use strict';

var stompClient = null;
var socket = null;
var subscription = null;

var commentURI = null;

$(document).ready(function() {
    var commentSection = $("#commentSection");

    connect();

    $(".card-body #viewCommentsBtn").on("click", function(event) {
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
        addComment(body);

        // MessageMapping URI
        stompClient.send("/app" + commentURI, {}, JSON.stringify({body: body})); // Sets when user View Comments
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
        type:"POST",
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
                commentSection.append("<li>" + commentDto.body + "</li>");
            });
        },
        error: function(xhr, status, error) {
            alert("Getting all comments failed!");
        }
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

