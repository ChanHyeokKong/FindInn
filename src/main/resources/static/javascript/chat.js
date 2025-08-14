    console.log("chat.js loaded and starting execution."); // Add this line at the very top

document.addEventListener('DOMContentLoaded', function () {
    console.log("DOMContentLoaded fired in chat.js"); // Add this line inside DOMContentLoaded

    const chatButton = document.getElementById('chat-button');
    const sendButton = document.getElementById('send-button');
    const chatInput = document.getElementById('chat-input');
    const chatBox = document.getElementById('chat-box');
    // Removed: const chatModal = new bootstrap.Modal(document.getElementById('chatModal'));

    // Get current user's memberIdx from the hidden input
    const currentMemberIdxElement = document.getElementById('currentMemberIdx');
    console.log("currentMemberIdxElement:", currentMemberIdxElement); // Check if element is found
    if (currentMemberIdxElement) {
        console.log("currentMemberIdxElement.value (string):", currentMemberIdxElement.value);
    }
    const currentMemberId = currentMemberIdxElement ? parseInt(currentMemberIdxElement.value) : null;
    console.log("Current Member ID (parsed):", currentMemberId); // For debugging

    let stompClient = null;
    let currentChatRoomId = null;
    let currentHotelId = null;

    // Function to connect to WebSocket
    function connect(firstMessageToSend = null) { // Accept optional message
        const socket = new SockJS('/ws-chat');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, () => onConnected(firstMessageToSend), onError);
    }

    function onConnected(firstMessageToSend = null) { // Accept optional message
        console.log('Connected to WebSocket');
        if (currentChatRoomId) {
            stompClient.subscribe('/topic/chatRoom/' + currentChatRoomId, onMessageReceived);
            console.log('Subscribed to /topic/chatRoom/' + currentChatRoomId);

            // If there's a message to send immediately after connection (i.e., the first message)
            if (firstMessageToSend) {
                const chatMessage = {
                    chatRoomIdx: currentChatRoomId,
                    senderIdx: currentMemberId,
                    message: firstMessageToSend
                };
                stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
                // Message is already displayed by displayMessage in sendFirstMessage
            }
        }
    }

    function onError(error) {
        console.error('Could not connect to WebSocket server. Error: ' + error);
    }

    // Function to send message
    function sendMessage() {
        const messageContent = chatInput.value.trim();
        if (messageContent === '') {
            return;
        }

        if (currentChatRoomId === null) {
            sendFirstMessage(messageContent);
        } else {
            if (stompClient && stompClient.connected) {
                const chatMessage = {
                    chatRoomIdx: currentChatRoomId,
                    senderIdx: currentMemberId,
                    message: messageContent
                };
                stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
                // REMOVED: displayMessage(chatMessage, 'sent'); // Remove this line
                chatInput.value = '';
                chatBox.scrollTop = chatBox.scrollHeight;
            } else {
                console.error("WebSocket not connected for subsequent messages.");
                alert("채팅 연결이 불안정합니다. 잠시 후 다시 시도해주세요.");
            }
        }
    }

    // Function to send the first message and create chat room
    function sendFirstMessage(messageContent) {
        const chatMessageData = {
            hotelIdx: currentHotelId,
            message: messageContent
        };

        fetch('/chat/message', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(chatMessageData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(chatRoomIdx => {
            currentChatRoomId = chatRoomIdx;
            console.log('Chat room created with ID:', currentChatRoomId);
            
            const firstMessageToSend = messageContent; 

            connect(firstMessageToSend);
            
            // REMOVED: displayMessage({ message: firstMessageToSend }, 'sent'); // Remove this line
            chatInput.value = '';
            chatBox.scrollTop = chatBox.scrollHeight;
        })
        .catch(error => {
            console.error('Error creating chat room or sending first message:', error);
            alert('채팅방 생성 및 메시지 전송에 실패했습니다.');
        });
    }

    // Function to display messages in the chat box
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
        displayMessage(message, message.senderIdx === currentMemberId ? 'sent' : 'received'); // Use actual member ID
    }

    // Event listener for "문의하기" button
    chatButton.addEventListener('click', function () {
        currentHotelId = this.dataset.hotelIdx;
        console.log('Chat button clicked for hotel:', currentHotelId);

        fetch(`/chat/room/${currentHotelId}`)
            .then(response => response.json())
            .then(chatRoomId => {
                if (chatRoomId !== -1) {
                    currentChatRoomId = chatRoomId;
                    console.log('Existing chat room found:', currentChatRoomId);
                    fetch(`/chat/history/${currentChatRoomId}`)
                        .then(response => response.json())
                        .then(history => {
                            chatBox.innerHTML = '';
                            history.forEach(msg => {
                                displayMessage(msg, msg.senderIdx === currentMemberId ? 'sent' : 'received');
                            });
                            connect();
                        })
                        .catch(error => console.error('Error loading chat history:', error));
                } else {
                    currentChatRoomId = null;
                    console.log('No existing chat room. Will create on first message.');
                    chatBox.innerHTML = '';
                    displayMessage({ message: '안녕하세요! 궁금한 점이 있으시면 무엇이든 물어보세요.' }, 'received');
                }
                // Removed: chatModal.show(); // Let Bootstrap's data-bs-toggle handle this
            })
            .catch(error => {
                console.error('Error checking for existing chat room:', error);
                alert('채팅방 정보를 불러오는데 실패했습니다.');
            });
    });

    // Event listeners for sending messages
    sendButton.addEventListener('click', sendMessage);
    chatInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    // Clear chat input and messages when modal is hidden
    document.getElementById('chatModal').addEventListener('hidden.bs.modal', function () {
        chatInput.value = '';
        chatBox.innerHTML = '';
        if (stompClient && stompClient.connected) {
            stompClient.disconnect();
            console.log("Disconnected from WebSocket.");
        }
        currentChatRoomId = null;
        currentHotelId = null;
    });
});