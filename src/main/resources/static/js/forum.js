'use strict';

var stompClient = null;
var socket = null;

var replySubscription = null;
var commentSubscription = null;

var replyURI = null;
var commentURI = null;

$(document).ready(function() {
    var commentSection = $("#commentSection");

    connect();

    $(".card-body #commentBtn").on("click", function(event) {
        commentURI = $(this).attr("href");
        console.log(commentURI);

        var postId = commentURI.split("/")[2];
        setCommentModalTitle(postId);

        // SendTo URI of Comment
        commentSubscription = stompClient.subscribe("/discussion" + commentURI, function(commentDto) {
            var json = JSON.parse(commentDto.body);
            generateCommentBlock(json);
        });

        getAllCommentsOf(commentURI);
        event.preventDefault();
    });

    $("#createPostBtn").on("submit", function() {
        event.preventDefault();

        var body = $("#postBody").val();
        if ($.trim(body) === '') return;
        savePost(body);

        $("#postBody").val("");
    });

    $(".commentModal #commentForm").on("submit", function(event) {
        event.preventDefault();

        var body = $("#commentBody").val();
        if ($.trim(body) === '') return;
        saveComment(body);

        $("#commentBody").val("");
    });

    $(".replyModal #replyForm").on("submit", function(event) {
        event.preventDefault();

        var body = $("#replyBody").val();
        if ($.trim(body) === '') return;
        saveReply(body);

        $("#replyBody").val("");
    });

    // Used to show the comments modal when the reply modal is closed
    $("#replyModal").on("hidden.bs.modal", function(e) {
        e.stopPropagation(); // Stop event propagation

        // Open the first modal again
        $('#commentModal').modal('show');
    });


    // Below this making sure that socket and stompClient is closed
    $("#commentModal").on("hidden.bs.modal", function() {
        commentSubscription.unsubscribe();
    });

    $("#replyModal").on("hidden.bs.modal", function() {
        replySubscription.unsubscribe();
        // Resubscribe to SendTo Comment URI
        commentSubscription = stompClient.subscribe("/discussion" + commentURI, function(commentDto) {
            var json = JSON.parse(commentDto.body);
            generateCommentBlock(json);
        });
    });

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

function setCommentModalTitle(postId) {
    $.ajax({
        type: "GET",
        url: "/forum/api/posts/" + postId,
        success: function(commentDto, response) {
            $("#commentModalTitle").text("Comments in " + commentDto.authorName + " post");
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}

function setReplyModalTitle(commentId) {
    $.ajax({
        type: "GET",
        url: "/forum/api" + commentURI + "/" + commentId,
        success: function(commentDto, response) {
            $("#replyModalTitle").text("Replies in " + commentDto.commenterName + " comment in " + commentDto.authorName + " post");
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
    $("#replyModalTitle").text();
}

function savePost(body) {
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

function saveComment(body) {
    $.ajax({
        type: "POST",
        url: "/forum/api" + commentURI,
        data: {
            body: body
        },
        success: function(response, status, xhr) {
            console.log("Returned CommentDTO" + xhr.responseText);
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}

function saveReply(body) {
    $.ajax({
        type: "POST",
        url: "/forum/api" + replyURI,
        data: {
            body: body
        },
        success: function(response, status, xhr) {
            console.log("Returned ReplyDTO " + xhr.responseText);
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}

function getAllCommentsOf(commentURI) {
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

function getAllReplies(replyURI) {
    $.ajax({
        type: "GET",
        url: "/forum/api" + replyURI,
        success: function(replyDtos, response) {
            var replySection = $(".modal-body #replySection");
            replySection.empty(); // Removes the recent comments in the modal

            $.each(replyDtos, function(index, replyDto) {
                generateReplyBlock(replyDto);
            });
        },
        error: function(xhr, response, error) {
            alert(xhr.responseText);
        }
    });
}

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

// Don't bother reading this code
// The actual html structure of this the comment-body is in /templates/fragments/comment-body
function generateCommentBlock(commentDto) {
    var commentSection = $(".modal-body #commentSection");
    var container = $("<div>")
        .attr("class", "commentContainer")
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
        "src": "/img/" + commentDto.commenterPicture
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
        "data-bs-toggle": "modal",
        "data-bs-target": "#replyModal",
        "data-bs-dismiss": "modal",
        "type": "button",
        "id": "replyBtn",
        "href": "/posts/comments/" + commentDto.id + "/replies",
        "class": "btn btn-primary me-1"
    }).text("Reply").appendTo(row3Col1);

    var timeCommented = $("<span>")
        .text(" at " + commentDto.formattedTime + " on " + commentDto.formattedDate)
        .appendTo(row3Col1);

    var hr = $("<hr>").appendTo(commentSection);

    replyBtn.on("click", function(event) {
        replyURI = $(this).attr("href");
        console.log(replyURI);

        var commentId = replyURI.split("/")[3];
        setReplyModalTitle(commentId);

        // SendTo URI of Reply
        replySubscription = stompClient.subscribe("/discussion" + replyURI, function(replyDto) {
            var json = JSON.parse(replyDto.body);
            generateReplyBlock(json);
        });

        getAllReplies(replyURI);
    });
}

// Don't bother reading this code
// The actual html structure of this the comment-body is in /templates/fragments/comment-body
function generateReplyBlock(replyDto) {
    var replySection = $(".modal-body #replySection");
    var replyContainer = $("<div>")
        .attr("class", "replyContainer")
        .appendTo(replySection);

    var row1 = $("<div>")
        .attr("class", "row mb-2")
        .appendTo(replyContainer);

    var row1Col1 = $("<div>")
        .attr("class", "md-col")
        .appendTo(row1);

    var replierImage = $("<img>").attr({
        "class": "rounded-circle shadow-4-strong",
        "height": "50px",
        "width": "50px",
        "src": "/img/" + replyDto.replierPicture
    }).appendTo(row1Col1);

    var replyName = $("<span>")
        .attr("class", "mb-5")
        .text(replyDto.replierName)
        .appendTo(row1Col1);

    var row2 = $("<div>")
        .attr("class", "row")
        .appendTo(replyContainer);

    var row2Col1 = $("<div>")
        .attr("class", "md-col")
        .appendTo(row2);

    var replyMessageBody = $("<p>")
        .attr("class", "mt-2")
        .text(replyDto.body)
        .appendTo(row2);

    var hr = $("<hr>").appendTo(replySection);
}