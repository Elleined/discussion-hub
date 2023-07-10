import { getNotificationBlock } from './get_repository.js';

const generateNotification = (notificationResponse, container) => {
    getNotificationBlock(notificationResponse)
        .then(res => container.append(res))
        .catch(error => alert("Generating notification block failed! " + error));
};

const updateNotification = (respondentId, id, type) => {
    if (type === "REPLY") {
        const messageCount = $("#messageReplyCount_" + respondentId + "_" + id);
        const newMessageCount = parseInt(messageCount.text()) + 1;
        messageCount.text(newMessageCount + "+");
        return
    }
    const messageCount = $("#messageCommentCount_" + respondentId + "_" + id);
    const newMessageCount = parseInt(messageCount.text()) + 1;
    messageCount.text(newMessageCount + "+");
};

const updateTotalNotificationCount = () => {
    const totalNotificationElement = $("#totalNotifCount");
    const newTotalNotificationValue = totalNotificationElement.attr("aria-valuetext") + 1;

    totalNotificationElement.text(newTotalNotificationValue + "+");
    totalNotificationElement.attr("aria-valuetext", newTotalNotificationValue);
};

export default generateNotification;
export {
    updateNotification,
    updateTotalNotificationCount
};