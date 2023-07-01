import * as GetRepository from './get_repository.js';

export const mentionedUsersId = new Set();
export function mention(userId, inputField, mentionList) {
    const userInputValue = inputField.val();
    const lastWord = userInputValue.split(" ").pop();

    if (lastWord.startsWith("@")) {
      const username = lastWord.substring(1); // Extract the username without the "@"
      GetRepository.getAllUsernames(userId, username)
        .then(users => {
            console.log("User mentioned: " + username);
            users.forEach(user => {
                generateMentionList(user, mentionList);
                bindGeneratedButton(user.id, username, inputField);
            });
        }).catch(error => console.error(error));
    }
    mentionList.empty();
}

function generateMentionList(user, mentionList) {
    const image = `/img/${user.picture}`;
    const mentionBtnId = `mentionBtn_${user.id}`;
    const nameSpanId = `nameSpan_${user.id}`;

    mentionList.append(
    `
    <div class="grid">
        <button type="button" class="btn btn-info ms-4" id=${mentionBtnId}>
            <img src=${image} height="40px" width="40px" />
            <span class="ms-2" id=${nameSpanId}> ${user.name} </span>
        </button>
    </div>
    <hr>
    `);
}

function bindGeneratedButton(userId, username, inputField) {
    $("#mentionBtn_" + userId).on("click", () => {
        const input = inputField.val().trim().replace(username, "");
        const name = $("#nameSpan_" + userId).text().trim();
        const updatedInput = input + name;

        inputField.val(updatedInput);
        mentionedUsersId.add(userId);
    });
}