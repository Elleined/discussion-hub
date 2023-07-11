'use strict';
import * as SaveRepository from './modules/repository/save_repository.js';
import * as GetRepository from './modules/repository/get_repository.js';
import * as UpdateRepository from './modules/repository/update_repository.js';
import * as DeleteRepository from './modules/repository/delete_repository.js';
import generateComment, { previousCommentBody } from './modules/generator/comment_generator.js';
import generateReply, { previousReplyBody } from './modules/generator/reply_generator.js';
import generateNotification, { updateNotification, updateTotalNotificationCount, generateMention } from './modules/generator/notification_generator.js';
import mention, { mentionedUsersId } from './modules/mention_user.js';

const socket = new SockJS("/websocket");
const stompClient = Stomp.over(socket);
stompClient.connect({},
    onConnected,
    () => console.log("Could not connect to WebSocket server. Please refresh this page to try again!"));

let replySubscription;
let commentSubscription;

let globalPostId; // Sets when user clicked the comments
let globalCommentId; // Sets when user clicked replies

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
        globalPostId = $(this).attr("href").split("/")[2];

        const postId = globalPostId;
        GetRepository.getPostById(postId)
            .then(res => $("#commentModalTitle").text("Comments in " + res.authorName + " post"))
            .catch(error => alert(error));

        subscribeToPostComments(postId);

        const userId = $("#userId").val();
        SaveRepository.saveTracker(userId, postId, "COMMENT");

        getAllCommentsOf(postId);
        getCommentSectionStatus(postId);


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
            SaveRepository.saveComment(body, globalPostId, mentionedUsersId);
        } else {
            SaveRepository.saveComment(body, globalPostId);
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
            SaveRepository.saveReply(body, globalCommentId, mentionedUsersId);
        } else {
            SaveRepository.saveReply(body, globalCommentId);
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
        const commentContainer = $("div").filter("#commentContainer" + json.id);

        // Use for delete
        if (json.status === "INACTIVE") {
            commentContainer.remove();
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
        generateComment(json, commentSection)
            .then(commentId => bindReplyBtn(commentId))
            .catch(error => alert("Binding replyBtn when generating comment failed! " + error));
    });
}

function subscribeToCommentReplies(commentId) {
    // SendTo URI of Reply
    const userId = $("#userId").val();
    replySubscription = stompClient.subscribe(`/discussion/posts/comments/${commentId}/replies`, function(replyDto) {
        const json = JSON.parse(replyDto.body);
        const replyContainer = $("div").filter("#replyContainer" + json.id);

        // Use for delete
        if (json.status === "INACTIVE") {
            replyContainer.remove();
            return;
        }

        // Use for block
        if (GetRepository.isUserBlocked(userId, json.replierId)) return;

        // Use for update
        if (previousReplyBody !== json.body && replyContainer.length) {
            $("#replyBody" + json.id).text(json.body);
            return;
        }

        const replySection = $(".modal-body #replySection");
        generateReply(json, replySection);
    });
}

function onConnected() {
    console.log("Web Socket Connected!!!");
    const currentUserId = $("#userId").val();
    const notificationContainer = $("#notificationContainer");

    stompClient.subscribe("/user/notification/comments", function(notificationResponse) {
        const json = JSON.parse(notificationResponse.body);
        if (json.respondentId == currentUserId) return; // If the post author commented in his own post it will not generate a notification block
        if (json.modalOpen) return; // If the post author modal is open this will not generate a notification block

        updateTotalNotificationCount();
        if ($("#notificationCommentItem_" + json.respondentId + "_" + json.id).length) {
            updateNotification(json.respondentId, json.id, json.type);
            return;
        }
        generateNotification(json, notificationContainer);
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
        generateNotification(json, notificationContainer);
    });

    stompClient.subscribe("/user/notification/mentions", function(notificationResponse) {
        const json = JSON.parse(notificationResponse.body);

        updateTotalNotificationCount();
        generateMention(json, notificationContainer);
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
            generateComment(commentDto, commentSection)
                .then(commentId => bindReplyBtn(commentId))
                .catch(error => alert("Binding replyBtn when generating comment failed! " + error));
        });
    } catch (error) {
        alert("Getting all comments failed! " + error);
    }
}

function bindReplyBtn(commentId) {
    $("#replyBtn" + commentId).on("click", function(event) {
        globalCommentId = commentId;
        setReplyModalTitle(commentId);

        subscribeToCommentReplies(commentId);

        const userId = $("#userId").val();
        SaveRepository.saveTracker(userId, commentId, "REPLY");

        getAllReplies(commentId);
    });
}

async function getAllReplies(commentId) {
    try {
        const replySection = $(".modal-body #replySection");
        replySection.empty();

        const replyDTOs = await GetRepository.getAllRepliesOf(commentId);
        $.each(replyDTOs, function(index, replyDto) {
            generateReply(replyDto, replySection);
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

function disconnect() {
    if (stompClient) {
        stompClient.disconnect();
    }
    if (socket) {
        socket.close();
    }
}