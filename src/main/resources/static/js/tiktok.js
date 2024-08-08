document.addEventListener('DOMContentLoaded', function () {
    var socket = new SockJS('/ws');
    var stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/gifts', function (message) {
            var giftList = document.getElementById('giftList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.giftSender || 'Unknown'}</strong>: ${messageData.giftMessage || 'No message'}`;
            giftList.appendChild(item);
            // Скролира до дъното
            giftList.scrollTop = giftList.scrollHeight;
        });

        stompClient.subscribe('/topic/roomInfo', function (message) {
            var roomInfo = JSON.parse(message.body);
            document.getElementById('roomId').textContent = roomInfo.roomId || '-';
            document.getElementById('likesCount').textContent = roomInfo.likes || 0;
            document.getElementById('viewersCount').textContent = roomInfo.viewers || 0;
        });

        stompClient.subscribe('/topic/join', function (message) {
            var joinList = document.getElementById('joinList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.username || 'Unknown'}</strong> joined the room`;
            joinList.appendChild(item);
            // Скролира до дъното
            joinList.scrollTop = joinList.scrollHeight;
        });

        stompClient.subscribe('/topic/status', function (message) {
            var statusList = document.getElementById('statusList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.statusSender || 'Unknown'}</strong>: ${messageData.statusMessage || 'No message'}`;
            statusList.appendChild(item);
            // Скролира до дъното
            statusList.scrollTop = statusList.scrollHeight;
        });

        stompClient.subscribe('/topic/error', function (message) {
            var errorList = document.getElementById('errorList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>Error:</strong> ${messageData.errorMessage || 'No message'}`;
            item.style.color = 'red';
            errorList.appendChild(item);
            // Скролира до дъното
            errorList.scrollTop = errorList.scrollHeight;
        });

        stompClient.subscribe('/topic/comments', function (message) {
            var commentList = document.getElementById('commentList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.commenterName || 'Unknown'}</strong>: ${messageData.commentMessage || 'No message'}`;
            commentList.appendChild(item);
            // Скролира до дъното
            commentList.scrollTop = commentList.scrollHeight;
        });
    });
});
