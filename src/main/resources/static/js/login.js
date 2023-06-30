$(document).ready(function() {
     $("#email").focus();
     // insert here
    $("#mentionName").on("input", function(event) {
    const userInputValue = $(this).val();
    const lastWord = userInputValue.split(" ").pop();

    if (lastWord.startsWith("@")) {
      // Perform your desired actions when a mention is detected
      const username = lastWord.substring(1); // Extract the username without the "@"
      console.log("User mentioned: " + username);
      // You can use the extracted username for further processing
    }
    });
});


