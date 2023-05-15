'use strict';

var stompClient = null;
var commentAPI_URI = null;
var commentWS_URI = null;
$(document).ready(function() {
    var commentSection = $("#commentSection");

    connect();

    $(".card-body #subscribeBtn").on("click", function(event) {
        // SendTo URI
        var href = $(this).attr("href");
        commentWS_URI = href; // Set the href of the comment to be use in web socket
        stompClient.subscribe("/forum" + commentWS_URI, onMessageReceive);

        getAllCommentsOf(href); // Get all comments of selected post
        event.preventDefault();
    });

    $("#createPostBtn").on("submit", function() {
        event.preventDefault();
        createPost();
    });

    $(".commentModal #commentForm").on("submit", function(event) {
        event.preventDefault();
        createComment();
    });
});

function connect() {
    var socket = new SockJS("/websocket");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    console.log("Web Socket Connected!!!");
}

function onError() {
    console.log("Could not connect to WebSocket server. Please refresh this page to try again!");
}

function onMessageReceive(payload) {
    var message = JSON.parse(payload.body);
    commentSection.append("<li>" + message.body + "</li>");
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

function createPost() {
    var body = $("#postBody").val();

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

function createComment() {
    var body = $("#commentBody").val();
    $.ajax({
        type: "POST",
        url: commentAPI_URI, // Sets when method getAllCommentsOf() is called when clicking the View Comments
        data: {
            body: body
        },
        success: function(response, status, xhr) {
            console.log(xhr.responseText);
            // MessageMapping URI
            stompClient.send("/app" + commentWS_URI, {}, JSON.stringify({body: body})); // Sets when user View Comments

            $("#commentBody").val("");
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
       }
    });
}

