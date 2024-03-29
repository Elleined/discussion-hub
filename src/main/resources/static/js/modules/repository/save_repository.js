const saveTracker = (userId, associatedTypeId, type) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "POST",
        url: `/forum/api/users/${userId}/saveTracker`,
        async: false,
        data: {
            associatedTypeId: associatedTypeId,
            type: type
        },
        success: function(response) {
            deferred.resolve(response);
            console.log("Saving the modal tracker for user with id of " + userId + " and associated id of " + associatedTypeId + " successful!");
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
}

const savePost = (body, attachedPicture, mentionedUserIds) => {
    const dataArray = Array.from(mentionedUserIds);
    return $.ajax({
        type: "POST",
        url: "/forum/api/posts",
        data: {
            body: body,
            attachedPicture: attachedPicture,
            mentionedUserIds: dataArray.join(",")
        }
    }).promise();
}

const saveComment = (body, postId, attachedPicture, mentionedUserIds) => {
    const dataArray = Array.from(mentionedUserIds);
    return $.ajax({
        type: "POST",
        url:  `/forum/api/posts/${postId}/comments`,
        data: {
            body: body,
            attachedPicture: attachedPicture,
            mentionedUserIds: dataArray.join(",")
        }
    }).promise();
};

const saveReply = (body, commentId, attachedPicture, mentionedUserIds) => {
    const dataArray = Array.from(mentionedUserIds);
    return $.ajax({
        type: "POST",
        url: `/forum/api/posts/comments/${commentId}/replies`,
        data: {
            body: body,
            attachedPicture: attachedPicture,
            mentionedUserIds: dataArray.join(",")
        }
    }).promise();
};

const likePost = (postId, currentUserId) => {
    return $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/${postId}/like/${currentUserId}`
    }).promise();
};

const likeComment = (commentId, currentUserId) => {
    return $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/0/comments/${commentId}/like/${currentUserId}`
    }).promise();
};

const likeReply = (replyId, currentUserId) => {
    return $.ajax({
        type: "PATCH",
        url: `/forum/api/posts/comments/0/replies/${replyId}/like/${currentUserId}`
    }).promise();
};

const blockUser = href => {
    $.ajax({
        type: "PATCH",
        url: href
    })
    .promise()
        .then(res => location.reload())
        .catch(error => alert("Blocking this user failed!" + error));
};

const unblockUser = href => {
    $.ajax({
        type: "PATCH",
        url: href
    })
    .promise()
    .then(res => location.reload())
    .catch(error => alert("Unblocking this user failed!" + error));
}

export {
    saveTracker,
    savePost,
    saveComment,
    saveReply,
    blockUser,
    unblockUser,
    likePost,
    likeComment,
    likeReply
};