import { getPostBlock } from '../repository/get_repository.js';
import { deletePost } from '../repository/delete_repository.js';
import { postLike } from '../like.js';
import { updatePostBody, updateCommentSectionStatus } from '../repository/update_repository.js';
import highlightMention from '../highlight_mention.js';
import { bindCommentBtn } from '../../forum.js';

const generatePost = (postDto, postContainer) => {
    getPostBlock(postDto)
        .then(res => {
            postContainer.prepend(res);
            bindCommentButton(postDto.id);
            bindDeleteBtn(postDto.id);
            bindLikeBtn(postDto.id);
            bindEditPostBtn(postDto.id);
            bindCommentSectionToggle(postDto.id);
            highlightMention(postDto.body, postDto.mentionedUsers, $("#postBody" + postDto.id));
        }).catch((xhr, status, error) => alert("Error Occurred! Cannot generate post! " + xhr.responseText));
};

function bindCommentButton(postId) {
   $("#commentBtn" + postId).on("click", function (event) {
      bindCommentBtn(postId);
      event.preventDefault();
   });
}

function bindCommentSectionToggle(postId) {
   $("#commentSectionStatusToggle" + postId).on("change", function () {
      if ($(this).is(':checked')) {
         $("#commentSectionStatusText" + postId).text("Close comment section");
         updateCommentSectionStatus(postId, "OPEN");
         return;
      }
      $("#commentSectionStatusText" + postId).text("Open comment section");
      updateCommentSectionStatus(postId, "CLOSED");
   });
}

function bindEditPostBtn(postId) {
   $("#editPostBtn" + postId).on("click", function (event) {
      event.preventDefault();

      const postContent = $("#postBody" + postId);
      postContent.attr("contenteditable", "true");
      postContent.focus();

      const editPostBtnSave = $("#editPostBtnSave" + postId);
      editPostBtnSave.removeClass("d-none");

      editPostBtnSave.on("click", function (event) {
         updateBody(postId, postContent.text());
      });
   });
}

function bindLikeBtn(postId) {
   $("#likeBtn" + postId).on("click", function(event) {
        event.preventDefault();
        const currentUserId = $("#userId").val();
        postLike(postId, currentUserId, $(this));
   });
}

function bindDeleteBtn(postId) {
   $("#postDeleteBtn" + postId).on("click", function (event) {
      event.preventDefault();
      deletePost(postId)
        .then(res => $("#postContainer" + postId).remove())
        .catch(error => alert("Error Occurred! Deleting post failed! " + error));
   });
}

async function updateBody(postId, newPostBody) {
   try {
      await updatePostBody(postId, newPostBody);
      $("#postBody" + postId).attr("contenteditable", "false");
      $("#editPostBtnSave" + postId).addClass("d-none");
   } catch (error) {
      alert("Updating the post body failed! " + error);
   }
}

export default generatePost;