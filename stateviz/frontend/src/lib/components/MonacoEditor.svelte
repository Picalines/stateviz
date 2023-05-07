<script lang="ts">
	import { onMount } from 'svelte';
	import * as monaco from 'monaco-editor';
	import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
	import 'monaco-editor/min/vs/editor/editor.main.css';

	self.MonacoEnvironment = { getWorker: () => new editorWorker() };

	let className = '';
	export { className as class };
	export let style = '';

	export let options: Omit<monaco.editor.IStandaloneEditorConstructionOptions, 'value'> = {};

	export let value = '';

	export let markers: monaco.editor.IMarkerData[] = [];

	let monacoContainer: HTMLElement;

	let editor: ReturnType<typeof monaco.editor.create> | null = null;
	let model: monaco.editor.IModel | null = null;

	onMount(() => {
		editor = monaco.editor.create(monacoContainer, options);
		model = editor.getModel();
		if (!model) {
			return;
		}

		model.setValue(value);
		model.onDidChangeContent(() => (value = model!.getValue()));
	});

	$: if (editor && model && markers) {
		monaco.editor.setModelMarkers(model, 'monaco-editor', markers);
	}
</script>

<div bind:this={monacoContainer} class={className} {style} />

<style>
	div {
		height: 100%;
	}
</style>
