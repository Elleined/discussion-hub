import { updateCommentUpvote, updateCommentBody } from '../repository/update_repository.js';
import { deleteComment } from '../repository/delete_repository.js';
import { getCommentBlock } from '../repository/get_repository.js';
import { bindReplyBtn } from '../../forum.js';
import { saveTracker } from '../repository/save_repository.js';
import { commentLike } from '../like.js';
import highlightMention from '../highlight_mention.js';
let previousCommentBody = null;

const generateComment = (commentDto, container) => {
    getCommentBlock(commentDto)
        .then(res => {
            container.append(res);
            highlightMention(commentDto.body, commentDto.mentionedUsers, $("#commentBody" + commentDto.id));
            bindUpvoteAndDownVoteBtn(commentDto.id);
            bindCommentHeaderBtn(commentDto.id);
            bindLikeBtn(commentDto.id);
            $("#replyBtn" + commentDto.id).on("click", function (event) {
                bindReplyBtn(commentDto.id, commentDto.postId);
                event.preventDefault();
            });
        }).catch(error => alert("Generating the comment failed! " + error));
};

function bindLikeBtn(commentId) {
            $("#commentLikeBtn" + commentId).on("click", function(event) {
                const currentUserId = $("#currentUserId").val();
                commentLike(commentId, currentUserId, $(this));
                event.preventDefault();
            });
}

function bindCommentHeaderBtn(commentId) {
    $("#commentDeleteBtn" + commentId).on("click", function(event) {
        event.preventDefault();
        deleteComment(commentId);
    });

    const editCommentSaveBtn = $("#editCommentSaveBtn" + commentId);
    editCommentSaveBtn.hide();
    $("#editCommentBtn" + commentId).on("click", function(event) {
        event.preventDefault();
        const commentBodyText = $("#commentBody" + commentId);
        previousCommentBody = commentBodyText.text();

        commentBodyText.attr("contenteditable", "true");
        commentBodyText.focus();
        editCommentSaveBtn.show();

        // Adding the editCommentSaveBtn click listener only when user clicks the editCommentBtn
        editCommentSaveBtn.on("click", function() {
            updateBody(commentId, commentBodyText.text());
            previousCommentBody = null;
        });
    });
}

function bindUpvoteAndDownVoteBtn(commentId) {
    let isClicked = false;
    $("#upvoteBtn" + commentId).on("click", function(event) {
        event.preventDefault();
        if (isClicked) return;
        const currentUpvoteCount = $("#upvoteValue" + commentId).text();
        updateUpvote(commentId, currentUpvoteCount);
        isClicked = true;
    });

    $("#downvoteBtn" + commentId).on("click", function(event) {
        event.preventDefault();
        if (isClicked) return;
        let currentUpvoteCount = $("#upvoteValue" + commentId).text();
        updateUpvote(commentId, currentUpvoteCount);
        isClicked = true;
    });
}

async function updateUpvote(commentId, currentUpvoteCount) {
    try {
        await updateCommentUpvote(commentId);

        console.log("Comment with id of " + commentId + " updated successfully");
        $("#upvoteValue" + commentId).text(parseInt(currentUpvoteCount) + 1);
    } catch (error) {
        $("#upvoteValue" + commentId).text(currentUpvoteCount); // Reset the upvote value to the original value from the server
        alert("Updating the comment upvote count failed! " + error);
    }
}

async function updateBody(commentId, newCommentBody) {
    try {
        await updateCommentBody(commentId, newCommentBody);

        console.log("Comment with id of " + commentId + " updated successfully with new comment body of " + newCommentBody);
        $("#commentBody" + commentId).attr("contenteditable", "false");
        $("#editCommentSaveBtn" + commentId).hide();
    } catch (error) {
        alert("Updating comment body failed! " + error);
    }
}

export const getPreviousCommentBody = () => previousCommentBody;
export default generateComment;