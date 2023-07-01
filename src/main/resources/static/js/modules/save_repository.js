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
        },
        error: function(xhr, status, error) {
            deferred.reject(error)
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
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
};

const saveReply = (body, replyURI) => {
    const deferred = $.Deferred();
    $.ajax({
        type: "POST",
        url: "/forum/api" + replyURI,
        data: {
            body: body
        },
        success: function(response, status, xhr) {
            deferred.resolve(response);
        },
        error: function(xhr, status, error) {
            deferred.reject(xhr.responseText);
        }
    });
    return deferred.promise();
}

export {
    saveTracker,
    savePost,
    saveComment,
    saveReply
};