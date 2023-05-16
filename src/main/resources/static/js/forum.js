'use strict';

var stompClient = null;
var socket = null;

var commentAPI_URI = null;
var commentWS_URI = null;
$(document).ready(function() {
    var commentSection = $("#commentSection");

    connect();

    $(".card-body #viewCommentsBtn").on("click", function(event) {
        // SendTo URI
        var href = $(this).attr("href");
        console.log("MESSAGE MAPPING URI" + href);
        commentWS_URI = href; // Set the href of the comment to be use in web socket
        stompClient.subscribe("/discussion" + href, function(commentDTO) {
             var dto = JSON.parse(commentDTO.body);
             var messageBody = dto.body;
             commentSection.append("<li>" + messageBody + "</li>");
        });

        getAllCommentsOf(href); // Get all comments of selected post
        event.preventDefault();
    });

    $("#createPostBtn").on("submit", function() {
        event.preventDefault();
         var body = $("#postBody").val();

        createPost(body);
    });

    $(".commentModal #commentForm").on("submit", function(event) {
        event.preventDefault();
        var body = $("#commentBody").val();

        createComment(body);

        console.log("WS URI " + commentWS_URI);
        // MessageMapping URI
        stompClient.send("/app" + commentWS_URI, {}, JSON.stringify({body: body})); // Sets when user View Comments
        $("#commentBody").val("");
    });

    // THIS IS USED TO PREVENT MEMORY LEAK WHEN PAGE IS RELOAD
    $(window).on('beforeunload', function() {

        if (stompClient) {
            stompClient.disconnect();
        }
        if (socket) {
            socket.close();
        }
    });
    // insert here
});

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

function getAllCommentsOf(href) {
    commentAPI_URI = '/forum/api' + href; // Set the href of the comment to comment in selected post
    $.ajax({
        type: "GET",
        url: commentAPI_URI,
        success: function(commentDTOs, response) {
            var commentSection = $(".modal-body #commentSection");
            commentSection.empty(); // Removes the recent comments in the modal
            $.each(commentDTOs, function(index, value) {
                commentSection.append("<li>" + value.body + "</li>");
            });
        },
        error: function(xhr, status, error) {
            alert("Getting all comments failed!");
        }
    });
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

function createComment(body) {
    $.ajax({
        type: "POST",
        url: commentAPI_URI, // Sets when method getAllCommentsOf() is called when clicking the View Comments
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

