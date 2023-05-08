<script lang="ts">
	import type * as monaco from 'monaco-editor';
	import StateGraph from './lib/components/StateGraph.svelte';
	import StateLangEditor from './lib/components/StateLangEditor.svelte';
	import {
		compileSource,
		reportToMarkerData,
		type StateMachine,
		type Symbol as StateLangSymbol,
	} from './lib/statelang';
	import { debounce, dedent } from './lib/utils';

	let program = dedent(`
		state {
			COUNTING,
			STOPPED,
		}

		const stop := 10;
		let count := 0;

		when COUNTING {
			assert count < stop;

			count := count + 1;

			if count = stop {
				state := STOPPED;
			}
		}
		`);

	let stateMachine: StateMachine | null = null;

	let markers: monaco.editor.IMarkerData[] = [];
	let symbols: StateLangSymbol[] = [];

	const recompile = debounce(async () => {
		(await compileSource({ descriptor: 'input', text: program })).ifSome(result => {
			if (result.program) {
				stateMachine = result.program?.stateMachine ?? null;
				symbols = Object.values(result.program?.symbols ?? {});
			}
			markers = result.reports.map(reportToMarkerData);
		});
	}, 500);

	$: program, (markers = []), recompile();
</script>

<main>
	<StateLangEditor {markers} {symbols} style="flex: 1; width: 50%" bind:value={program} />
	{#if stateMachine}
		<StateGraph {stateMachine} style="flex: 1" />
	{:else}
		<div style:flex="1" />
	{/if}
</main>

<style>
	main {
		display: flex;
		flex-direction: row;
		height: 100vh;
	}
</style>
