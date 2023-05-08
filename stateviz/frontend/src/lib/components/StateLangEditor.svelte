<script lang="ts">
	import type * as monaco from 'monaco-editor';
	import {
		registerStateLangCompletionProvider,
		statelangId,
		type Symbol as StateLangSymbol,
	} from '../statelang';
	import MonacoEditor from './MonacoEditor.svelte';

	let className = '';
	export { className as class };
	export let style = '';

	export let value = '';

	export let readOnly = false;

	export let markers: monaco.editor.IMarkerData[] = [];

	export let decorations: monaco.editor.IModelDeltaDecoration[] = [];

	export let symbols: StateLangSymbol[] = [];

	const options: monaco.editor.IStandaloneEditorConstructionOptions = {
		language: statelangId,
		automaticLayout: true,
		minimap: { enabled: false },
		theme: 'vs-dark',
	};

	let completionProvider: monaco.IDisposable | null = null;

	$: {
		completionProvider?.dispose();
		completionProvider = registerStateLangCompletionProvider(symbols);
	}
</script>

<MonacoEditor class={className} {options} {readOnly} {markers} {decorations} {style} bind:value />
