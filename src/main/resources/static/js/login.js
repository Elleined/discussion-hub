import { getAllUsernames as fetchAllUsernames } from './modules/get_repository.js';

let mentionedUsers = new Set();
$(document).ready(function() {
    $("#email").focus();

    $("#mentionName").on("input", function(event) {
    const userInputValue = $(this).val();
    const lastWord = userInputValue.split(" ").pop();

    const mentionList = $("#mentionList");
    if (lastWord.startsWith("@")) {
      // Perform your desired actions when a mention is detected
      const username = lastWord.substring(1); // Extract the username without the "@"
      fetchAllUsernames(username)
        .then(users => {
            console.log("User mentioned: " + username);
            users.forEach(user => {
                generateMentionList(user);
                bindGeneratedButton(user.id, username);
            });
        }).catch(error => console.error(error));
      // You can use the extracted username for further processing
    }
    mentionList.empty();
    });
    // insert here
});

function generateMentionList(user) {
    const mentionList = $("#mentionList");
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
    `); // End of auto-generated html
}

function bindGeneratedButton(userId, username) {
    $("#mentionBtn_" + userId).on("click", () => {
        const inputField = $("#mentionName");
        const input = inputField.val().trim().replace(username, "");
        const name = $("#nameSpan_" + userId).text().trim();
        const updatedInput = input + name;
        inputField.val(updatedInput);
        mentionedUsers.add(userId);
    });
}