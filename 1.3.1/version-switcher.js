document.addEventListener("DOMContentLoaded", () => {
    const switcher = document.getElementById('version-switcher');
    if (!switcher) return;
    
    const regExp = new RegExp(/^\/(\d+\.\d+\.\d+)\//);
    const currentPath = globalThis.location.pathname;
    
    fetch('https://api.github.com/repos/Tugamer89/autogex/tags')
        .then(res => {
            if (!res.ok) throw new Error("API Limit");
            return res.json();
        })
        .then(tags => {
            let options = '<option value="/">Latest Stable</option>';
            tags.forEach(tag => {
                const v = tag.name.replace('v', '');
                options += `<option value="/${v}/">Version ${v}</option>`;
            });
            options += '<option value="/dev/">Development (main)</option>';
            switcher.innerHTML = options;
            
            const match = regExp.exec(currentPath);
            if (currentPath.startsWith('/dev/')) switcher.value = '/dev/';
            else if (match) switcher.value = `/${match[1]}/`;
            else switcher.value = '/';
        })
        .catch(err => {
            switcher.innerHTML = '<option value="/">Latest Stable</option><option value="/dev/">Development (main)</option>';
            const match = regExp.exec(currentPath);
            if (match) {
                switcher.innerHTML += `<option value="/${match[1]}/">Version ${match[1]}</option>`;
                switcher.value = `/${match[1]}/`;
            } else if (currentPath.startsWith('/dev/')) switcher.value = '/dev/';
            else switcher.value = '/';
        });
});