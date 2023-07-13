import { getNotificationBlock, getMentionBlock } from '../repository/get_repository.js';
import { bindReplyBtn, bindCommentBtn } from '../../forum.js';
import { saveTracker } from '../repository/save_repository.js';

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

const generateMention = (notificationResponse, container) => {
    getMentionBlock(notificationResponse)
        .then(res => {
            container.append(res);
            $("#mentionNotification" + notificationResponse.id).on("click", function(event) {
                $(this).parent().parent().parent().remove();
                event.preventDefault();
            });
        }).catch(error => alert("Generating mention notification block failed" + error.responseText));
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

const updateTotalNotificationCount = () => {
    const totalNotificationElement = $("#totalNotifCount");
    const newTotalNotificationValue = parseInt(totalNotificationElement.attr("aria-valuetext")) + 1;

    totalNotificationElement.text(newTotalNotificationValue + "+");
    totalNotificationElement.attr("aria-valuetext", newTotalNotificationValue);
};

export default generateNotification;
export {
    generateMention,
    updateNotification,
    updateTotalNotificationCount
};