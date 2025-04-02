// JSON 语法高亮
function syntaxHighlight(json) {
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'json-number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'json-key';
            } else {
                cls = 'json-string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'json-boolean';
        } else if (/null/.test(match)) {
            cls = 'json-null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}

// 复制 JSON 内容
function copyJsonContent(selector) {
    const jsonText = $(selector).text();
    navigator.clipboard.writeText(jsonText).then(() => {
         Swal.fire({
             icon: 'success',
             title: window.currentLanguage === 'en' ? 'Copied!' : '已复制！',
             timer: 1500,
             showConfirmButton: false
         });
    });
} 