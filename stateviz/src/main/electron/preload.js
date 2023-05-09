const { ipcRenderer, contextBridge } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
    subscribe: (event, handler) => {
        ipcRenderer.on(event, (_, ...args) => handler(...args));
    },
    send: (event, ...args) => {
        ipcRenderer.send(event, ...args);
    },
});
