import { updateCommentUpvote, updateCommentBody } from './repository/update_repository.js';
import { deleteComment } from './repository/delete_repository.js';
import { getCommentBlock } from './repository/get_repository.js';

const generateComment = (commentDto, container) => {
    return new Promise((resolve, reject) => {
        getCommentBlock(commentDto)
            .then(res => {
                container.append(res);
                bindUpvoteAndDownVoteBtn(commentDto.id);
                bindCommentHeaderBtn(commentDto.id);
                resolve(commentDto.id);
            }).catch(error => {
                alert("Generating the comment failed! " + error);
                reject(error);
            });
    });
};

export let previousCommentBody;
export default generateComment;

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
        });
    });
}

function bindUpvoteAndDownVoteBtn(commentId) {
    let isClicked = false;
    $("#upvoteBtn" + commentId).on("click", function(event) {
        event.preventDefault();
        if (isClicked) return;
        let originalUpdateValue = parseInt($("#upvoteValue" + commentId).text());
        const newUpvoteValue = originalUpdateValue + 1;
        updateUpvote(commentId, newUpvoteValue, originalUpdateValue);
        isClicked = true;
    });

    $("#downvoteBtn" + commentId).on("click", function(event) {
        event.preventDefault();
        if (isClicked) return;
        let originalUpdateValue = parseInt($("#upvoteValue" + commentId).text());
        const newUpvoteValue = originalUpdateValue - 1;
        updateUpvote(commentId, newUpvoteValue, originalUpdateValue);
        isClicked = true;
    });
}

async function updateUpvote(commentId, newUpvoteCount, originalUpdateValue) {
    try {
        await updateCommentUpvote(commentId, newUpvoteCount);
        $("#upvoteValue" + commentId).text(newUpvoteCount);
    } catch (error) {
        $("#upvoteValue" + commentId).text(originalUpdateValue); // Reset the upvote value to the original value from the server
        alert("Updating the comment upvote count failed! " + error);
    }
}

async function updateBody(commentId, newCommentBody) {
    try {
        await updateCommentBody(commentId, newCommentBody);

        $("#commentBody" + commentId).attr("contenteditable", "false");
        $("#editCommentSaveBtn" + commentId).hide();
    } catch (error) {
        alert("Updating comment body failed! " + error);
    }
}