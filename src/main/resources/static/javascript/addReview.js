
/*<![CDATA[*/

// NEW: Function to handle removing an already saved image
function removeExistingImage(button) {
const imagePath = button.getAttribute('data-path');
if (!imagePath) return;

// Create a hidden input to send the path of the deleted image to the server
const form = document.getElementById('review-form');
const hiddenInput = document.createElement('input');
hiddenInput.type = 'hidden';
hiddenInput.name = 'deletedImages'; // Controller will use this name
hiddenInput.value = imagePath;
form.appendChild(hiddenInput);

// Remove the preview item from the view
button.parentElement.remove();
}


document.addEventListener('DOMContentLoaded', function () {
const fileInput = document.getElementById('file-input');
const previewContainer = document.getElementById('image-preview-container');

fileInput.addEventListener('change', function () {
// NOTE: We no longer clear the whole container, in case there are existing images.
// Instead, we will just remove previews for NEW files if re-selected.
// Find and remove only the previews for newly selected files.
document.querySelectorAll('.new-image-preview').forEach(el => el.remove());

const dataTransfer = new DataTransfer();

Array.from(this.files).forEach(file => {
if (file.type.startsWith('image/')) {
dataTransfer.items.add(file);

const reader = new FileReader();
reader.onload = function (e) {
const previewItem = document.createElement('div');
// Add a class to distinguish new previews from existing ones
previewItem.classList.add('image-preview-item', 'new-image-preview');

const img = document.createElement('img');
img.src = e.target.result;
img.style.width = '100px';
img.style.height = '100px';
img.style.objectFit = 'cover';
img.classList.add('rounded');

const closeButton = document.createElement('button');
closeButton.type = 'button';
closeButton.classList.add('btn-close');

// The logic for removing NEWLY added files remains the same
closeButton.addEventListener('click', function() {
for (let i = 0; i < dataTransfer.files.length; i++) {
if (dataTransfer.files[i].name === file.name) {
dataTransfer.items.remove(i);
break;
}
}
fileInput.files = dataTransfer.files;
previewItem.remove();
});

previewItem.appendChild(img);
previewItem.appendChild(closeButton);
previewContainer.appendChild(previewItem);
};
reader.readAsDataURL(file);
}
});
this.files = dataTransfer.files;
});
});


document.addEventListener('DOMContentLoaded', function() {
const reviewForm = document.getElementById('review-form');
reviewForm.addEventListener('submit', function(event) {

event.preventDefault();
const formData = new FormData(reviewForm);

// 5. Send the data to the server using the Fetch API
fetch('/reviews/save', {
method: 'POST',
body: formData,
})
.then(response => {
if (response.ok) {
return response.text(); // Or response.json()
} else {
return response.text().then(errorMessage => {
throw new Error(errorMessage);
});
}
})
.then(successMessage => {
alert('리뷰가 작성되었습니다!');
window.close();
})
.catch(error => {
console.error('Submission failed:', error);
alert(error.message);
});
});
});
