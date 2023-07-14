import { unblockUser } from './modules/repository/save_repository.js';

$(document).ready(function() {

    $(".blockedUserSelector #blockBtn").on("click", function(event) {
        const href = $(this).attr("href");
        alert(href);
        event.preventDefault();
    });
});