    document.addEventListener('DOMContentLoaded', function () {
        const sendButton = document.getElementById('send-button');
        const chatInput = document.getElementById('chat-input');
        const chatBox = document.getElementById('chat-box');

        function sendMessage() {
            const messageText = chatInput.value.trim();
            if (messageText === '') return;

            const messageElement = document.createElement('div');
            messageElement.classList.add('message', 'sent');

            const paragraph = document.createElement('p');
            paragraph.textContent = messageText;

            const timestamp = document.createElement('span');
            timestamp.classList.add('timestamp');
            const now = new Date();
            timestamp.textContent = now.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });

            messageElement.appendChild(paragraph);
            messageElement.appendChild(timestamp);
            chatBox.appendChild(messageElement);

            chatInput.value = '';
            chatBox.scrollTop = chatBox.scrollHeight;
        }

        sendButton.addEventListener('click', sendMessage);

        chatInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                sendMessage();
            }
        });
    });