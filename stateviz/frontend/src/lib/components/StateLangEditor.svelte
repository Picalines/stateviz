<script lang="ts">
	import type * as monaco from 'monaco-editor';
	import { statelangId } from '../statelang';
	import MonacoEditor from './MonacoEditor.svelte';

	let className = '';
	export { className as class };
	export let style = '';

	export let value = '';

	export let markers: monaco.editor.IMarkerData[] = [];

	const options: monaco.editor.IStandaloneEditorConstructionOptions = {
		language: statelangId,
		automaticLayout: true,
		minimap: { enabled: false },
		theme: 'vs-dark',
	};
</script>

<MonacoEditor class={className} {options} {markers} {style} bind:value />
