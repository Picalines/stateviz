<script lang="ts">
	import type * as monaco from 'monaco-editor';
	import MonacoEditor from './MonacoEditor.svelte';
	import { statelangId } from './statelang';

    export let value = '';

	const options: monaco.editor.IStandaloneEditorConstructionOptions = {
        value,
		language: statelangId,
		automaticLayout: true,
		minimap: { enabled: false },
	};
</script>

<MonacoEditor {options} style="height: 100vh" />
