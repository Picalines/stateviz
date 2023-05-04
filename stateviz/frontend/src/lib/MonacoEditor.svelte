<script lang="ts">
	import { onMount } from 'svelte';
	import * as monaco from 'monaco-editor';
	import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
	import 'monaco-editor/min/vs/editor/editor.main.css';

	let monacoContainer: HTMLElement;

	export let options: monaco.editor.IStandaloneEditorConstructionOptions = {};

	let className = '';
	export { className as class };

	export let style = '';

	onMount(() => {
		monaco.editor.create(monacoContainer, options);
	});

	self.MonacoEnvironment = {
		getWorker: () => new editorWorker(),
	};
</script>

<div bind:this={monacoContainer} class={className} {style} />

<style>
	div {
		height: 100%;
	}
</style>
