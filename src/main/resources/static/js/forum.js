'use strict';

const socket = new SockJS("/websocket");
const stompClient = Stomp.over(socket);
stompClient.connect({}, onConnected, onError);

let replySubscription;
let commentSubscription;

let commentURI; // Sets when user pressed the commentBtn in specific post
let replyURI; // Sets when user pressed the replyBtn in specific comment

let previousCommentBody; // Sets when user click the save button after clicking the comment edit
let previousReplyBody; // Sets when user click the save button after clicking the reply edit

$(document).ready(function() {
    const commentSection = $("#commentSection");

    $(".card-body #commentBtn").on("click", function(event) {
        commentURI = $(this).attr("href");
        console.log(commentURI);

        const postId = commentURI.split("/")[2];
        setCommentModalTitle(postId);

        // SendTo URI of Comment
        commentSubscription = stompClient.subscribe("/discussion" + commentURI, function(commentDto) {
            const json = JSON.parse(commentDto.body);
            const commentContainer = $("div").filter("#comment_" + json.id);
            if (json.status === "INACTIVE") {
                commentContainer.remove();
                updateCommentCount(json.postId, "-");
                return;
            }

            if (previousCommentBody !== json.body && commentContainer.length) {
                $("#commentBody" + json.id).text(json.body);
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

        const body = $("#postBody").val();
        if ($.trim(body) === '') return;
        savePost(body);

        $("#postBody").val("");
    });

    $(".commentModal #commentForm").on("submit", function(event) {
        event.preventDefault();

        const body = $("#commentBody").val();
        if ($.trim(body) === '') return;
        saveComment(body);

        $("#commentBody").val("");
    });

    $(".replyModal #replyForm").on("submit", function(event) {
        event.preventDefault();

        const body = $("#replyBody").val();
        if ($.trim(body) === '') return;
        saveReply(body);

        $("#replyBody").val("");
    });

    $(".card-title #postDeleteBtn").on("click", function(event) {
        event.preventDefault();

        const href = $(this).attr("href");
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
            const json = JSON.parse(commentDto.body);
            const commentContainer = $("div").filter("#comment_" + json.id);
            if (json.status === "INACTIVE") {
                commentContainer.remove();
                updateCommentCount(json.postId, "-");
                return;
            }

            if (previousCommentBody !== json.body && commentContainer.length) {
                $("#commentBody" + json.id).text(json.body);
                return;
            }

            generateCommentBlock(json);
            updateCommentCount(json.postId, "+");
        });
    });

    $("#logoutBtn").on("click", function() {
        disconnect();
    });

    $(window).on('beforeunload', function() {
        disconnect();
    });

    $(document).on('close', function() {
        disconnect();
    });

    $(window).on('unload', function() {
        disconnect();
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
            const commentSection = $(".modal-body #commentSection");
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
            const replySection = $(".modal-body #replySection");
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

function updateCommentUpvote(commentId, newUpvoteCount, originalUpdateValue) {
    $.ajax({
        type: "PATCH",
        url: "/forum/api" + commentURI + "/upvote/" + commentId,
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

function updateCommentBody(commentId, newCommentBody) {
    $.ajax({
        type: "PATCH",
        url: "/forum/api" + commentURI + "/body/" + commentId,
        data: {
            newCommentBody: newCommentBody
        },
        success: function(commentDto, response) {
            console.log("Comment with id of " + commentId + "updated successfully with new comment body of " + newCommentBody);
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
        }
    });
}

function updateReplyBody(replyId, newReplyBody) {
    $.ajax({
        type: "PATCH",
        url: "/forum/api" + replyURI + "/body/" + replyId,
        data: {
            newReplyBody: newReplyBody
        },
        success: function(commentDto, response) {
            console.log("Reply with id of " + replyId + "updated successfully with new reply body of " + newReplyBody);
        },
        error: function(xhr, status, error) {
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

function onConnected() {
    console.log("Web Socket Connected!!!");

    const authorId = $("#userId").val();
    stompClient.subscribe("/discussion/forum-notification/" + authorId, function(notificationResponse) {
        const json = JSON.parse(notificationResponse.body);
        generateNotificationBlock(json);
    });
}

function onError() {
    console.log("Could not connect to WebSocket server. Please refresh this page to try again!");
}

// Don't bother reading this code
// The actual html structure of this the comment-body is in /templates/fragments/comment-body
function generateCommentBlock(commentDto) {
    const commentSection = $(".modal-body #commentSection");
    const container = $("<div>")
        .attr({
            "class": "commentContainer container ms-5",
            "id": "comment_" + commentDto.id
        })
        .appendTo(commentSection);

    const childContainer = $("<div>")
        .attr("class", "row gx-5 ")
        .appendTo(container);

    generateCommentUpvoteBlock(childContainer, commentDto);

    const commentColumn = $("<div>")
        .attr("class", "col-md-6")
        .appendTo(childContainer);

    generateCommentHeader(commentColumn, commentDto);

    const row2 = $("<div>")
        .attr({
            "class": "row",
            "id": "commentMessageContainer" + commentDto.id
        })
        .appendTo(commentColumn);

    const row2Col1 = $("<div>")
        .attr("class", "col-md-10")
        .appendTo(row2);

    const commenterMessageBody = $("<p>")
        .attr({
            "class": "mt-2",
            "id": "commentBody" + commentDto.id
        })
        .text(commentDto.body)
        .appendTo(row2Col1);

    const row2Col2 = $("<div>")
        .attr("class", "col-md-2")
        .appendTo(row2);

    const editCommentSaveBtn = $("<button>")
        .attr({
            "type": "button",
            "class": "btn btn-primary",
            "href": "#",
            "id": "editCommentSaveBtn" + commentDto.id
        })
        .text("Save")
        .appendTo(row2Col2);
    editCommentSaveBtn.hide();

    const row3 = $("<div>")
        .attr("class", "row")
        .appendTo(commentColumn);

    const row3Col1 = $("<div>")
        .attr("class", "md-col")
        .appendTo(row3);

    const replyBtn = $("<button>").attr({
        "data-bs-toggle": "modal",
        "data-bs-target": "#replyModal",
        "data-bs-dismiss": "modal",
        "type": "button",
        "id": "replyBtn" + commentDto.id,
        "href": "/posts/comments/" + commentDto.id + "/replies",
        "class": "btn btn-primary me-1",
        "value": commentDto.totalReplies
    }).text("Reply  ·  " + commentDto.totalReplies).appendTo(row3Col1);

    const timeCommented = $("<span>")
        .text(" at " + commentDto.formattedTime + " on " + commentDto.formattedDate)
        .appendTo(row3Col1);

    const hr = $("<hr>").appendTo(childContainer);

    replyBtn.on("click", function(event) {
        replyURI = $(this).attr("href");
        console.log(replyURI);

        const commentId = replyURI.split("/")[3];
        setReplyModalTitle(commentId);

        // SendTo URI of Reply
        replySubscription = stompClient.subscribe("/discussion" + replyURI, function(replyDto) {
            const json = JSON.parse(replyDto.body);
            const replyContainer = $("div").filter("#reply_" + json.id);
            if (json.status === "INACTIVE") {
                replyContainer.remove();
                updateReplyCount(json.commentId, "-");
                updateCommentCount(json.postId, "-");
                return;
            }

            if (previousReplyBody !== json.body && replyContainer.length) {
                $("#replyBody" + json.id).text(json.body);
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
    const replySection = $(".modal-body #replySection");
    const replyContainer = $("<div>")
        .attr({
            "class": "replyContainer",
            "id": "reply_" + replyDto.id
        })
        .appendTo(replySection);

    const row1 = $("<div>")
        .attr("class", "row mb-2")
        .appendTo(replyContainer);

    generateReplyHeader(row1, replyDto);

    const row2 = $("<div>")
        .attr("class", "row")
        .appendTo(replyContainer);

    const row2Col1 = $("<div>")
        .attr("class", "col-md-10")
        .appendTo(row2);

    const replyBody = $("<p>")
        .attr({
            "class": "mt-2",
            "id": "replyBody" + replyDto.id
        })
        .text(replyDto.body)
        .appendTo(row2Col1);

    const row2Col2 = $("<div>")
        .attr("class", "col-md-2")
        .appendTo(row2);

    const editReplySaveBtn = $("<button>")
        .attr({
            "type": "button",
            "class": "btn btn-primary",
            "href": "#",
            "id": "editReplySaveBtn" + replyDto.id
        })
        .text("Save")
        .appendTo(row2Col2);
    // editReplySaveBtn.hide();

    const hr = $("<hr>").appendTo(replyContainer);
}

function generateCommentUpvoteBlock(container, dto) {
    const upvoteColumn = $("<div>")
        .attr("class", "col-md-1")
        .appendTo(container);

    const upvoteContainer = $("<div>")
        .attr("class", "row gx-5")
        .appendTo(upvoteColumn);

    const upvoteBtn = $("<a>")
        .attr("href", "#")
        .appendTo(upvoteContainer);

    const upvoteIcon = $("<i>")
        .attr("class", "fas fa-angle-up fa-3x")
        .appendTo(upvoteBtn);

    const upvoteValue = $("<span>")
        .attr({
            "class": "d-flex justify-content-center mt-2 mb-2",
            "id": "upvoteValue" + dto.id
        })
        .text(dto.upvote)
        .appendTo(upvoteColumn);

    const downVoteContainer = $("<div>")
        .attr("class", "row gx-5")
        .appendTo(upvoteColumn);

    const downVoteBtn = $("<a>")
        .attr("href", "#")
        .appendTo(downVoteContainer);

    const downVoteIcon = $("<i>")
        .attr("class", "fas fa-angle-down fa-3x")
        .appendTo(downVoteBtn);

    let isClicked = false;
    upvoteBtn.on("click", function(event) {
        event.preventDefault();
        if (isClicked) return;
        let originalUpdateValue = parseInt($("#upvoteValue" + dto.id).text());
        const newUpvoteValue = originalUpdateValue + 1;
        $("#upvoteValue" + dto.id).text(newUpvoteValue);
        updateCommentUpvote(dto.id, newUpvoteValue, originalUpdateValue);
        isClicked = true;
    });

    downVoteBtn.on("click", function(event) {
        event.preventDefault();
        if (isClicked) return;
        let originalUpdateValue = parseInt($("#upvoteValue" + dto.id).text());
        const newUpvoteValue = originalUpdateValue - 1;
        $("#upvoteValue" + dto.id).text(newUpvoteValue);
        updateCommentUpvote(dto.id, newUpvoteValue, originalUpdateValue);
        isClicked = true;
    });
}

function generateCommentHeader(container, dto) {
    const parentContainer = $("<div>")
        .attr("class", "container")
        .appendTo(container);

    const row1 = $("<div>")
        .attr("class", "row")
        .appendTo(parentContainer);

    const row1Col1 = $("<div>")
        .attr("class", "col-md-6")
        .appendTo(row1);

    const commenterImage = $("<img>").attr({
        "class": "rounded-circle shadow-4-strong",
        "height": "50px",
        "width": "50px",
        "src": "/img/" + dto.commenterPicture
    }).appendTo(row1Col1);

    const commenterName = $("<span>")
        .attr("class", "md5 mb-5")
        .text(dto.commenterName)
        .appendTo(row1Col1);

    const userId = $("#userId").val();
    if (dto.commenterId == userId) {
        const row1Col2 = $("<div>")
            .attr("class", "col-md-6")
            .appendTo(row1);

        const row1Col1Container = $("<div>")
            .attr("class", "d-grid gap-2 d-md-flex justify-content-md-end")
            .appendTo(row1Col2);

        const deleteCommentBtn = $("<a>")
            .attr({
                "href": "/forum/api" + commentURI + "/" + dto.id,
                "role": "button",
                "class": "btn btn-danger",
                "id": "commentDeleteBtn" + dto.id
            })
            .text("Delete")
            .appendTo(row1Col1Container);

        const deleteIcon = $("<i>")
            .attr("class", "fas fa-trash")
            .appendTo(deleteCommentBtn);

        const editCommentBtn = $("<a>")
            .attr({
                "href": "#",
                "role": "button",
                "class": "btn btn-primary",
                "id": "editCommentBtn" + dto.id
            })
            .text("Edit")
            .appendTo(row1Col1Container);

        const editIcon = $("<i>")
            .attr("class", "fas fa-pencil")
            .appendTo(editCommentBtn);

        deleteCommentBtn.on("click", function(event) {
            event.preventDefault();

            const deleteCommentURI = $(this).attr("href");
            deleteComment(deleteCommentURI);
        });

        editCommentBtn.on("click", function(event) {
            event.preventDefault();
            const editCommentSaveBtn = $("#editCommentSaveBtn" + dto.id);
            const commentBodyText = $("#commentBody" + dto.id);
            previousCommentBody = commentBodyText.text();

            commentBodyText.attr("contenteditable", "true");
            commentBodyText.focus();
            editCommentSaveBtn.show();

            // Adding the editCommentSaveBtn click listener only when user clicks the editCommentBtn
            editCommentSaveBtn.on("click", function() {
                commentBodyText.attr("contenteditable", "false");
                editCommentSaveBtn.hide();
                updateCommentBody(dto.id, commentBodyText.text());
            });
        });
    }
}

function generateReplyHeader(container, dto) {
    const parentContainer = $("<div>")
        .attr("class", "container")
        .appendTo(container);

    const row1 = $("<div>")
        .attr("class", "row")
        .appendTo(parentContainer);

    const row1Col1 = $("<div>")
        .attr("class", "col-md-6")
        .appendTo(row1);

    const commenterImage = $("<img>").attr({
        "class": "rounded-circle shadow-4-strong",
        "height": "50px",
        "width": "50px",
        "src": "/img/" + dto.replierPicture
    }).appendTo(row1Col1);

    const commenterName = $("<span>")
        .attr("class", "md5 mb-5")
        .text(dto.replierName)
        .appendTo(row1Col1);

    const userId = $("#userId").val();
    if (dto.replierId == userId) {
        const row1Col2 = $("<div>")
            .attr("class", "col-md-6")
            .appendTo(row1);

        const row1Col1Container = $("<div>")
            .attr("class", "d-grid gap-2 d-md-flex justify-content-md-end")
            .appendTo(row1Col2);

        const deleteReplyBtn = $("<a>")
            .attr({
                "href": "/forum/api" + replyURI + "/" + dto.id,
                "role": "button",
                "class": "btn btn-danger",
                "id": "replyDeleteBtn" + dto.id
            })
            .text("Delete")
            .appendTo(row1Col1Container);

        const deleteIcon = $("<i>")
            .attr("class", "fas fa-trash")
            .appendTo(deleteReplyBtn);

        const editReplyBtn = $("<a>")
            .attr({
                "href": "#",
                "role": "button",
                "class": "btn btn-primary",
                "id": "editReplyBtn" + dto.id
            })
            .text("Edit")
            .appendTo(row1Col1Container);

        const editReplyIcon = $("<i>")
            .attr("class", "fas fa-pencil")
            .appendTo(editReplyBtn);

        deleteReplyBtn.on("click", function(event) {
            event.preventDefault();

            const deleteReplyURI = $(this).attr("href");
            deleteReply(deleteReplyURI);
        });

        editReplyBtn.on("click", function(event) {
            event.preventDefault();
            const editReplySaveBtn = $("#editReplySaveBtn" + dto.id);
            const replyBody = $("#replyBody" + dto.id);
            previousReplyBody = replyBody.text();

            replyBody.attr("contenteditable", "true");
            replyBody.focus();
            editReplySaveBtn.show();

            // Adding the editReplySaveBtn click listener only when user clicks the editReplyBtn
            editReplySaveBtn.on("click", function() {
                replyBody.attr("contenteditable", "false");
                editReplySaveBtn.hide();
                updateReplyBody(dto.id, replyBody.text());
            });
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
    const replyCountButton = $("button").filter("#replyBtn" + commentId);
    let replyCount;
    if (operation == "+") {
        replyCount = parseInt(replyCountButton.attr("value")) + 1;
    } else if (operation == "-") {
        replyCount = parseInt(replyCountButton.attr("value")) - 1;
    } else {
        replyCount = replyCountButton.attr("value");
    }
    replyCountButton.text("Reply  · " + replyCount);
    replyCountButton.attr("value", replyCount);
    console.log("Reply count updated successfully " + replyCount);
}

function generateNotificationBlock(notificationResponse) {
    const notificationContainer = $("#notificationContainer");

    const notificationItem = $("<li>")
        .attr("class", "d-inline-flex position-relative ms-2 dropdown-item")
        .appendTo(notificationContainer);

    const messageCount = $("<span>")
        .attr("class", "position-absolute top-0 start-100 translate-middle p-1 bg-success border border-light rounded-circle")
        .text("1+")
        .appendTo(notificationItem);

    const senderImage = $("<img>").attr({
        "class": "rounded-4 shadow-4",
        "src": "/img/" + notificationResponse.commenterPicture,
        "style": "width: 50px; height: 50px;"
        }).appendTo(notificationItem);

    const notificationLink = $("<a>")
        .attr("href", "#")
        .appendTo(notificationItem);

    const notificationMessage = $("<p>")
        .attr("class", "lead mt-2 ms-2 me-2")
        .text(notificationResponse.message)
        .appendTo(notificationLink);

    const br = $("<br>").appendTo(notificationContainer);
}