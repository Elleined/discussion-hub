'use strict';

var stompClient = null;
var commentHref = null;
$(document).ready(function() {
    var commentSection = $("#commentSection");

    connect();

    $(".card-body #subscribeBtn").on("click", function(event) {
        // SendTo URI
        var href = $(this).attr("href");
        stompClient.subscribe(href); // Try to have callback function here

        // MessagaMapping URI
        stompClient.send('/app' + href,
           {},
           JSON.stringify({body: "This is my comment"})
        );
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

function getAllCommentsOf(href) {
    commentHref = '/forum/api' + href; // Set the href of the comment to comment in selected post
    $.ajax({
        type: "GET",
        url: commentHref,
        success: function(commentDTOs, response) {
            console.log("Comments of selected post fetch successfully");
            console.table(commentDTOs);
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
        url: commentHref,
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

