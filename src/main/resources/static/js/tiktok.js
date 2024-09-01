document.addEventListener('DOMContentLoaded', function () {
    var socket = new SockJS('/ws');
    var stompClient = Stomp.over(socket);

    var commentCount = 0;
    var joinCount = 0;

    // Променлива за проследяване на автоматичното скролиране
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

    // Зареждане на съхранените данни при зареждане на страницата
    loadData();

    // Обработчик на събития за изпращане на формуляра
    document.getElementById('userForm').addEventListener('submit', function () {
        // Изтрийте съхранените данни от localStorage
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
            // Скролира до дъното, ако авто-скролирането е активно
            if (autoScrollEnabled) {
                giftList.scrollTop = giftList.scrollHeight;
            }
            saveData(); // Запази данните
        });

        stompClient.subscribe('/topic/roomInfo', function (message) {
            var roomInfo = JSON.parse(message.body);
            document.getElementById('roomId').textContent = roomInfo.roomId || '-';
            document.getElementById('likesCount').textContent = roomInfo.likes || 0;
            document.getElementById('viewersCount').textContent = roomInfo.viewers || 0;
            document.getElementById('startTime').textContent = new Date(roomInfo.startTime).toLocaleString() || '-';
            document.getElementById('totalViewers').textContent = roomInfo.totalViewers || 0;

            // Display the ranking information with line breaks
            document.getElementById('ranking').textContent = roomInfo.ranking || '-';
            document.getElementById('ranking').innerHTML = roomInfo.ranking.replace(/\n/g, '<br>');

            document.getElementById('title').textContent = roomInfo.title || '-';

            var pictureElement = document.getElementById('picture');
            if (roomInfo.picture) {
                pictureElement.src = roomInfo.picture;
                pictureElement.style.display = 'block';  // Show the image
            } else {
                pictureElement.style.display = 'none';   // Hide the image
            }
        });



        stompClient.subscribe('/topic/join', function (message) {
            var joinList = document.getElementById('joinList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.username || 'Unknown'}</strong> joined the room`;
            joinList.appendChild(item);

            joinCount++;
            document.getElementById('joinCount').textContent = joinCount;

            // Скролира до дъното, ако авто-скролирането е активно
            if (autoScrollEnabled) {
                joinList.scrollTop = joinList.scrollHeight;
            }
            saveData(); // Запази данните
        });

        stompClient.subscribe('/topic/status', function (message) {
            var statusList = document.getElementById('statusList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.statusSender || 'Unknown'}</strong>: ${messageData.statusMessage || 'No message'}`;
            statusList.appendChild(item);
            // Скролира до дъното, ако авто-скролирането е активно
            if (autoScrollEnabled) {
                statusList.scrollTop = statusList.scrollHeight;
            }
            saveData(); // Запази данните
        });

        stompClient.subscribe('/topic/error', function (message) {
            var errorList = document.getElementById('errorList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>Error:</strong> ${messageData.errorMessage || 'No message'}`;
            item.style.color = 'red';
            errorList.appendChild(item);
            // Скролира до дъното, ако авто-скролирането е активно
            if (autoScrollEnabled) {
                errorList.scrollTop = errorList.scrollHeight;
            }
            saveData(); // Запази данните
        });

        stompClient.subscribe('/topic/comments', function (message) {
            var commentList = document.getElementById('commentList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');

            // Създай img елемент за профилната снимка
            var img = document.createElement('img');
            img.src = messageData.profileImageUrl || 'default-image-url.jpg';
            img.alt = messageData.commenterName || 'Unknown';
            img.style.width = '30px';
            img.style.height = '30px';
            img.style.borderRadius = '50%';
            img.style.marginRight = '10px';

            // Създай span елемент за текста
            var textSpan = document.createElement('span');
            textSpan.innerHTML = `<strong>${messageData.commenterName || 'Unknown'}</strong>: ${messageData.commentMessage || 'No message'}`;

            // Добави img и span към li елемента
            item.appendChild(img);

            // Добави баджовете (ако има такива)
            if (messageData.badges && messageData.badges.length > 0) {
                messageData.badges.forEach(function (badgeUrl) {
                    var badgeImg = document.createElement('img');
                    badgeImg.src = badgeUrl || 'default-badge-url.jpg';  // Заместител, ако няма валидна снимка на баджа
                    badgeImg.alt = 'Badge';
                    badgeImg.style.width = '20px';
                    badgeImg.style.height = '20px';
                    badgeImg.style.marginLeft = '5px';
                    item.appendChild(badgeImg);
                });
            }

            // Ако има атрибути, добави ги към текста
            if (messageData.attributes && messageData.attributes.length > 0) {
                var attributesSpan = document.createElement('span');
                attributesSpan.style.fontStyle = 'italic';  // Примерен стил за атрибутите
                attributesSpan.style.marginLeft = '10px';
                attributesSpan.textContent = `(${messageData.attributes.join(', ')})`;
                item.appendChild(attributesSpan);
            }

            // Добави текста след баджовете и атрибутите
            item.appendChild(textSpan);

            // Добави новия коментар в списъка
            commentList.appendChild(item);

            // Увеличи брояча на коментарите
            commentCount++;
            document.getElementById('commentCount').textContent = commentCount;

            // Скролирай до дъното, ако авто-скролирането е активно
            if (autoScrollEnabled) {
                item.scrollIntoView({ behavior: 'smooth', block: 'end' });
            }
            saveData(); // Запази данните
        });

    });

    // Спиране на автоматичното скролиране при клик върху коментарите
    document.getElementById('commentContainer').addEventListener('click', function () {
        autoScrollEnabled = false;
    });

    // Възобновяване на автоматичното скролиране при скрол до дъното
    document.getElementById('commentContainer').addEventListener('scroll', function () {
        var commentContainer = document.getElementById('commentContainer');
        if (commentContainer.scrollHeight - commentContainer.scrollTop === commentContainer.clientHeight) {
            autoScrollEnabled = true;
        }
    });

    // Възобновяване на автоматичното скролиране при клик върху всяка област извън полето за коментари и други данни
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
});



function clearLocalStorage() {
    // Изтриване на данни от localStorage (локално)
    localStorage.clear();
}

// Добавяне на събитие към бутона за изчистване на данни
document.getElementById('clearStorageBtn').addEventListener('click', clearLocalStorage);