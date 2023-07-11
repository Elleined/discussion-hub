import { getNotificationBlock, getMentionBlock } from '../repository/get_repository.js';

const generateNotification = (notificationResponse, container) => {
    getNotificationBlock(notificationResponse)
        .then(res => container.append(res))
        .catch(error => alert("Generating notification block failed! " + error));
};

const generateMention = (notificationResponse, container) => {
    getMentionBlock(notificationResponse)
        .then(res => container.append(res))
        .catch(error => alert("Generating mention notification block failed" + error.responseText));
};

const updateNotification = (respondentId, id, type) => {
    if (type === "REPLY") {
        const messageCount = $("#messageReplyCount_" + respondentId + "_" + id);
        const newMessageCount = parseInt(messageCount.attr("aria-valuetext")) + 1;

        messageCount.text(newMessageCount + "+");
        messageCount.attr("aria-valuetext", newMessageCount);
        return
    }

    const messageCount = $("#messageCommentCount_" + respondentId + "_" + id);
    const newMessageCount = parseInt(messageCount.attr("aria-valuetext")) + 1;

    messageCount.text(newMessageCount + "+");
    messageCount.attr("aria-valuetext", newMessageCount);
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