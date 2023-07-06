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
}

export {
    saveTracker,
    savePost,
    saveComment,
    saveReply
};