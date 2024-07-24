function sortBySelectedOption() {
    var selectBox = document.getElementById("sort");
    var selectedValue = selectBox.options[selectBox.selectedIndex].value;
    var currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set("sort", selectedValue);
    window.location.replace(currentUrl.toString());
}

function setSelectedOption() {
    var sortParam = new URLSearchParams(window.location.search).get("sort");
    if (sortParam) {
        document.getElementById("sort").value = sortParam;
    }
}

function highlightText(text, searchWords) {
    if (!searchWords) return text;

    const words = searchWords.split(/\s+/);
    let highlightedText = text;

    words.forEach(word => {
        if (word) {
            const regex = new RegExp(`(${word})`, 'gi');
            highlightedText = highlightedText.replace(regex, '<span class="highlight">$1</span>');
        }
    });

    return highlightedText;
}

document.addEventListener("DOMContentLoaded", function() {
    const searchWords = new URLSearchParams(window.location.search).get('words');
    if (searchWords) {
        const comments = document.querySelectorAll('.comment-text');
        comments.forEach(comment => {
            const text = comment.innerHTML;
            comment.innerHTML = highlightText(text, searchWords);
        });
    }
    setSelectedOption();
    document.body.classList.add('loaded1');
});
