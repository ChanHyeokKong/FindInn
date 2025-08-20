document.addEventListener('DOMContentLoaded', function () {
    const chatModal = document.getElementById('chatModal');
    const chatModalLabel = document.getElementById('chatModalLabel');
    const chatBox = document.getElementById('chatBox');
    const chatInput = document.getElementById('chatInput');
    const sendButton = document.getElementById('sendButton');

    let stompClient = null;
    let currentChatRoomId = null;

    // The global `currentMemberIdx` is expected to be defined in a <script> tag in the HTML before this script runs.

    // Function to connect to WebSocket
    function connectChat() {
        const socket = new SockJS('/ws-chat');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onError);
    }

    function onConnected() {
        console.log('Chat: Connected to WebSocket');
        if (currentChatRoomId) {
            stompClient.subscribe('/topic/chatRoom/' + currentChatRoomId, onMessageReceived);
            console.log('Chat: Subscribed to /topic/chatRoom/' + currentChatRoomId);
        }
    }

    function onError(error) {
        console.error('Chat: Could not connect to WebSocket server. Error: ' + error);
    }

    // Function to send a message
    function sendMessage() {
        const messageContent = chatInput.value.trim();
        if (messageContent === '' || !stompClient || !stompClient.connected || !currentChatRoomId) {
            console.error("Chat: Cannot send message. WebSocket not connected or chat room not selected.");
            return;
        }

        const chatMessage = {
            chatRoomIdx: currentChatRoomId,
            senderIdx: currentMemberIdx, // User's own memberIdx from global variable
            message: messageContent
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        chatInput.value = '';
    }

    // Function to display a message in the chat box
    function displayMessage(message, type) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', type);

        const paragraph = document.createElement('p');
        paragraph.textContent = message.message;

        const timestamp = document.createElement('span');
        timestamp.classList.add('timestamp');
        const date = message.sendTime ? new Date(message.sendTime) : new Date();
        timestamp.textContent = date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });

        messageElement.appendChild(paragraph);
        messageElement.appendChild(timestamp);
        chatBox.appendChild(messageElement);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    // Function to handle incoming WebSocket messages
    function onMessageReceived(payload) {
        const message = JSON.parse(payload.body);
        // Determine if the message is from the current user or the other party
        displayMessage(message, message.senderIdx === currentMemberIdx ? 'sent' : 'received');
    }

    // Event listener for "채팅 보기" buttons
    document.querySelectorAll('.view-chat-btn').forEach(button => {
        button.addEventListener('click', function () {
            currentChatRoomId = this.dataset.chatRoomIdx;
            const hotelName = this.dataset.hotelName;

            // Update modal title as per request
            chatModalLabel.textContent = `${hotelName}에 문의하기`;

            // Fetch chat history for the selected chat room
            fetch(`/chat/history/${currentChatRoomId}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(history => {
                    chatBox.innerHTML = ''; // Clear previous messages
                    history.forEach(msg => {
                        // `currentMemberIdx` is the global variable for the logged-in user
                        displayMessage(msg, msg.senderIdx === currentMemberIdx ? 'sent' : 'received');
                    });
                    connectChat(); // Connect to WebSocket for this chat room

                    // Scroll to bottom after messages are loaded
                    chatBox.scrollTop = chatBox.scrollHeight;
                })
                .catch(error => console.error('Chat: Error loading chat history:', error));
        });
    });

    // Scroll to bottom when modal is shown
    chatModal.addEventListener('shown.bs.modal', function () {
        chatBox.scrollTop = chatBox.scrollHeight;
    });

    // Event listeners for sending messages
    sendButton.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    // Clear chat and disconnect when modal is hidden
    chatModal.addEventListener('hidden.bs.modal', function () {
        chatInput.value = '';
        chatBox.innerHTML = '';
        if (stompClient && stompClient.connected) {
            stompClient.disconnect(() => {
                 console.log("Chat: Disconnected from WebSocket.");
            });
        }
        currentChatRoomId = null;
    });
});
