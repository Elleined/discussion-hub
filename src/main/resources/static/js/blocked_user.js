import { unblockUser, blockUser } from './modules/repository/save_repository.js';

$(document).ready(function() {

   $(".blockedUserSelector #blockBtn").on("click", function (event) {
      event.preventDefault();
      const href = $(this).attr("href");

      alert(href);
      $(".blockedModalSelector #blockModalBtn").on("click", function () {
        blockUser(href);
      });
   });

    $(".unblockedUserSelector #unblockBtn").on("click", function(event) {
        event.preventDefault();
        const href = $(this).attr("href");

    alert(href);
        $(".unblockedModalSelector #unblockModalBtn").on("click", function() {
            unblockUser(href);
        });
    });
});