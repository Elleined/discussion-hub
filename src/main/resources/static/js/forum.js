'use strict';
import * as SaveRepository from './modules/repository/save_repository.js';
import * as GetRepository from './modules/repository/get_repository.js';
import * as UpdateRepository from './modules/repository/update_repository.js';
import * as DeleteRepository from './modules/repository/delete_repository.js';
import { postLike } from './modules/like.js';
import highlightMention from './modules/highlight_mention.js';
import generatePost from './modules/generator/post_generator.js';
import uploadPhoto, {
   getAttachedPicture,
   clearAttachedPicture
} from './modules/upload_photo.js';
import generateComment, {
   getPreviousCommentBody
} from './modules/generator/comment_generator.js';
import generateReply, {
   getPreviousReplyBody
} from './modules/generator/reply_generator.js';
import generateNotification, {
   updateTotalNotificationCount,
   generateAllNotification

} from './modules/generator/notification_generator.js';
import mention, {
   getMentionedUsers,
   clearMentionedUsers
} from './modules/mention_user.js';

const socket = new SockJS("/websocket");
const stompClient = Stomp.over(socket);
stompClient.connect({},
   onConnected,
   () => console.log("Could not connect to WebSocket server. Please refresh this page to try again!"));

let replySubscription;
let commentSubscription;

let globalPostId; // Sets when user clicked the comments
let globalCommentId; // Sets when user clicked replies

$(document).ready(function () {
   bindGenerateAllNotification();
   bindUploadPhoto();
   getAllPost();

   $("#postForm").on("submit", function (event) {
      event.preventDefault();
      const postSection = $("#postSection");
      const body = $("#postBody").val();

      if ($.trim(body) === '') return;
      const attachedPicture = getAttachedPicture();
      const mentionedUsers = getMentionedUsers();
      SaveRepository.savePost(body, attachedPicture, mentionedUsers)
         .then(postDto => {
            generatePost(postDto, postSection);
            console.table(postDto);
         }).catch((xhr, status, error) => alert(xhr.responseText));

      $("#postBody").val("");
      clearMentionedUsers();
      $("#postImagePreview").addClass("d-none");
      clearAttachedPicture();
   });

   $("#postBody").on("input", function (event) {
      const userId = $("#userId").val();
      const mentionList = $("#postMentionList");
      mention(userId, $(this), mentionList);
   });

   $(".commentModal #commentForm").on("submit", function (event) {
      event.preventDefault();
      const body = $("#commentBody").val();

      if ($.trim(body) === '') return;
      const attachedPicture = getAttachedPicture();
      const mentionedUsers = getMentionedUsers();
      SaveRepository.saveComment(body, globalPostId, attachedPicture, mentionedUsers)
         .then(res => console.table(res))
         .catch((xhr, status, error) => alert("Error Occurred! Cannot save comment " + xhr.responseText));

      $("#commentBody").val("");
      $("#commentImagePreview").addClass("d-none");
      clearAttachedPicture();
      clearMentionedUsers();
   });

   $("#commentBody").on("input", function () {
      const userId = $("#userId").val();
      const mentionList = $("#commentMentionList");
      mention(userId, $(this), mentionList);
   });

   $(".replyModal #replyForm").on("submit", function (event) {
      event.preventDefault();

      const body = $("#replyBody").val();
      if ($.trim(body) === '') return;
      const mentionedUsers = getMentionedUsers();
      const attachedPicture = getAttachedPicture();
      SaveRepository.saveReply(body, globalCommentId, attachedPicture, mentionedUsers)
         .then(res => console.table(res))
         .catch((xhr, status, error) => alert("Error Occurred! Cannot save reply " + xhr.responseText));

      $("#replyBody").val("");
      $("#replyImagePreview").addClass("d-none");
      clearAttachedPicture();
      clearMentionedUsers();
   });

   $("#replyBody").on("input", function (event) {
      const userId = $("#userId").val();
      const mentionList = $("#replyMentionList");
      mention(userId, $(this), mentionList);
   });

   // Below this making sure that socket and stompClient is closed
   $("#commentModal").on("hidden.bs.modal", function () {
      commentSubscription.unsubscribe();

      const userId = $("#userId").val();
      DeleteRepository.deleteTracker(userId, "COMMENT");
   });

   $("#replyModal").on("hidden.bs.modal", function () {
      replySubscription.unsubscribe();

      const userId = $("#userId").val();
      DeleteRepository.deleteTracker(userId, "REPLY");
   });

   $("#logoutBtn").on("click", function () {
      disconnect();
   });

   $(window).on('beforeunload', function () {
      disconnect();
      const userId = $("#userId").val();
      DeleteRepository.deleteTracker(userId, "COMMENT");
      DeleteRepository.deleteTracker(userId, "REPLY");
   });

   $(document).on('close', function () {
      disconnect();
   });
   // insert here
});

function subscribeToPostComments(postId) {
   // SendTo URI of Comment
   const userId = $("#userId").val();
   commentSubscription = stompClient.subscribe(`/discussion/posts/${postId}/comments`, function (commentDto) {
      const json = JSON.parse(commentDto.body);
      const commentContainer = $("#commentContainer" + json.id);

      // Use for delete
      if (json.status === "INACTIVE") {
         commentContainer.remove();
         return;
      }

      // Use for block
      if (GetRepository.isUserBlocked(userId, json.commenterId)) return;

      // Used for update
      if (getPreviousCommentBody() !== json.body && commentContainer.length) {
         $("#commentBody" + json.id).text(json.body);
         return;
      }

      // Used for update
      if (getPreviousCommentBody() === json.body && commentContainer.length) {
         $("#commentBody" + json.id).text(json.body);
         return;
      }

      const commentSection = $(".modal-body #commentSection");
      generateComment(json, commentSection);
   });
}

export function subscribeToCommentReplies(commentId) {
   // SendTo URI of Reply
   const userId = $("#userId").val();
   replySubscription = stompClient.subscribe(`/discussion/posts/comments/${commentId}/replies`, function (replyDto) {
      const json = JSON.parse(replyDto.body);
      const replyContainer = $("#replyContainer" + json.id);

      // Use for delete
      if (json.status === "INACTIVE") {
         replyContainer.remove();
         return;
      }

      // Use for block
      if (GetRepository.isUserBlocked(userId, json.replierId)) return;

      // Use for update
      if (getPreviousReplyBody() !== json.body && replyContainer.length) {
         $("#replyBody" + json.id).text(json.body);
         return;
      }

      if (getPreviousReplyBody() === json.body && replyContainer.length) {
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

   stompClient.subscribe("/user/notification/comments", function (notificationResponse) {
      const json = JSON.parse(notificationResponse.body);
      if (json.respondentId == currentUserId) return; // If the post author commented in his own post it will not generate a notification block
      if (json.notificationStatus === "READ") return; // If the post author modal is open this will not generate a notification block

      updateTotalNotificationCount(currentUserId);
      generateNotification(json, notificationContainer);
   });

   stompClient.subscribe("/user/notification/replies", function (notificationResponse) {
      const json = JSON.parse(notificationResponse.body);
      if (json.respondentId == currentUserId) return; // If the post author replied in his own post it will not generate a notification block
      if (json.notificationStatus === "READ") return; // If the comment author modal is open this will not generate a notification block

      updateTotalNotificationCount(currentUserId);
      generateNotification(json, notificationContainer);
   });
}

async function getAllPost() {
    try {
        const postSection = $("#postSection");
        const postDtos = await GetRepository.getAllPost();
        $.each(postDtos, function(index, postDto) {
            generatePost(postDto, postSection);
        });
    } catch(error) {
        alert("Error Occurred! Generating all post failed! " + error);
    }
}

async function getAllCommentsOf(postId) {
   try {
      const commentSection = $(".modal-body #commentSection"); // Removes the recent comments in the modal
      commentSection.empty();

      const commentDTOs = await GetRepository.getAllCommentsOf(postId);
      $.each(commentDTOs, function (index, commentDto) {
         generateComment(commentDto, commentSection);
      });
   } catch (error) {
      alert("Getting all comments failed! " + error);
   }
}

async function getAllReplies(commentId) {
   try {
      const replySection = $(".modal-body #replySection");
      replySection.empty();

      const replyDTOs = await GetRepository.getAllRepliesOf(commentId);
      $.each(replyDTOs, function (index, replyDto) {
         generateReply(replyDto, replySection);
      });
   } catch (error) {
      alert("Getting all replies failed! " + error);
   }
}

export function bindCommentBtn(postId) {
   globalPostId = postId;
   subscribeToPostComments(postId);

   GetRepository.getPostById(postId)
      .then(res => $("#commentModalTitle").text("Comments in " + res.authorName + " post: " + res.body))
      .catch(error => alert(error));

   getCommentSectionStatus(postId);

   getAllCommentsOf(postId);

   const userId = $("#userId").val();
   SaveRepository.saveTracker(userId, postId, "COMMENT");
}

export function bindReplyBtn(commentId, postId) {
   globalCommentId = commentId;

   subscribeToCommentReplies(commentId);
   GetRepository.getCommentById(commentId)
      .then(commentDto => $("#replyModalTitle").text(`Replies in ${commentDto.commenterName} comment: ${commentDto.body} in ${commentDto.authorName} post: ${commentDto.postBody}`))
      .catch(error => alert("Setting reply modal title failed! " + error));

   getCommentSectionStatus(postId);

   getAllReplies(commentId);

   const userId = $("#userId").val();
   SaveRepository.saveTracker(userId, commentId, "REPLY");
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

function bindUploadPhoto() {
    // post
    const postUploadBtn = $("#postUploadBtn");
    const postFileInput = $("#postFileInput");
    const postImagePreview = $("#postImagePreview");
    uploadPhoto(postUploadBtn, postFileInput, postImagePreview);
   // comment
   const commentUploadBtn = $("#commentUploadBtn");
   const commentFileInput = $("#commentFileInput");
   const commentImagePreview = $("#commentImagePreview");
   uploadPhoto(commentUploadBtn, commentFileInput, commentImagePreview);

   // reply
   const replyUploadBtn = $("#replyUploadBtn");
   const replyFileInput = $("#replyFileInput");
   const replyImagePreview = $("#replyImagePreview");
   uploadPhoto(replyUploadBtn, replyFileInput, replyImagePreview);
}

function bindGenerateAllNotification() {
   const notificationContainer = $("#notificationContainer");
   const userId = $("#userId").val();
   generateAllNotification(userId, notificationContainer);
}

function disconnect() {
   if (stompClient) {
      stompClient.disconnect();
   }
   if (socket) {
      socket.close();
   }
}