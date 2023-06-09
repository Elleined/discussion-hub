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
            if (json.status === "INACTIVE") {
                $("div").filter("#comment_" + json.id).remove();
                updateCommentCount(json.postId, "-");
                return;
            }
            generateCommentBlock(json);
            updateCommentCount(json.postId, "+");
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

    $(".card-title #postDeleteBtn").on("click", function(event) {
        event.preventDefault();

        var href = $(this).attr("href");
        deletePost(href);
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
        // SendTo URI of Comment
        commentSubscription = stompClient.subscribe("/discussion" + commentURI, function(commentDto) {
            var json = JSON.parse(commentDto.body);
            if (json.status === "INACTIVE") {
                $("div").filter("#comment_" + json.id).remove();
                updateCommentCount(json.postId, "-");
                return;
            }
            generateCommentBlock(json);
            updateCommentCount(json.postId, "+");
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

function updateUpvote(commentId, newUpvoteCount, originalUpdateValue) {
    $.ajax({
        type: "PATCH",
        url: "/forum/api" + commentURI + "/" + commentId,
        data: {
            newUpvoteCount: newUpvoteCount
        },
        success: function(commentDto, response) {
            console.log("Comment with id of " + commentId + "updated successfully with new upvote count of " + newUpvoteCount);
        },
        error: function(xhr, status, error) {
            $("#upvoteValue" + commentId).text(originalUpdateValue); // Reset the upvote value to the original value from the server
            alert(xhr.responseText);
        }
    });
}

function deletePost(postURI) {
    $.ajax({
        type: "DELETE",
        url: "/forum/api" + postURI,
        success: function(postDto, response) {
            window.location.href = "/forum";
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Deletion of post failed!");
        }
    });
}

function deleteComment(commentURI) {
    $.ajax({
        type: "DELETE",
        url: commentURI,
        success: function(commentDto, response) {
            console.log("Comment deleted successfully");
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Deletion of comment failed!");
        }
    });
}

function deleteReply(deleteReplyURI) {
    $.ajax({
        type: "DELETE",
        url: deleteReplyURI,
        success: function(commentDto, response) {
            console.log("Reply deleted successfully");
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Deletion of reply failed!");
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
        .attr({
            "class": "commentContainer container ms-5",
            "id": "comment_" + commentDto.id
        })
        .appendTo(commentSection);

    var childContainer = $("<div>")
        .attr("class", "row gx-5 ")
        .appendTo(container);

    generateCommentUpvoteBlock(childContainer, commentDto);

    var commentColumn = $("<div>")
        .attr("class", "col-md-6")
        .appendTo(childContainer);

    generateCommentHeader(commentColumn, commentDto);

    var row2 = $("<div>")
        .attr("class", "row")
        .appendTo(commentColumn);

    var row2Col1 = $("<div>")
        .attr("class", "md-col")
        .appendTo(row2);

    var commenterMessageBody = $("<p>")
        .attr("class", "mt-2")
        .text(commentDto.body)
        .appendTo(row2Col1);

    var row3 = $("<div>")
        .attr("class", "row")
        .appendTo(commentColumn);

    var row3Col1 = $("<div>")
        .attr("class", "md-col")
        .appendTo(row3);

    var replyBtn = $("<button>").attr({
        "data-bs-toggle": "modal",
        "data-bs-target": "#replyModal",
        "data-bs-dismiss": "modal",
        "type": "button",
        "id": "replyBtn" + commentDto.id,
        "href": "/posts/comments/" + commentDto.id + "/replies",
        "class": "btn btn-primary me-1",
        "value": commentDto.totalReplies
    }).text("Reply  ·  " + commentDto.totalReplies).appendTo(row3Col1);

    var timeCommented = $("<span>")
        .text(" at " + commentDto.formattedTime + " on " + commentDto.formattedDate)
        .appendTo(row3Col1);

    var hr = $("<hr>").appendTo(childContainer);

    replyBtn.on("click", function(event) {
        replyURI = $(this).attr("href");
        console.log(replyURI);

        var commentId = replyURI.split("/")[3];
        setReplyModalTitle(commentId);

        // SendTo URI of Reply
        replySubscription = stompClient.subscribe("/discussion" + replyURI, function(replyDto) {
            var json = JSON.parse(replyDto.body);
            if (json.status === "INACTIVE") {
                $("div").filter("#reply_" + json.id).remove();
                updateReplyCount(json.commentId, "-");
                updateCommentCount(json.postId, "-");
                return;
            }
            generateReplyBlock(json);
            updateReplyCount(json.commentId, "+");
            updateCommentCount(json.postId, "+");
        });

        getAllReplies(replyURI);
    });
}

// Don't bother reading this code
// The actual html structure of this the comment-body is in /templates/fragments/comment-body
function generateReplyBlock(replyDto) {
    var replySection = $(".modal-body #replySection");
    var replyContainer = $("<div>")
        .attr({
            "class": "replyContainer",
            "id": "reply_" + replyDto.id
        })
        .appendTo(replySection);

    var row1 = $("<div>")
        .attr("class", "row mb-2")
        .appendTo(replyContainer);

    generateReplyHeader(row1, replyDto);

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

    var hr = $("<hr>").appendTo(replyContainer);
}

function generateCommentUpvoteBlock(container, dto) {
    var upvoteColumn = $("<div>")
        .attr("class", "col-md-1")
        .appendTo(container);

    var upvoteContainer = $("<div>")
        .attr("class", "row gx-5")
        .appendTo(upvoteColumn);

    var upvoteBtn = $("<a>")
        .attr("href", "#")
        .appendTo(upvoteContainer);

    var upvoteIcon = $("<i>")
        .attr("class", "fas fa-angle-up fa-3x")
        .appendTo(upvoteBtn);

    var upvoteValue = $("<span>")
        .attr({
            "class": "d-flex justify-content-center mt-2 mb-2",
            "id": "upvoteValue" + dto.id
        })
        .text(dto.upvote)
        .appendTo(upvoteColumn);

    var downVoteContainer = $("<div>")
        .attr("class", "col-md-1")
        .appendTo(upvoteColumn);

    var downVoteContainer = $("<div>")
        .attr("class", "row gx-5")
        .appendTo(downVoteContainer);

    var downVoteBtn = $("<a>")
        .attr("href", "#")
        .appendTo(downVoteContainer);

    var downVoteIcon = $("<i>")
        .attr("class", "fas fa-angle-down fa-3x")
        .appendTo(downVoteBtn);

    let isClicked = false;
    upvoteBtn.on("click", function(event) {
        event.preventDefault();
        if (isClicked) return;
        let originalUpdateValue = parseInt($("#upvoteValue" + dto.id).text());
        var newUpvoteValue = originalUpdateValue + 1;
        $("#upvoteValue" + dto.id).text(newUpvoteValue);
        updateUpvote(dto.id, newUpvoteValue, originalUpdateValue);
        isClicked = true;
    });

    downVoteBtn.on("click", function(event) {
        event.preventDefault();
        if (isClicked) return;
        let originalUpdateValue = parseInt($("#upvoteValue" + dto.id).text());
        var newUpvoteValue = originalUpdateValue - 1;
        $("#upvoteValue" + dto.id).text(newUpvoteValue);
        updateUpvote(dto.id, newUpvoteValue, originalUpdateValue);
        isClicked = true;
    });
}

function generateCommentHeader(container, dto) {
    var parentContainer = $("<div>")
        .attr("class", "container")
        .appendTo(container);

    var row1 = $("<div>")
        .attr("class", "row")
        .appendTo(parentContainer);

    var row1Col1 = $("<div>")
        .attr("class", "col-md-6")
        .appendTo(row1);

    var commenterImage = $("<img>").attr({
        "class": "rounded-circle shadow-4-strong",
        "height": "50px",
        "width": "50px",
        "src": "/img/" + dto.commenterPicture
    }).appendTo(row1Col1);

    var commenterName = $("<span>")
        .attr("class", "md5 mb-5")
        .text(dto.commenterName)
        .appendTo(row1Col1);

    var userId = $("#userId").val();
    if (dto.commenterId == userId) {
        var row1Col2 = $("<div>")
            .attr("class", "col-md-6")
            .appendTo(row1);

        var row1Col1Container = $("<div>")
            .attr("class", "d-grid gap-2 d-md-flex justify-content-md-end")
            .appendTo(row1Col2);

        var deleteCommentBtn = $("<a>")
            .attr({
                "href": "/forum/api" + commentURI + "/" + dto.id,
                "role": "button",
                "class": "btn btn-danger",
                "id": "commentDeleteBtn" + dto.id
            })
            .text("Delete")
            .appendTo(row1Col1Container);

        var deleteIcon = $("<i>")
            .attr("class", "fas fa-trash")
            .appendTo(deleteCommentBtn);

        deleteCommentBtn.on("click", function(event) {
            event.preventDefault();

            var deleteCommentURI = $(this).attr("href");
            deleteComment(deleteCommentURI);
        });
    }
}

function generateReplyHeader(container, dto) {
    var parentContainer = $("<div>")
        .attr("class", "container")
        .appendTo(container);

    var row1 = $("<div>")
        .attr("class", "row")
        .appendTo(parentContainer);

    var row1Col1 = $("<div>")
        .attr("class", "col-md-6")
        .appendTo(row1);

    var commenterImage = $("<img>").attr({
        "class": "rounded-circle shadow-4-strong",
        "height": "50px",
        "width": "50px",
        "src": "/img/" + dto.replierPicture
    }).appendTo(row1Col1);

    var commenterName = $("<span>")
        .attr("class", "md5 mb-5")
        .text(dto.replierName)
        .appendTo(row1Col1);

    var userId = $("#userId").val();
    if (dto.replierId == userId) {
        var row1Col2 = $("<div>")
            .attr("class", "col-md-6")
            .appendTo(row1);

        var row1Col1Container = $("<div>")
            .attr("class", "d-grid gap-2 d-md-flex justify-content-md-end")
            .appendTo(row1Col2);

        var deleteReplyBtn = $("<a>")
            .attr({
                "href": "/forum/api" + replyURI + "/" + dto.id,
                "role": "button",
                "class": "btn btn-danger",
                "id": "replyDeleteBtn" + dto.id
            })
            .text("Delete")
            .appendTo(row1Col1Container);

        var deleteIcon = $("<i>")
            .attr("class", "fas fa-trash")
            .appendTo(deleteReplyBtn);

        deleteReplyBtn.on("click", function(event) {
            event.preventDefault();

            var deleteReplyURI = $(this).attr("href");
            deleteReply(deleteReplyURI);
        });
    }
}

function updateCommentCount(postId, operation) {
    const totalCommentsSpan = $("span").filter("#totalCommentsOfPost" + postId);
    let totalComments;
    if (operation == "+") {
        totalComments = parseInt(totalCommentsSpan.attr("aria-valuetext")) + 1;
    } else if (operation == "-") {
        totalComments = parseInt(totalCommentsSpan.attr("aria-valuetext")) - 1;
    } else {
        totalComments = parseInt(totalCommentsSpan.attr("aria-valuetext"));
    }
    totalCommentsSpan.text("Comments  ·  " + totalComments);
    totalCommentsSpan.attr("aria-valuetext", totalComments);
}

function updateReplyCount(commentId, operation) {
    const replyCountButton = $("div").filter("#replyBtn" + commentId);
    let replyCount;
    if (operation == "+") {
        replyCount = replyCountButton.attr("value") + 1;
    } else if (operation == "-") {
        replyCount = replyCountButton.attr("value") - 1;
    } else {
        replyCount = replyCount = replyCountButton.attr("value");
    }
    replyCountButton.text("Reply  · " + replyCount);
    replyCountButton.attr("value", replyCount);
}