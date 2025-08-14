document.addEventListener('DOMContentLoaded', function () {
    const managerChatModal = new bootstrap.Modal(document.getElementById('managerChatModal'));
    const managerChatBox = document.getElementById('managerChatBox');
    const managerChatInput = document.getElementById('managerChatInput');
    const managerSendButton = document.getElementById('managerSendButton');
    const modalChatRoomIdSpan = document.getElementById('modalChatRoomId');

    let stompClient = null;
    let currentManagerChatRoomId = null;
    let currentManagerMemberId = null; // This will be the manager's own memberIdx
    let currentCustomerMemberId = null; // This will be the customer's memberIdx for the selected chat
    let currentHotelIdForChat = null; // This will be the hotelIdx for the selected chat

    // Function to connect to WebSocket
    function connectManagerChat() {
        const socket = new SockJS('/ws-chat');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnectedManagerChat, onErrorManagerChat);
    }

    function onConnectedManagerChat() {
        console.log('Manager Chat: Connected to WebSocket');
        if (currentManagerChatRoomId) {
            stompClient.subscribe('/topic/chatRoom/' + currentManagerChatRoomId, onMessageReceivedManagerChat);
            console.log('Manager Chat: Subscribed to /topic/chatRoom/' + currentManagerChatRoomId);
        }
    }

    function onErrorManagerChat(error) {
        console.error('Manager Chat: Could not connect to WebSocket server. Error: ' + error);
    }

    // Function to send message from manager
    function sendManagerMessage() {
        const messageContent = managerChatInput.value.trim();
        if (messageContent === '') {
            return;
        }

        if (stompClient && stompClient.connected && currentManagerChatRoomId) {
            const chatMessage = {
                chatRoomIdx: currentManagerChatRoomId,
                senderIdx: currentManagerMemberId, // Manager's own memberIdx
                message: messageContent
            };
            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
            managerChatInput.value = '';
            managerChatBox.scrollTop = managerChatBox.scrollHeight;
        } else {
            console.error("Manager Chat: WebSocket not connected or chat room not selected.");
            alert("채팅 연결이 불안정하거나 채팅방이 선택되지 않았습니다.");
        }
    }

    // Function to display messages in the manager chat box
    function displayManagerMessage(message, type) {
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
        managerChatBox.appendChild(messageElement);
        managerChatBox.scrollTop = managerChatBox.scrollHeight;
    }

    // Function to handle incoming WebSocket messages for manager
    function onMessageReceivedManagerChat(payload) {
        const message = JSON.parse(payload.body);
        // Determine if the message is from the manager (current user) or the customer
        displayManagerMessage(message, message.senderIdx === currentManagerMemberId ? 'sent' : 'received');
    }

    // Event listener for "채팅 보기" buttons
    document.querySelectorAll('.view-chat-btn').forEach(button => {
        button.addEventListener('click', function () {
            currentManagerChatRoomId = this.dataset.chatRoomIdx;
            currentCustomerMemberId = this.dataset.memberIdx; // Customer's memberIdx
            currentHotelIdForChat = this.dataset.hotelIdx; // HotelIdx for this chat

            modalChatRoomIdSpan.textContent = currentManagerChatRoomId; // Update modal title

            console.log('Manager Chat: Viewing chat room:', currentManagerChatRoomId);
            console.log('Manager Chat: Customer Member ID:', currentCustomerMemberId);
            console.log('Manager Chat: Hotel ID:', currentHotelIdForChat);

            // Fetch chat history for the selected chat room
            fetch(`/chat/history/${currentManagerChatRoomId}`)
                .then(response => response.json())
                .then(history => {
                    managerChatBox.innerHTML = ''; // Clear previous messages
                    history.forEach(msg => {
                        displayManagerMessage(msg, msg.senderIdx === currentManagerMemberId ? 'sent' : 'received');
                    });
                    connectManagerChat(); // Connect to WebSocket for this chat room

                    // Scroll to bottom after messages are loaded
                    managerChatBox.scrollTop = managerChatBox.scrollHeight;
                })
                .catch(error => console.error('Manager Chat: Error loading chat history:', error));
        });
    });

    // Scroll to bottom when modal is shown
    document.getElementById('managerChatModal').addEventListener('shown.bs.modal', function () {
        managerChatBox.scrollTop = managerChatBox.scrollHeight;
    });

    // Event listeners for sending messages from manager
    managerSendButton.addEventListener('click', sendManagerMessage);
    managerChatInput.addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            sendManagerMessage();
        }
    });

    // Clear chat input and messages when modal is hidden
    document.getElementById('managerChatModal').addEventListener('hidden.bs.modal', function () {
        managerChatInput.value = '';
        managerChatBox.innerHTML = '';
        if (stompClient && stompClient.connected) {
            stompClient.disconnect();
            console.log("Manager Chat: Disconnected from WebSocket.");
        }
        currentManagerChatRoomId = null;
        currentCustomerMemberId = null;
        currentHotelIdForChat = null;
    });

    // TODO: Get manager's own memberIdx (currentManagerMemberId)
    // This needs to be passed from the server-side to the managerchat.js
    // Similar to how currentMemberIdx was passed to accommodation-detail.html
    // For now, hardcode for testing if needed, but ideally it comes from the server.
    // Example: currentManagerMemberId = /* value from server */;

    // Hotel filtering logic from qna.html
    const hotelFilter = document.getElementById('hotelFilter');
    const chatRoomTbody = document.getElementById('chatRoomTbody');
    
    if(hotelFilter && chatRoomTbody) { // qna.html 페이지에만 해당 요소들이 있으므로 null 체크
        const tableRows = chatRoomTbody.getElementsByTagName('tr');

        hotelFilter.addEventListener('change', function () {
            const selectedHotelId = this.value;

            for (let i = 0; i < tableRows.length; i++) {
                const row = tableRows[i];
                const hotelId = row.getAttribute('data-hotel-id');

                if (selectedHotelId === 'all' || selectedHotelId === hotelId) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            }
        });
    }
});