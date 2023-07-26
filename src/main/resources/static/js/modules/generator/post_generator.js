import { getPostBlock } from '../repository/get_repository.js';
import { deletePost } from '../repository/delete_repository.js';

const generatePost = (postDto, postContainer) => {
    getPostBlock(postDto)
        .then(res => {
            postContainer.append(res);
            bindDeleteBtn(postDto.id);
        }).catch((xhr, status, error) => alert("Error Occurred! Cannot generate post! " + xhr.responseText));
};

function bindLikeBtn() {

}

function bindDeleteBtn(postId) {
   $("#postDeleteBtn" + postId).on("click", function (event) {
      event.preventDefault();
      deletePost(postId)
        .then(res => $("#postContainer" + postId).remove())
        .catch(error => alert("Error Occurred! Deleting post failed! " + error));
   });
}

export default generatePost;