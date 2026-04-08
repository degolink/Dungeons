function showToast(message, isSuccess = true, toastId = 'toast') {
    const toast = document.getElementById(toastId);
    if (!toast) {
        return;
    }

    toast.textContent = message;
    toast.classList.remove('bg-red-600', 'bg-green-600');
    toast.classList.add(isSuccess ? 'bg-green-600' : 'bg-red-600');
    toast.classList.add('opacity-100');

    if (toast._hideTimeout) {
        window.clearTimeout(toast._hideTimeout);
    }

    toast._hideTimeout = window.setTimeout(() => {
        toast.classList.remove('opacity-100');
    }, 2200);
}

function prependLogItem(logId, text, itemClass = 'bg-slate-800 border border-slate-700 rounded p-2') {
    const log = document.getElementById(logId);
    if (!log || !text) {
        return;
    }

    const item = document.createElement('li');
    item.className = itemClass;
    item.textContent = text;
    log.prepend(item);
}
