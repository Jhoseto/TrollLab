document.addEventListener('DOMContentLoaded', function () {
    var socket = new SockJS('/ws');
    var stompClient = Stomp.over(socket);

    var commentCount = 0;
    var joinCount = 0;

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/gifts', function (message) {
            var giftList = document.getElementById('giftList');
            var messageData = JSON.parse(message.body);
            var item = document.createElement('li');
            item.innerHTML = `<strong>${messageData.giftSender || ''}</strong>: ${messageData.giftMessage || 'No message'}`;
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

            joinCount++;
            document.getElementById('joinCount').textContent = joinCount;

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

            // Скролирай до дъното
            item.scrollIntoView({ behavior: 'smooth', block: 'end' });
        });

    });
});
