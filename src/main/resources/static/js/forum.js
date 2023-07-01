'use strict';
import * as SaveRepository from './modules/save_repository.js';
import * as GetRepository from './modules/get_repository.js';
import * as UpdateRepository from './modules/update_repository.js';
import { mention, mentionedUsersId } from './modules/mention_user.js';

const socket = new SockJS("/websocket");
const stompClient = Stomp.over(socket);
stompClient.connect({}, onConnected, onError);

let replySubscription;
let commentSubscription;

let commentURI; // "/posts/{postId}/comments"
let replyURI; // "/posts/comments/{commentId}/replies"

let previousCommentBody; // Sets when user click the save button after clicking the comment edit
let previousReplyBody; // Sets when user click the save button after clicking the reply edit

$(document).ready(function() {
    $("#postForm").on("submit", function(event) {
        event.preventDefault();

        const body = $("#postBody").val();
        if ($.trim(body) === '') return;

        if (mentionedUsersId !== null) {
            SaveRepository.savePost(body, mentionedUsersId);
        } else {
           SaveRepository.savePost(body);
        }

        $("#postBody").val("");
        mentionedUsersId.clear();
    });

    $("#postBody").on("input", function(event) {
        const userId = $("#userId").val();
        const mentionList = $("#postMentionList");
        mention(userId, $(this), mentionList);
    });

    $(".card-title #postDeleteBtn").on("click", function(event) {
        event.preventDefault();

        const href = $(this).attr("href");
        deletePost(href);
    });

    $(".card-title #editPostBtn").on("click", function(event) {
        event.preventDefault();

        const href = $(this).attr("href");
        const postId = href.split("/")[3];

        const postContent = $("#postBody" + postId);
        postContent.attr("contenteditable", "true");
        postContent.focus();

        const editPostBtnSave = $("#editPostBtnSave" + postId);
        editPostBtnSave.removeClass("d-none");

        editPostBtnSave.on("click", function(event) {
            updatePostBody(href, postContent.text());
        });
    });

    $(".card-title #commentSectionStatusToggle").on("change", function() {
        const postId = $(this).attr("value");
        if ($(this).is(':checked')) {
            $(".card-title #commentSectionStatusText").text("Close comment section");
            updateCommentSectionStatus(postId, "OPEN");
            return;
        }
        $(".card-title #commentSectionStatusText").text("Open comment section");
        updateCommentSectionStatus(postId, "CLOSED");
    });

    $(".card-body #commentBtn").on("click", function(event) {
        commentURI = $(this).attr("href");
        console.log(commentURI);

        const postId = commentURI.split("/")[2];
        setCommentModalTitle(postId);

        subscribeToPostComments();

        const userId = $("#userId").val();
        saveTracker(userId, postId, "COMMENT");

        getAllCommentsOf(commentURI);
        getCommentSectionStatus(postId);

        updateTotalNotifCount(userId, postId, "COMMENT");
        event.preventDefault();
    });

    $(".row #blockBtn").on("click", function(event) {
        event.preventDefault();
        const href = $(this).attr("href");

        $("#blockModalBtn").on("click", function() {
            blockUser(href);
        });
    });

    $(".commentModal #commentForm").on("submit", function(event) {
        event.preventDefault();

        const body = $("#commentBody").val();
        if ($.trim(body) === '') return;
        if (mentionedUsersId !== null) {
            SaveRepository.saveComment(body, commentURI, mentionedUsersId);
        } else {
            SaveRepository.saveComment(body, commentURI);
        }

        $("#commentBody").val("");
        mentionedUsersId.clear();
    });

    $("#commentBody").on("input", function() {
        const userId = $("#userId").val();
        const mentionList = $("#commentMentionList");
        mention(userId, $(this), mentionList);
    });

    $(".replyModal #replyForm").on("submit", function(event) {
        event.preventDefault();

        const body = $("#replyBody").val();
        if ($.trim(body) === '') return;
        saveReply(body);

        $("#replyBody").val("");
    });

    // Below this making sure that socket and stompClient is closed
    $("#commentModal").on("hidden.bs.modal", function() {
        commentSubscription.unsubscribe();

        const userId = $("#userId").val();
        deleteTracker(userId, "COMMENT");
    });

    $("#replyModal").on("hidden.bs.modal", function() {
        replySubscription.unsubscribe();

        const userId = $("#userId").val();
        deleteTracker(userId, "REPLY");
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

function subscribeToPostComments() {
// SendTo URI of Comment
        const userId = $("#userId").val();
        commentSubscription = stompClient.subscribe("/discussion" + commentURI, function(commentDto) {
            const json = JSON.parse(commentDto.body);
            const commentContainer = $("div").filter("#comment_" + json.id);

            // Use for delete
            if (json.status === "INACTIVE") {
                commentContainer.remove();
                updateCommentCount(json.postId, "-");
                return;
            }

            // Use for block
            if (isUserBlocked(json.commenterId)) return;

            // Used for update
            if (previousCommentBody !== json.body && commentContainer.length) {
                $("#commentBody" + json.id).text(json.body);
                return;
            }

            generateCommentBlock(json);
            updateCommentCount(json.postId, "+");
        });
}

function subscribeToCommentReplies() {
 // SendTo URI of Reply
        replySubscription = stompClient.subscribe("/discussion" + replyURI, function(replyDto) {
            const json = JSON.parse(replyDto.body);
            const replyContainer = $("div").filter("#reply_" + json.id);

            // Use for delete
            if (json.status === "INACTIVE") {
                replyContainer.remove();
                updateReplyCount(json.commentId, "-");
                updateCommentCount(json.postId, "-");
                return;
            }

            // Use for block
            if (isUserBlocked(json.replierId)) return;

            // Use for update
            if (previousReplyBody !== json.body && replyContainer.length) {
                $("#replyBody" + json.id).text(json.body);
                return;
            }

            generateReplyBlock(json);
            updateReplyCount(json.commentId, "+");
            updateCommentCount(json.postId, "+");
        });
}

function isUserBlocked(id) {
          let blockedBy, youBeenBlockedBy;
            isBlockedBy(id).done(function(data) {
                blockedBy = data == true ? true : false;
            });
            isYouBeenBlockedBy(id).done(function(data) {
                youBeenBlockedBy = data == true ? true : false;
            });

            return blockedBy || youBeenBlockedBy;
}

// Use isUserBlocked method instead
function isBlockedBy(userToCheckId) {
    const userId = $("#userId").val();

    return $.ajax({
        type: "GET",
        url: "/forum/api/users/" + userId + "/isBlockedBy/" + userToCheckId,
        async: false,
        success: function(isBlockedBy, response) {
            console.log("Is " + userToCheckId + " blocked by " + userId + ": " + isBlockedBy);
        },
        error: function(xhr, status, response) {
            alert("Error Occurred! is user blocked failed to fetch!")
        }
    });
}

// Use isUserBlocked method instead
function isYouBeenBlockedBy(suspectedBlockerId) {
    const userId = $("#userId").val();
    return $.ajax({
        type: "GET",
        url: "/forum/api/users/" + userId + "/isYouBeenBlockedBy/" + suspectedBlockerId,
        async: false,
        success: function(isYouBeenBlockedBy, response) {
            console.log("Is you been blocked by " + userId + ": " + isYouBeenBlockedBy);
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Is you been blocked by failed to fetch!");
        }
    });
}

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
            alert("Error Occurred! Setting the reply modal title failed!");
        }
    });
    $("#replyModalTitle").text();
}

function blockUser(href) {
    $.ajax({
        type: "PATCH",
        url: href,
        success: function(response) {
            console.log("Successfully blocked this user with href of " + href);
            location.reload();
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Blocking this user failed!" + xhr.responseText);
        }
    });
}

async function saveTracker(userId, associatedTypeId, type) {
    try {
        await SaveRepository.saveTracker(userId, associatedTypeId, type);
        console.log("Saving the modal tracker for user with id of " + userId + " and associated id of " + associatedTypeId + " successful!");
    } catch (error) {
        alert("Error Occurred! Saving the modal tracker for this user failed!");
    }
}

async function saveReply(body) {
    try {
        const savedReply = await SaveRepository.saveReply(body, replyURI);
        console.log("Reply saved successfully");
        console.table(savedReply);
    } catch (error) {
        alert(error);
    }
}

async function getAllCommentsOf(commentURI) {
    try {
        $(".modal-body #commentSection").empty(); // Removes the recent comments in the modal

        const commentDTOs = await GetRepository.getAllCommentsOf(commentURI);
        $.each(commentDTOs, function(index, commentDto) {
            generateCommentBlock(commentDto);
        });
    } catch (error) {
        alert("Getting all comments failed! " + error);
    }
}

async function getAllReplies(replyURI) {
    try {
        $(".modal-body #replySection").empty(); // Removes the recent comments in the modal

        const replyDTOs = await GetRepository.getAllRepliesOf(replyURI);
        $.each(replyDTOs, function(index, replyDto) {
            generateReplyBlock(replyDto);
        });
    } catch (error) {
        alert("Getting all replies failed! " + error);
    }
}

async function getCommentSectionStatus(postId) {
    try {
        const commentSectionStatus = await GetRepository.getCommentSectionStatus(postId);
        if (commentSectionStatus === "CLOSED") {
            $(".disabledCommentAndReplySectionInfo").show();
            $(".replyModal #replyForm").hide();
            $(".commentModal #commentForm").hide();
            return;
        }
        $(".disabledCommentAndReplySectionInfo").hide();
        $(".replyModal #replyForm").show();
        $(".commentModal #commentForm").show();
    } catch (error) {
        alert("Getting the comment section status failed! " + error);
    }
}

async function updateCommentSectionStatus(postId, newStatus) {
    try {
        await UpdateRepository.updateCommentSectionStatus(postId, newStatus);
        console.log("Comment section status updated successfully to " + newStatus);
    } catch (error) {
        alert("Updating comment section status failed! " + error);
    }
}

async function updateCommentUpvote(commentId, newUpvoteCount, originalUpdateValue) {
    try {
        await UpdateRepository.updateCommentUpvote(commentId, newUpvoteCount, commentURI);
        $("#upvoteValue" + commentId).text(newUpvoteCount);
        console.log("Comment with id of " + commentId + " updated successfully with new upvote count of " + newUpvoteCount);
    } catch (error) {
        $("#upvoteValue" + commentId).text(originalUpdateValue); // Reset the upvote value to the original value from the server
        alert("Updating the comment upvote count failed! " + error);
    }
}

async function updatePostBody(href, newPostBody) {
    try {
        await UpdateRepository.updatePostBody(href, newPostBody);
        console.log("Post updated successfully with new body of " + newPostBody);

        const postId = href.split("/")[3];
        $("#postBody" + postId).attr("contenteditable", "false");
        $("#editPostBtnSave" + postId).addClass("d-none");
    } catch (error) {
        alert("Updating the post body failed! " + error);
    }
}

async function updateCommentBody(commentId, newCommentBody) {
    try {
        await UpdateRepository.updateCommentBody(commentId, newCommentBody, commentURI);
        console.log("Comment with id of " + commentId + " updated successfully with new comment body of " + newCommentBody);

        $("#commentBody" + commentId).attr("contenteditable", "false");
        $("#editCommentSaveBtn" + commentId).hide();
    } catch (error) {
        alert("Updating comment body failed! " + error);
    }
}

async function updateReplyBody(replyId, newReplyBody) {
    try {
        await UpdateRepository.updateReplyBody(replyId, newReplyBody, replyURI);
        console.log("Reply with id of " + replyId + " updated successfully with new reply body of " + newReplyBody);

        $("#replyBody" + replyId).attr("contenteditable", "false");
        $("#editReplySaveBtn" + replyId).hide();
    } catch (error) {
        alert("Updating reply body failed! " + error);
    }
}

async function updateTotalNotifCount(userId, id, type) {
    const totalNotifCountElement = $("#totalNotifCount");
    const notifCount = totalNotifCountElement.attr("aria-valuetext");
    try {
        const count = await UpdateRepository.updateTotalNotificationCount(userId, id, type);

        const newTotalNotifCount = parseInt(notifCount) - count;
        totalNotifCountElement.text(newTotalNotifCount + "+");
        totalNotifCountElement.attr("aria-valuetext", newTotalNotifCount);
        console.log("Updating the total notification count success!");
    } catch (error) {
        // alert("Updating total notification count failed! " + error); // ignore this becuase the current user might not be the author of the post or the commenter of the comment
    }
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

function deleteTracker(userId, type) {
    return $.ajax({
        type: "DELETE",
        url: "/forum/api/users/" + userId + "/deleteTracker",
        async: false,
        data: {
            type: type
        },
        success: function(response) {
            console.log("User with id of " + userId + " modal tracker deleted successfully!")
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Deleting the modal tracker of user with id of " + userId + " failed");
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
    const currentUserId = $("#userId").val();
    stompClient.subscribe("/user/notification/comments", function(notificationResponse) {
        const json = JSON.parse(notificationResponse.body);
        if (json.respondentId == currentUserId) return; // If the post author commented in his own post it will not generate a notification block
        if (json.modalOpen) return; // If the post author modal is open this will not generate a notification block

        updateTotalNotificationCount();
        if($("#notificationCommentItem_" + json.respondentId + "_" + json.id).length) {
            updateNotification(json.respondentId, json.id, json.type);
            return;
        }
        generateNotificationBlock(json);
    });

    stompClient.subscribe("/user/notification/replies", function(notificationResponse) {
        const json = JSON.parse(notificationResponse.body);
        if (json.respondentId == currentUserId) return; // If the post author replied in his own post it will not generate a notification block
        if (json.modalOpen) return; // If the comment author modal is open this will not generate a notification block

        updateTotalNotificationCount();
        if($("#notificationReplyItem_" + json.respondentId + "_" + json.id) .length) {
            updateNotification(json.respondentId, json.id, json.type);
            return;
        }

        generateNotificationBlock(json);
    });
}
function onError() {
    console.log("Could not connect to WebSocket server. Please refresh this page to try again!");
}

function updateNotification(respondentId, id, type) {
    if (type === "REPLY") {
        const messageCount = $("#messageReplyCount_" + respondentId + "_" + id);
        const newMessageCount = parseInt(messageCount.text()) + 1;
        messageCount.text(newMessageCount + "+");
        return
    }
    const messageCount = $("#messageCommentCount_" + respondentId + "_" + id);
    const newMessageCount = parseInt(messageCount.text()) + 1;
    messageCount.text(newMessageCount + "+");
}

function updateTotalNotificationCount() {
            const totalNotifCount = $("#totalNotifCount");
            const newTotalNotifCount = parseInt(totalNotifCount.text()) + 1;
            totalNotifCount.text(newTotalNotifCount + "+");
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

    const userId = $("#userId").val();
    if (commentDto.commenterId == userId) {
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
    }

    const row3 = $("<div>")
        .attr("class", "row")
        .appendTo(commentColumn);

    const row3Col1 = $("<div>")
        .attr("class", "md-col")
        .appendTo(row3);

    const replyBtn = $("<button>").attr({
        "data-bs-toggle": "modal",
        "data-bs-target": "#replyModal",
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

        subscribeToCommentReplies();

        const userId = $("#userId").val();
        saveTracker(userId, commentId, "REPLY");

        getAllReplies(replyURI);

        updateTotalNotifCount(userId, commentId, "REPLY");
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

    const userId = $("#userId").val();
    if (replyDto.replierId == userId) {
        const editReplySaveBtn = $("<button>")
            .attr({
                "type": "button",
                "class": "btn btn-primary",
                "href": "#",
                "id": "editReplySaveBtn" + replyDto.id
            }).text("Save").appendTo(row2Col2);
        editReplySaveBtn.hide();
    }

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
        updateCommentUpvote(dto.id, newUpvoteValue, originalUpdateValue);
        isClicked = true;
    });

    downVoteBtn.on("click", function(event) {
        event.preventDefault();
        if (isClicked) return;
        let originalUpdateValue = parseInt($("#upvoteValue" + dto.id).text());
        const newUpvoteValue = originalUpdateValue - 1;
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

    const notificationItemId = notificationResponse.type === "REPLY" ? "notificationReplyItem_" + notificationResponse.respondentId + "_" + notificationResponse.id
        : "notificationCommentItem_" + notificationResponse.respondentId + "_" + notificationResponse.id;
    const notificationItem = $("<li>")
        .attr({
            "class": "d-inline-flex position-relative ms-2 dropdown-item",
            "id": notificationItemId
        })
        .appendTo(notificationContainer);

    const messageCountId = notificationResponse.type === "REPLY" ? "messageReplyCount_" + notificationResponse.respondentId + "_" + notificationResponse.id
        : "messageCommentCount_" + notificationResponse.respondentId + "_" + notificationResponse.id;
    const messageCount = $("<span>")
        .attr({
            "class": "position-absolute top-0 start-100 translate-middle p-1 bg-success border border-light rounded-circle",
            "id": messageCountId
        }).text(1 + "+").appendTo(notificationItem);

    const senderImage = $("<img>").attr({
        "class": "rounded-4 shadow-4",
        "src": "/img/" + notificationResponse.respondentPicture,
        "style": "width: 50px; height: 50px;"
        }).appendTo(notificationItem);

        const notificationLink = $("<a>")
            .attr({
                "href": "#",
                "role": "button"
            }).appendTo(notificationItem);

    const notificationMessage = $("<p>")
        .attr("class", "lead mt-2 ms-2 me-2")
        .text(notificationResponse.message)
        .appendTo(notificationLink);

    const br = $("<br>").appendTo(notificationContainer);

    notificationLink.on("click", function(event) {
        event.preventDefault();

        const totalNotifCount = $("#totalNotifCount");
        const newTotalNotifCount = parseInt(totalNotifCount.text()) - parseInt(messageCount.text());
        totalNotifCount.text(newTotalNotifCount + "+");

        messageCount.text(0 + "+");

        notificationItem.remove();

        if (notificationResponse.type === "COMMENT") {
            const uri = notificationResponse.uri;
            const associatedBtn = $("a").filter(function() {
                return $(this).attr("href") === uri;
            }).last();

            associatedBtn.click();

            $("#commentModal").modal('show');
            const postId = uri.split("/")[2];
            getCommentSectionStatus(postId);
        }

        if (notificationResponse.type === "REPLY") {
            replyURI = notificationResponse.uri;
            commentURI = notificationResponse.commentURI;

            console.log(replyURI);

            const commentId = notificationResponse.uri.split("/")[3];
            setReplyModalTitle(commentId);

           subscribeToCommentReplies();

            const userId = $("#userId").val();
            saveTracker(userId, commentId, "REPLY");

            getAllReplies(replyURI);

            $("#replyModal").modal('show');
            const postId = commentURI.split("/")[2];
            getCommentSectionStatus(postId);

            updateTotalNotifCount(userId, commentId, "REPLY");
        }
    });
}