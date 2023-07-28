import {
    getNotificationBlock,
    getMentionBlock,
    getAllNotification,
    getTotalNotificationCount
} from '../repository/get_repository.js';
import {
    bindReplyBtn,
    bindCommentBtn
} from '../../forum.js';
import {
    saveTracker
} from '../repository/save_repository.js';

const generateNotification = (notificationResponse, container) => {
    if (notificationResponse.type === "REPLY") {
        getNotificationBlock(notificationResponse)
            .then(res => {
                container.append(res);
                bindReplyButton(notificationResponse);
            }).catch(error => alert("Generating reply notification block failed! " + error));
    } else {
        getNotificationBlock(notificationResponse)
            .then(res => {
                container.append(res);
                bindCommentButton(notificationResponse);
            }).catch(error => alert("Generating comment notification block failed! " + error));
    }
};

const generateAllNotification = (currentUserId, container) => {
    getAllNotification(currentUserId)
        .then(notificationResponses => {
            $.each(notificationResponses, function(index, notificationResponse) {
                generateNotification(notificationResponse, container);
            });
        }).catch(error => alert("Generating all notification failed! " + error));
};

const bindCommentButton = notificationResponse => {
    $("#commentNotificationButton_" + notificationResponse.respondentId + "_" + notificationResponse.id).on("click", function(event) {
        bindCommentBtn(notificationResponse.id);
        $(this).parent().parent().parent().remove();
        event.preventDefault();
    });
};

const bindReplyButton = notificationResponse => {
    $("#replyNotificationButton_" + notificationResponse.respondentId + "_" + notificationResponse.id).on("click", function(event) {
        bindReplyBtn(notificationResponse.id, notificationResponse.postId);
        $(this).parent().parent().parent().remove();
        event.preventDefault();
    });
};

const updateNotification = (notificationResponse, container) => {
    getNotificationBlock(notificationResponse)
        .then(res => {
            if (notificationResponse.type === "REPLY") {
                container.replaceWith(res);
                bindReplyButton(notificationResponse);
                return;
            }
            container.replaceWith(res);
            bindCommentButton(notificationResponse);
        }).catch(error => alert("Updating the notification failed! " + error));
};

const updateTotalNotificationCount = async () => {
    const currentUserId = $("#currentUserId").val();
    try {
        const totalNotificationCount = await getTotalNotificationCount(currentUserId);
        const totalNotificationElement = $("#totalNotificationCount");
        totalNotificationElement.text(`${totalNotificationCount}+`);
    } catch(err) {
        alert("Error Occurred! Getting total notification count failed! " + err);
    }
};

export default generateNotification;
export {
    updateNotification,
    updateTotalNotificationCount,
    generateAllNotification
};