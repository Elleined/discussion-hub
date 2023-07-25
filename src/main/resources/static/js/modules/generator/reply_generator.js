import { getReplyBlock, getReplyLikeIcon } from '../repository/get_repository.js';
import { deleteReply } from '../repository/delete_repository.js';
import { updateReplyBody } from '../repository/update_repository.js';
import { likeReply } from '../repository/save_repository.js';

export let previousReplyBody = null;

const generateReply = (replyDto, container) => {
        getReplyBlock(replyDto)
            .then(res => {
                container.append(res);
                bindReplyHeaderBtn(replyDto.id);
                $("#likeBtn" + replyDto.id).on("click", function(event) {
                    like(replyDto.id, $(this));
                    event.preventDefault();
                });
        }).catch(error => alert("Generating reply failed! " + error.responseText));
};

async function like(replyId, likeBtn) {
    try {
        const currentUserId = $("#currentUserId").val();
        const reply = await likeReply(replyId, currentUserId);
        const likeIcon = await getReplyLikeIcon(reply);
        likeBtn.empty(); // deletes the old like icon
        likeBtn.append(likeIcon); // add the new like icon
        console.log("User with id of " + currentUserId + " like/unlike reply with id of " + replyId);
    } catch(error) {
        alert("Error Occurred! Cannot like/unlike reply" + error);
    }
}

function bindReplyHeaderBtn(replyId) {
   const editReplySaveBtn = $("#editReplySaveBtn" + replyId);
   editReplySaveBtn.hide();

   $("#replyDeleteBtn" + replyId).on("click", function (event) {
      event.preventDefault();
      deleteReply(replyId);
   });

   $("#editReplyBtn" + replyId).on("click", function (event) {
      event.preventDefault();
      const replyBody = $("#replyBody" + replyId);
      previousReplyBody = replyBody.text();

      replyBody.attr("contenteditable", "true");
      replyBody.focus();
      editReplySaveBtn.show();

      // Adding the editReplySaveBtn click listener only when user clicks the editReplyBtn
      editReplySaveBtn.on("click", function() {
         updateReplyBody(replyId, replyBody.text());

         editReplySaveBtn.hide();
         replyBody.attr("contenteditable", "false");
         previousReplyBody = null;
      });
   });
}

export const getPreviousReplyBody = () => previousReplyBody;
export default generateReply;