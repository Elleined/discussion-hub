let attachedPicture = null;

const uploadPhoto = (uploadBtn, inputField, imagePreview) => {
   uploadBtn.on("click", function () {
      inputField.click();
   });

   inputField.on("change", function () {
      const selectedImage = this.files[0];
      if (selectedImage) {
         imagePreview.removeClass("d-none");
         const reader = new FileReader();
         reader.onload = e => imagePreview.attr("src", e.target.result);
         reader.readAsDataURL(selectedImage);
         attachedPicture = selectedImage.name;
      }
   });
};

export const getAttachedPicture = () => attachedPicture;
export default uploadPhoto;