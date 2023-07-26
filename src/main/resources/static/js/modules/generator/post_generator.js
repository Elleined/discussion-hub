import { getPostBlock } from '../repository/get_repository.js';
import { deletePost } from '../repository/delete_repository.js';
import { postLike } from '../like.js';
import { updatePostBody } from '../repository/update_repository.js';

const generatePost = (postDto, postContainer) => {
    getPostBlock(postDto)
        .then(res => {
            postContainer.append(res);
            bindDeleteBtn(postDto.id);
            bindLikeBtn(postDto.id);
            bindEditPostBtn(postDto.id);
        }).catch((xhr, status, error) => alert("Error Occurred! Cannot generate post! " + xhr.responseText));
};

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