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
                $("#notificationReplyItem_" + notificationResponse.respondentId + "_" + notificationResponse.commentId).remove();
                container.append(res);
                bindReplyButton(notificationResponse);
            }).catch(error => alert("Generating reply notification block failed! " + error));
    } else {
        getNotificationBlock(notificationResponse)
            .then(res => {
                $("#notificationCommentItem_" + notificationResponse.respondentId + "_" + notificationResponse.postId).remove();
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
    $("#commentNotificationButton_" + notificationResponse.respondentId + "_" + notificationResponse.postId).on("click", function(event) {
        bindCommentBtn(notificationResponse.id);
        $(this).parent().parent().parent().remove();
        event.preventDefault();
    });
};

const bindReplyButton = notificationResponse => {
    $("#replyNotificationButton_" + notificationResponse.respondentId + "_" + notificationResponse.commentId).on("click", function(event) {
        bindReplyBtn(notificationResponse.id, notificationResponse.postId);
        $(this).parent().parent().parent().remove();
        event.preventDefault();
    });
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
    updateTotalNotificationCount,
    generateAllNotification
};