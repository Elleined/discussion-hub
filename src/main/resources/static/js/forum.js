'use strict';

var stompClient = null;
$(document).ready(function() {
    var commentSection = $("#commentSection");

    connect();

    $(".card-body #subscribeBtn").on("click", function(event) {
        // SendTo URI
        var href = $(this).attr("href");

        console.log(href);
        stompClient.subscribe(href, function(commentDTO) {
            alert(commentDTO);
        });

        // MessagaMapping URI
        stompClient.send('/app' + href,
           {},
           JSON.stringify({body: "This is my comment"})
        );

        event.preventDefault();
    });

    $("#createPostBtn").on("submit", function() {
        createPost();
    });
});

function connect() {
    var socket = new SockJS("/websocket");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    console.log("Connected");
}

function onError() {
    console.log("Could not connect to WebSocket server. Please refresh this page to try again!");
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
            alert(xhr.responseText);
            window.location.href = "/forum";
        },
        error: function(xhr, status, error) {
            alert("Creating post failed!")
        }
    });
}

