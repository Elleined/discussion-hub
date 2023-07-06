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
    const deferred = $.Deferred();
    $.ajax({
        type: "POST",
        url: "/forum/api/posts",
        data: {
            body: body,
            mentionedUserIds: dataArray.join(",")
        },
        success: function(response) {
            deferred.resolve(response);
             window.location.href = "/forum";
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
}

const saveComment = (body, commentURI, mentionedUserIds) => {
    const dataArray = Array.from(mentionedUserIds);

    const deferred = $.Deferred();
    $.ajax({
        type: "POST",
        url: "/forum/api" + commentURI,
        data: {
            body: body,
            mentionedUserIds: dataArray.join(",")
        },
        success: function(response) {
            deferred.resolve(response);
            console.log("Comment saved successfully!");
            console.table(response);
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
              deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const saveReply = (body, replyURI, mentionedUserIds) => {
    const dataArray = Array.from(mentionedUserIds);

    const deferred = $.Deferred();
    $.ajax({
        type: "POST",
        url: "/forum/api" + replyURI,
        data: {
            body: body,
            mentionedUserIds: dataArray.join(",")
        },
        success: function(response) {
            deferred.resolve(response);
            console.log("Reply saved sucessfully!");
            console.table(response);
        },
        error: function(xhr, status, error) {
            alert(xhr.responseText);
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
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

export {
    saveTracker,
    savePost,
    saveComment,
    saveReply,
    blockUser
};