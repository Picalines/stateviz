'use strict';

const electron = require('electron');
const childProcess = require('child_process');
const os = require('os');
const net = require('net');
const killProcess = require('tree-kill');

const app = electron.app;
app.setName('stateviz');

/** @type {electron.BrowserWindow | null} */
let mainWindow = null;

app.on('ready', async () => {
	mainWindow = new electron.BrowserWindow({
		backgroundColor: 'lightgray',
		title: 'stateviz',
		show: false,
		webPreferences: {
			nodeIntegration: true,
			defaultEncoding: 'UTF-8',
		},
	});

	const freePort = await getFreePort();

	const jarPath = app.getAppPath() + './stateviz.jar';
	const jarProcess = childProcess.spawn('java', ['-jar', jarPath, `--server.port=${freePort}`]);

	const serverUrl = `http://localhost:${freePort}`;

	mainWindow.once('ready-to-show', () => {
		// mainWindow.setMenu(null);
		mainWindow.maximize();
		mainWindow.show();
	});

	mainWindow.onbeforeunload = e => {
		e.returnValue = false;
	};

	mainWindow.on('closed', function () {
		killProcess(jarProcess.pid);
		mainWindow = null;
	});

	await delay(1000);
	mainWindow.loadURL(serverUrl);
});

app.on('window-all-closed', () => {
	app.quit();
});

/** @returns {Promise<number>} */
async function getFreePort() {
	return new Promise(resolve => {
		const server = net.createServer();
		server.listen(0, () => {
			const port = server.address().port;
			server.close(() => resolve(port));
		});
	});
}

/** @param {number} delayMs */
async function delay(delayMs) {
	return new Promise(resolve => setTimeout(resolve, delayMs));
}
