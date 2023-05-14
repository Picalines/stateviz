'use strict';

const { BrowserWindow, Menu, app, dialog, ipcMain } = require('electron');
const childProcess = require('child_process');
const net = require('net');
const killProcess = require('tree-kill');
const path = require('path');
const fs = require('fs/promises');

app.setName('stateviz');

app.on('ready', async () => {
	let mainWindow = new BrowserWindow({
		backgroundColor: 'lightgray',
		title: 'stateviz',
		show: false,
		icon: path.join(__dirname, 'assets/icons/app.ico'),
		webPreferences: {
			defaultEncoding: 'UTF-8',
			preload: path.join(__dirname, 'preload.js'),
		},
	});

	const serverPort = await getFreePort();

	const jarPath = app.getAppPath() + './stateviz.jar';
	const jarProcess = childProcess.spawn('java', ['-jar', jarPath, `--server.port=${serverPort}`]);

	let serverStarted = false;

	const jarStdoutListener = data => {
		if (String(data).includes(`Tomcat started on port(s): ${serverPort}`)) {
			serverStarted = true;
			jarProcess.stdout.off('data', jarStdoutListener);
		}
	};

	jarProcess.stdout.on('data', jarStdoutListener);

	const serverUrl = `http://localhost:${serverPort}`;

	mainWindow.once('ready-to-show', () => {
		mainWindow.maximize();
		mainWindow.show();
	});

	mainWindow.onbeforeunload = e => {
		e.returnValue = false;
	};

	mainWindow.on('closed', () => {
		killProcess(jarProcess.pid);
		mainWindow = null;
	});

	mainWindow.setMenu(createMenu());

	while (!serverStarted) {
		await delay(250);
	}

	mainWindow.loadURL(serverUrl);

	function newFileMenuItem() {
		mainWindow.webContents.send('newFile');
	}

	async function openFileMenuItem() {
		const { canceled, filePaths } = await dialog.showOpenDialog(mainWindow, {
			properties: ['openFile'],
			filters: [{ name: 'Statelang', extensions: ['sl'] }],
		});
		if (!canceled) {
			const program = await fs.readFile(filePaths[0]);
			mainWindow.webContents.send('fileOpen', program.toString());
		}
	}

	ipcMain.on('saveFile', async (_, ...args) => {
		const [program] = args;
		const { canceled, filePath } = await dialog.showSaveDialog(mainWindow, {
			filters: [{ name: 'Statelang', extensions: ['sl'] }],
		});
		if (!canceled) {
			await fs.writeFile(filePath, program);
		}
	});

	function saveFileMenuItem() {
		mainWindow.webContents.send('saveFileRequest');
	}

	/** @returns {Menu} */
	function createMenu() {
		return Menu.buildFromTemplate([
			{
				label: 'File',
				submenu: [
					{
						label: 'New',
						click: newFileMenuItem,
					},
					{
						label: 'Open',
						click: openFileMenuItem,
					},
					{
						label: 'Save',
						click: saveFileMenuItem,
					},
					{ type: 'separator' },
					{ label: 'Exit', role: 'quit' },
				],
			},
			{
				label: 'Edit',
				submenu: [
					{ role: 'undo' },
					{ role: 'redo' },
					{ type: 'separator' },
					{ role: 'cut' },
					{ role: 'copy' },
					{ role: 'paste' },
				],
			},
			{
				label: 'Debug',
				submenu: [{ role: 'toggleDevTools' }],
			},
		]);
	}
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
