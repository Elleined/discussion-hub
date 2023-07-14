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

const deleteReply = replyId => {
    $.ajax({
        type: "DELETE",
        url: `/forum/api/posts/comments/0/replies/${replyId}`,
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
        url: `/forum/api/users/${userId}/deleteTracker`,
        data: { type: type }
    })
    .promise()
    .then(res => console.log(`Deleting modal tracker for ${userId} with type of ${type} success`))
    .catch(error => alert("Deleting modal tracker failed!" + error));
};

export {
    deletePost,
    deleteComment,
    deleteReply,
    deleteTracker
};