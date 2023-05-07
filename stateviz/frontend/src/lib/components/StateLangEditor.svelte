<script lang="ts">
	import * as monaco from 'monaco-editor';
	import { statelangId, compileSource, humanReportMessage, type Report } from '../statelang';
	import { debounce } from '../utils';
	import MonacoEditor from './MonacoEditor.svelte';

	export let value = '';

	const options: monaco.editor.IStandaloneEditorConstructionOptions = {
		language: statelangId,
		automaticLayout: true,
		minimap: { enabled: false },
	};

	let markers: monaco.editor.IMarkerData[] = [];

	const MARKER_SEVERITY_MAP: Record<Report['severity'], monaco.MarkerSeverity> = {
		INFO: monaco.MarkerSeverity.Info,
		WARNING: monaco.MarkerSeverity.Warning,
		ERROR: monaco.MarkerSeverity.Error,
	};

	const recompile = debounce(async () => {
		const result = (await compileSource({ descriptor: 'input', text: value })).orElse(null);
		if (!result) {
			return;
		}

		markers = result.reports.map(report => ({
			startLineNumber: report.selection.start.line,
			startColumn: report.selection.start.column,
			endLineNumber: report.selection.end.line,
			endColumn: report.selection.end.column + 1,

			severity: MARKER_SEVERITY_MAP[report.severity],

			message: humanReportMessage(report),
		}));
	}, 500);

	$: value, (markers = []), recompile();
</script>

<MonacoEditor {options} {markers} style="height: 100vh" bind:value />
