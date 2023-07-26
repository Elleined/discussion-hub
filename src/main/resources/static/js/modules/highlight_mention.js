const highlightMention = (body, mentionedUsers, bodyContainer) => {
   if (mentionedUsers.length === 0) return;
   const highlightedBody =  body.replace(/@(\w+)/g, '<span class="fst-italic fw-bold text-decoration-underline text-primary">@$1</span>');
   bodyContainer.html(highlightedBody);
};
export default highlightMention;