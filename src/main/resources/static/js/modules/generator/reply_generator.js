import { getReplyBlock } from '../repository/get_repository.js';
import { deleteReply } from '../repository/delete_repository.js';
import { updateReplyBody } from '../repository/update_repository.js';
import { replyLike } from '../like.js';

export let previousReplyBody = null;

const generateReply = (replyDto, container) => {
        getReplyBlock(replyDto)
            .then(res => {
                container.append(res);
                bindReplyHeaderBtn(replyDto.id);
                $("#likeBtn" + replyDto.id).on("click", function(event) {
                    const currentUserId = $("#currentUserId").val();
                    replyLike(replyDto.id, currentUserId, $(this));
                    event.preventDefault();
                });
        }).catch(error => alert("Generating reply failed! " + error.responseText));
};

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