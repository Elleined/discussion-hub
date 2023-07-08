'use strict';
import * as SaveRepository from './modules/save_repository.js';
import * as GetRepository from './modules/get_repository.js';
import * as UpdateRepository from './modules/update_repository.js';
import * as DeleteRepository from './modules/delete_repository.js';
import generateComment, { previousCommentBody } from './modules/comment_generator.js';

import {
    mention,
    mentionedUsersId
} from './modules/mention_user.js';

const socket = new SockJS("/websocket");
const stompClient = Stomp.over(socket);
stompClient.connect({},
    onConnected,
    () => console.log("Could not connect to WebSocket server. Please refresh this page to try again!"));

let replySubscription;
let commentSubscription;

let postId; // Sets when user clicked the comments
let commentId; // Sets when user clicked replies

let previousReplyBody; // Sets when user click the save button after clicking the reply edit

$(document).ready(function() {
    $("#postForm").on("submit", function(event) {
        event.preventDefault();

        const body = $("#postBody").val();
        if ($.trim(body) === '') return;

        if (mentionedUsersId !== null || mentionedUsersId.size() !== 0) {
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
        DeleteRepository.deletePost(href);
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
            UpdateRepository.updateCommentSectionStatus(postId, "OPEN");
            return;
        }
        $(".card-title #commentSectionStatusText").text("Open comment section");
        UpdateRepository.updateCommentSectionStatus(postId, "CLOSED");
    });

    $(".card-body #commentBtn").on("click", function(event) {
        postId = $(this).attr("href").split("/")[2];

        GetRepository.getPostById(postId)
            .then(res => $("#commentModalTitle").text("Comments in " + res.authorName + " post"))
            .catch(error => alert(error));

        subscribeToPostComments(postId);

        const userId = $("#userId").val();
        SaveRepository.saveTracker(userId, postId, "COMMENT");

        getAllCommentsOf(postId);
        getCommentSectionStatus(postId);

        updateTotalNotifCount(userId, postId, "COMMENT");
        event.preventDefault();
    });

    $(".row #blockBtn").on("click", function(event) {
        event.preventDefault();
        const href = $(this).attr("href");

        $("#blockModalBtn").on("click", function() {
            SaveRepository.blockUser(href);
        });
    });

    $(".commentModal #commentForm").on("submit", function(event) {
        event.preventDefault();

        const body = $("#commentBody").val();
        if ($.trim(body) === '') return;
        if (mentionedUsersId !== null || mentionedUsersId.size() !== 0) {
            SaveRepository.saveComment(body, postId, mentionedUsersId);
        } else {
            SaveRepository.saveComment(body, postId);
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

        if (mentionedUsersId !== null || mentionedUsersId.size() !== 0) {
            SaveRepository.saveReply(body, commentId, mentionedUsersId);
        } else {
            SaveRepository.saveReply(body, commentId);
        }

        $("#replyBody").val("");
        mentionedUsersId.clear();
    });

    $("#replyBody").on("input", function(event) {
        const userId = $("#userId").val();
        const mentionList = $("#replyMentionList");
        mention(userId, $(this), mentionList);
    });

    // Below this making sure that socket and stompClient is closed
    $("#commentModal").on("hidden.bs.modal", function() {
        commentSubscription.unsubscribe();

        const userId = $("#userId").val();
        DeleteRepository.deleteTracker(userId, "COMMENT");
    });

    $("#replyModal").on("hidden.bs.modal", function() {
        replySubscription.unsubscribe();

        const userId = $("#userId").val();
        DeleteRepository.deleteTracker(userId, "REPLY");
    });

    $("#logoutBtn").on("click", function() {
        disconnect();
    });

    $(window).on('beforeunload, unload', function() {
        disconnect();
    });

    $(document).on('close', function() {
        disconnect();
    });
    // insert here
});

function subscribeToPostComments(postId) {
    // SendTo URI of Comment
    const userId = $("#userId").val();
    commentSubscription = stompClient.subscribe(`/discussion/posts/${postId}/comments`, function(commentDto) {
        const json = JSON.parse(commentDto.body);
        const commentContainer = $("div").filter("#comment_" + json.id);

        // Use for delete
        if (json.status === "INACTIVE") {
            commentContainer.remove();
            updateCommentCount(json.postId, "-");
            return;
        }

        // Use for block
        if (GetRepository.isUserBlocked(userId, json.commenterId)) return;

        // Used for update
        if (previousCommentBody !== json.body && commentContainer.length) {
            $("#commentBody" + json.id).text(json.body);
            return;
        }


        const commentSection = $(".modal-body #commentSection");
        generateComment(json.id, commentSection);
        updateCommentCount(json.postId, "+");
    });
}

function subscribeToCommentReplies(commentId) {
    // SendTo URI of Reply
    const userId = $("#userId").val();
    replySubscription = stompClient.subscribe(`/discussion/posts/comments/${commentId}/replies`, function(replyDto) {
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
        if (GetRepository.isUserBlocked(userId, json.replierId)) return;

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

async function setReplyModalTitle(commentId) {
    try {
        const comment = await GetRepository.getCommentById(commentId);
        $("#replyModalTitle").text("Replies in " + comment.commenterName + " comment in " + comment.authorName + " post");
    } catch(error) {
        alert("Error Occurred! Setting the reply modal title failed!" + error);
    }
}

async function getAllCommentsOf(postId) {
    try {
        const commentSection = $(".modal-body #commentSection"); // Removes the recent comments in the modal
        commentSection.empty();

        const commentDTOs = await GetRepository.getAllCommentsOf(postId);
        $.each(commentDTOs, function(index, commentDto) {
            generateComment(commentDto.id, commentSection);
            bindReplyBtn(commentId);
        });
    } catch (error) {
        alert("Getting all comments failed! " + error);
    }
}

function bindReplyBtn(commentId) {
    $("#replyBtn" + commentId).on("click", function(event) {
        setReplyModalTitle(commentId);

        subscribeToCommentReplies(commentId);

        const userId = $("#userId").val();
        SaveRepository.saveTracker(userId, commentId, "REPLY");

        getAllReplies(commentId);

        updateTotalNotifCount(userId, commentId, "REPLY");
    });
}

async function getAllReplies(commentId) {
    try {
        $(".modal-body #replySection").empty(); // Removes the recent comments in the modal

        const replyDTOs = await GetRepository.getAllRepliesOf(commentId);
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

async function updatePostBody(href, newPostBody) {
    try {
        await UpdateRepository.updatePostBody(href, newPostBody);

        const postId = href.split("/")[3];
        $("#postBody" + postId).attr("contenteditable", "false");
        $("#editPostBtnSave" + postId).addClass("d-none");
    } catch (error) {
        alert("Updating the post body failed! " + error);
    }
}

async function updateReplyBody(replyId, newReplyBody) {
    try {
        await UpdateRepository.updateReplyBody(replyId, newReplyBody);

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
        if ($("#notificationCommentItem_" + json.respondentId + "_" + json.id).length) {
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
        if ($("#notificationReplyItem_" + json.respondentId + "_" + json.id).length) {
            updateNotification(json.respondentId, json.id, json.type);
            return;
        }

        generateNotificationBlock(json);
    });

    stompClient.subscribe("/user/notification/mentions", function(mentionResponse) {
        const json = JSON.parse(mentionResponse.body);
        updateTotalNotificationCount();
        alert(`Message: ${json.message}`);
    });
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
                "href": `/forum/api/posts/comments/${dto.commentId}/replies/${dto.id}`,
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
            DeleteRepository.deleteReply(deleteReplyURI);
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

    const notificationItemId = notificationResponse.type === "REPLY" ? "notificationReplyItem_" + notificationResponse.respondentId + "_" + notificationResponse.id :
        "notificationCommentItem_" + notificationResponse.respondentId + "_" + notificationResponse.id;
    const notificationItem = $("<li>")
        .attr({
            "class": "d-inline-flex position-relative ms-2 dropdown-item",
            "id": notificationItemId
        })
        .appendTo(notificationContainer);

    const messageCountId = notificationResponse.type === "REPLY" ? "messageReplyCount_" + notificationResponse.respondentId + "_" + notificationResponse.id :
        "messageCommentCount_" + notificationResponse.respondentId + "_" + notificationResponse.id;
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
            commentId = notificationResponse.uri.split("/")[3];
            setReplyModalTitle(commentId);

            subscribeToCommentReplies(commentId);

            const userId = $("#userId").val();
            SaveRepository.saveTracker(userId, commentId, "REPLY");

            getAllReplies(commentId);

            $("#replyModal").modal('show');
            postId = notificationResponse.commentURI.split("/")[2];
            getCommentSectionStatus(postId);

            updateTotalNotifCount(userId, commentId, "REPLY");
        }
    });
}