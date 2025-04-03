var stompClient;

document.addEventListener('DOMContentLoaded', function () {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    var commentCount = 0;
    var joinCount = 0;
    let autoScrollEnabled = true;

    function saveData() {
        localStorage.setItem('commentList', document.getElementById('commentList').innerHTML);
        localStorage.setItem('joinList', document.getElementById('joinList').innerHTML);
        localStorage.setItem('giftList', document.getElementById('giftList').innerHTML);
        localStorage.setItem('statusList', document.getElementById('statusList').innerHTML);
        localStorage.setItem('errorList', document.getElementById('errorList').innerHTML);
        localStorage.setItem('commentCount', commentCount);
        localStorage.setItem('joinCount', joinCount);
    }

    function loadData() {
        document.getElementById('commentList').innerHTML = localStorage.getItem('commentList') || '';
        document.getElementById('joinList').innerHTML = localStorage.getItem('joinList') || '';
        document.getElementById('giftList').innerHTML = localStorage.getItem('giftList') || '';
        document.getElementById('statusList').innerHTML = localStorage.getItem('statusList') || '';
        document.getElementById('errorList').innerHTML = localStorage.getItem('errorList') || '';
        commentCount = parseInt(localStorage.getItem('commentCount')) || 0;
        joinCount = parseInt(localStorage.getItem('joinCount')) || 0;
        document.getElementById('commentCount').textContent = commentCount;
        document.getElementById('joinCount').textContent = joinCount;
    }

    loadData();

    document.getElementById('tiktokUser').addEventListener('submit', function () {
        localStorage.removeItem('commentList');
        localStorage.removeItem('joinList');
        localStorage.removeItem('giftList');
        localStorage.removeItem('statusList');
        localStorage.removeItem('errorList');
        localStorage.removeItem('commentCount');
        localStorage.removeItem('joinCount');
    });

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/gifts', function (message) {
            var giftList = document.getElementById('giftList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.giftSender || ''}</strong>: ${messageData.giftMessage || 'No message'}`;
            giftList.appendChild(item);
            if (autoScrollEnabled) {
                giftList.scrollTop = giftList.scrollHeight;
            }
            saveData();
        });

        stompClient.subscribe('/topic/roomInfo', function (message) {
            var roomInfo = JSON.parse(message.body);

            document.getElementById('roomId').textContent = roomInfo.roomId || '-';
            document.getElementById('likesCount').textContent = roomInfo.likes || 0;
            document.getElementById('viewersCount').textContent = roomInfo.viewers || 0;
            document.getElementById('totalViewers').textContent = roomInfo.totalViewers || 0;
            document.getElementById('ranking').innerHTML = roomInfo.ranking ? roomInfo.ranking.replace(/\n/g, '<br>') : '-';
            document.getElementById('title').textContent = roomInfo.title || '-';

            // Проверка дали startTime вече е запазено
            if (!localStorage.getItem('startTime')) {
                localStorage.setItem('startTime', roomInfo.startTime);
            }

            // Вземаме запазеното време и го показваме
            var storedStartTime = localStorage.getItem('startTime');
            document.getElementById('startTime').textContent = new Date(storedStartTime).toLocaleString() || '-';

            var pictureElement = document.getElementById('picture');
            if (roomInfo.picture) {
                pictureElement.src = roomInfo.picture;
                pictureElement.style.display = 'block';
            } else {
                pictureElement.style.display = 'none';
            }
        });

        stompClient.subscribe('/topic/join', function (message) {
            var joinList = document.getElementById('joinList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            var timestamp = new Date().toLocaleString();
            item.innerHTML = `<strong>${messageData.username || 'Unknown'}</strong> joined the room <span style="color: gray; font-size: 0.8rem;">(${timestamp})</span>`;
            joinList.appendChild(item);

            joinCount++;
            document.getElementById('joinCount').textContent = joinCount;

            var joinContainer = document.getElementById('joinList');
            if (autoScrollEnabled) {
                joinContainer.scrollTop = joinContainer.scrollHeight;
            }

            saveData();
        });

        stompClient.subscribe('/topic/status', function (message) {
            var statusList = document.getElementById('statusList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.statusSender || 'Unknown'}</strong>: ${messageData.statusMessage || 'No message'}`;
            statusList.appendChild(item);
            if (autoScrollEnabled) {
                statusList.scrollTop = statusList.scrollHeight;
            }
            saveData();
        });

        stompClient.subscribe('/topic/error', function (message) {
            var errorList = document.getElementById('errorList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>Error:</strong> ${messageData.errorMessage || 'No message'}`;
            item.style.color = 'red';
            errorList.appendChild(item);
            if (autoScrollEnabled) {
                errorList.scrollTop = errorList.scrollHeight;
            }
            saveData();
        });

        stompClient.subscribe('/topic/comments', function (message) {
            var commentList = document.getElementById('commentList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');

            var img = document.createElement('img');
            img.src = messageData.profileImageUrl || 'default-image-url.jpg';
            img.alt = messageData.commenterName || 'Unknown';
            img.style.width = '30px';
            img.style.height = '30px';
            img.style.borderRadius = '50%';
            img.style.marginRight = '10px';

            var textSpan = document.createElement('span');
            textSpan.innerHTML = `<strong>${messageData.commenterName || 'Unknown'}</strong>: ${messageData.commentMessage || 'No message'}`;

            item.appendChild(img);
            item.appendChild(textSpan);
            commentList.appendChild(item);

            commentCount++;
            document.getElementById('commentCount').textContent = commentCount;

            var commentContainer = document.getElementById('commentContainer');
            if (autoScrollEnabled) {
                commentContainer.scrollTop = commentContainer.scrollHeight;
            }

            saveData();
        });
    });

    document.getElementById('commentContainer').addEventListener('click', function () {
        autoScrollEnabled = false;
    });

    document.getElementById('commentContainer').addEventListener('scroll', function () {
        var commentContainer = document.getElementById('commentContainer');
        if (commentContainer.scrollHeight - commentContainer.scrollTop === commentContainer.clientHeight) {
            autoScrollEnabled = true;
        }
    });

    document.addEventListener('click', function (event) {
        var clickInsideCommentContainer = document.getElementById('commentContainer').contains(event.target);
        var clickInsideOtherContainers = document.getElementById('joinContainer').contains(event.target) ||
            document.getElementById('giftContainer').contains(event.target) ||
            document.getElementById('statusContainer').contains(event.target) ||
            document.getElementById('errorContainer').contains(event.target);
        if (!clickInsideCommentContainer && !clickInsideOtherContainers) {
            autoScrollEnabled = true;
        }
    });

    document.getElementById("clearStorageBtn").addEventListener("click", function() {
        sessionStorage.clear();
        localStorage.clear();

        fetch('/api/clear-data', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(() => window.location.reload())
            .catch(error => console.error("Error while clearing data:", error));
    });
});
