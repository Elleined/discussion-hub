import { getNotificationBlock, getMentionBlock } from '../repository/get_repository.js';

const generateNotification = (notificationResponse, container) => {
    return new Promise((resolve, reject) => {
        getNotificationBlock(notificationResponse)
            .then(res => {
                container.append(res);
                resolve(notificationResponse);
            })
            .catch(error => {
                alert("Generating notification block failed! " + error);
                reject(error);
            });
    });
};

const generateMention = (notificationResponse, container) => {
    getMentionBlock(notificationResponse)
        .then(res => container.append(res))
        .catch(error => alert("Generating mention notification block failed" + error.responseText));
};

const updateNotification = (notificationResponse, container) => {
    getNotificationBlock(notificationResponse)
        .then(res => container.replaceWith(res))
        .catch(error => alert("Updating the notification failed! " + error));
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