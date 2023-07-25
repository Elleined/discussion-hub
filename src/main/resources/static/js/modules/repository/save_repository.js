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

const savePost = (body, mentionedUserIds) => {
    const dataArray = Array.from(mentionedUserIds);
    return $.ajax({
        type: "POST",
        url: "/forum/api/posts",
        data: {
            body: body,
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

const blockUser = href => {
    $.ajax({
        type: "PATCH",
        url: href,
        success: function(response) {
            console.log("Successfully blocked this user with href of " + href);
            location.reload();
        },
        error: function(xhr, status, error) {
            alert("Error Occurred! Blocking this user failed!" + xhr.responseText);
        }
    });
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
    unblockUser
};