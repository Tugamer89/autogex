document.addEventListener("DOMContentLoaded", () => {
    const currentPath = window.location.pathname;
    
    const getBase = (path) => {
        if (path === '/dev' || path.startsWith('/dev/')) return '/dev/';
        const match = path.match(/^\/([0-9]+\.[0-9]+\.[0-9]+)(?:\/|$)/);
        if (match) return `/${match[1]}/`;
        return '/';
    };
    
    const currentBase = getBase(currentPath);
    document.body.addEventListener('change', (e) => {
        if (e.target && e.target.id === 'version-switcher') {
            const targetBase = e.target.value;
            
            let remainder = currentPath.substring(currentBase.length);
            if (currentBase === '/' && currentPath.startsWith('/')) {
                remainder = currentPath.substring(1);
            }
            
            let newUrl = (targetBase + remainder).replace(/\/\//g, '/');
            window.location.href = newUrl;
        }
    });
    
    const updateSelects = (optionsHtml) => {
        document.querySelectorAll('select#version-switcher').forEach(switcher => {
            switcher.removeAttribute('onchange'); 
            switcher.innerHTML = optionsHtml;
            switcher.value = currentBase;
        });
    };

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
            
            updateSelects(options);
        })
        .catch(err => {
            let options = '<option value="/">Latest Stable</option><option value="/dev/">Development (main)</option>';
            const match = currentPath.match(/^\/([0-9]+\.[0-9]+\.[0-9]+)(?:\/|$)/);
            if (match && currentBase !== '/dev/' && currentBase !== '/') {
                options += `<option value="${currentBase}">Version ${match[1]}</option>`;
            }
            updateSelects(options);
        });
});