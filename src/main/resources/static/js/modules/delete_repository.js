const deletePost = href => {
    $.ajax({
        type: "DELETE",
        url: "/forum/api" + href,
        success: function(response) {

            window.location.href = "/forum";
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Deletion of post failed!" + xhr.responseText);
        }
    });
};

const deleteComment = commentId => {
    $.ajax({
        type: "DELETE",
        url: `/forum/api/posts/0/comments/${commentId}`,
        success: function(response) {
            console.log("Comment deleted successfully");
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Deletion of comment failed!" + xhr.responseText);
        }
    });
};

const deleteReply = deleteReplyURI => {
    $.ajax({
        type: "DELETE",
        url: deleteReplyURI,
        success: function(response) {
            console.log("Reply deleted successfully");
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Deletion of reply failed!" + xhr.responseText);
        }
    });
};

const deleteTracker = (userId, type) => {
    $.ajax({
        type: "DELETE",
        url: "/forum/api/users/" + userId + "/deleteTracker",
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
};

export {
    deletePost,
    deleteComment,
    deleteReply,
    deleteTracker
};