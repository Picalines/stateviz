<script lang="ts">
	import type * as monaco from 'monaco-editor';
	import MonacoEditor from './MonacoEditor.svelte';

    export let value = '';

	const options: monaco.editor.IStandaloneEditorConstructionOptions = {
        value,
		automaticLayout: true,
		minimap: { enabled: false },
	};
</script>

<MonacoEditor {options} style="height: 100vh" />
