import { svelte } from '@sveltejs/vite-plugin-svelte';
import { defineConfig } from 'vite';

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [svelte()],

	build: {
		emptyOutDir: true,
		outDir: '../src/main/resources/static',
	},
});
