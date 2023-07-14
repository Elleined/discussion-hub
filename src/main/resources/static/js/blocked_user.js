import { unblockUser } from './modules/repository/save_repository.js';

$(document).ready(function() {

    $(".blockedUserSelector #blockBtn").on("click", function(event) {
        event.preventDefault();
        const href = $(this).attr("href");

        $(".unblockedModalSelector #unblockModalBtn").on("click", function() {
            unblockUser(href);
        });
    });
});