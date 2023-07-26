import { likePost, likeComment, likeReply } from './repository/save_repository.js';
import { getLikeIcon } from './repository/get_repository.js';

const postLike = async (postId, currentUserId, likeBtn) => {
    try {
        const post = await likePost(postId, currentUserId);
        const likeIcon = await getLikeIcon(post.currentUserLikedPost);
        likeBtn.empty();
        likeBtn.append(likeIcon);
        console.log("User with id of " + currentUserId + " like/unlike post with id of " + postId);
    } catch(error) {
        alert("Error Occurred! Cannot like/unlike post! " + error);
    }
}

const commentLike = async (commentId, currentUserId, likeBtn) => {
    try {
        const comment = await likeComment(commentId, currentUserId);
        const likeIcon = await getLikeIcon(comment.currentUserLikedComment);
        likeBtn.empty(); // deletes the old like icon
        likeBtn.append(likeIcon); // add the new like icon
        console.log("User with id of " + currentUserId + " like/unlike comment with id of " + commentId);
    } catch (error) {
        alert("Error Occurred! Cannot like/unlike comment " + error);
    }
};

const replyLike = async (replyId, currentUserId, likeBtn) => {
    try {
        const reply = await likeReply(replyId, currentUserId);
        const likeIcon = await getLikeIcon(reply.currentUserLikedReply);
        likeBtn.empty(); // deletes the old like icon
        likeBtn.append(likeIcon); // add the new like icon
        console.log("User with id of " + currentUserId + " like/unlike reply with id of " + replyId);
    } catch(error) {
        alert("Error Occurred! Cannot like/unlike reply" + error);
    }
}

export {
    postLike,
    commentLike,
    replyLike
}