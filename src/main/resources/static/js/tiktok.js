var stompClient;

document.addEventListener('DOMContentLoaded', function () {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    var commentCount = 0;
    var joinCount = 0;
    let autoScrollEnabled = true;

    // Функция за запазване на данни в localStorage
    function saveData() {
        localStorage.setItem('commentList', document.getElementById('commentList').innerHTML);
        localStorage.setItem('joinList', document.getElementById('joinList').innerHTML);
        localStorage.setItem('giftList', document.getElementById('giftList').innerHTML);
        localStorage.setItem('statusList', document.getElementById('statusList').innerHTML);
        localStorage.setItem('errorList', document.getElementById('errorList').innerHTML);
        localStorage.setItem('commentCount', commentCount);
        localStorage.setItem('joinCount', joinCount);
    }

    // Функция за зареждане на данни от localStorage
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

    // Почиства данните при изпращане на форма
    document.getElementById('tiktokUser').addEventListener('submit', function () {
        localStorage.clear();
    });

    // Извлича уникален идентификатор за потребителя от бисквитките
    const userId = getCookie('uniqueUserId');
    if (!userId) {
        console.error('User ID not found in cookies!');
        return;
    }

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // Подписване на каналите за съобщения за конкретния потребител
        stompClient.subscribe(`/user/${userId}/topic/gifts`, function (message) {
            var giftList = document.getElementById('giftList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.user || ''}</strong>: ${messageData.giftMessage || 'No message'}`;
            giftList.appendChild(item);
            if (autoScrollEnabled) giftList.scrollTop = giftList.scrollHeight;
            saveData();
        });

        stompClient.subscribe(`/user/${userId}/topic/roomInfo`, function (message) {
            var roomInfo = JSON.parse(message.body);

            document.getElementById('roomId').textContent = roomInfo.roomId || '-';
            document.getElementById('likesCount').textContent = roomInfo.likes || 0;
            document.getElementById('viewersCount').textContent = roomInfo.viewers || 0;
            document.getElementById('totalViewers').textContent = roomInfo.totalViewers || 0;
            document.getElementById('ranking').innerHTML = roomInfo.ranking ? roomInfo.ranking.replace(/\n/g, '<br>') : '-';
            document.getElementById('title').textContent = roomInfo.title || '-';

            if (!localStorage.getItem('startTime')) {
                localStorage.setItem('startTime', roomInfo.startTime);
            }

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

        // Подписване на канал за нови потребители
        stompClient.subscribe(`/user/${userId}/topic/join`, function (message) {
            var joinList = document.getElementById('joinList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            var timestamp = new Date().toLocaleString();
            item.innerHTML = `<strong>${messageData.username || 'Unknown'}</strong> joined the room <span style="color: gray; font-size: 0.8rem;">(${timestamp})</span>`;
            joinList.appendChild(item);

            joinCount++;
            document.getElementById('joinCount').textContent = joinCount;

            if (autoScrollEnabled) {
                joinList.scrollTop = joinList.scrollHeight;
            }
            saveData();
        });

        // Подписване на канал за статус съобщения
        stompClient.subscribe(`/user/${userId}/topic/status`, function (message) {
            var statusList = document.getElementById('statusList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>System:</strong> ${messageData.statusMessage || 'No message'}`;
            statusList.appendChild(item);
            if (autoScrollEnabled) statusList.scrollTop = statusList.scrollHeight;
            saveData();
        });

        // Подписване на канал за грешки
        stompClient.subscribe(`/user/${userId}/topic/error`, function (message) {
            var errorList = document.getElementById('errorList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>Error:</strong> ${messageData.errorMessage || 'No message'}`;
            item.style.color = 'red';
            errorList.appendChild(item);
            if (autoScrollEnabled) errorList.scrollTop = errorList.scrollHeight;
            saveData();
        });

        // Подписване на канал за коментари
        stompClient.subscribe(`/user/${userId}/topic/comments`, function (message) {
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

            if (autoScrollEnabled) {
                var commentContainer = document.getElementById('commentContainer');
                commentContainer.scrollTop = commentContainer.scrollHeight;
            }

            saveData();
        });
    });

    // 🎯 Scroll behavior toggle
    document.getElementById('commentContainer').addEventListener('click', () => autoScrollEnabled = false);
    document.getElementById('commentContainer').addEventListener('scroll', function () {
        var container = this;
        if (container.scrollHeight - container.scrollTop === container.clientHeight) {
            autoScrollEnabled = true;
        }
    });
    document.addEventListener('click', function (event) {
        var insideAny = ['commentContainer', 'joinContainer', 'giftContainer', 'statusContainer', 'errorContainer']
            .some(id => document.getElementById(id).contains(event.target));
        if (!insideAny) autoScrollEnabled = true;
    });

    // 🧹 Clear Storage
    document.getElementById("clearStorageBtn").addEventListener("click", function () {
        sessionStorage.clear();
        localStorage.clear();
        fetch('/api/clear-data', {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        }).then(() => window.location.reload())
            .catch(error => console.error("Error while clearing data:", error));
    });

    // Функция за извличане на бисквитка
    function getCookie(name) {
        const nameEQ = name + "=";
        const ca = document.cookie.split(';');
        for (let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    }
});
