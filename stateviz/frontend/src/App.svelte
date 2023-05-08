<script lang="ts">
	import type * as monaco from 'monaco-editor';
	import StateGraph from './lib/components/StateGraph.svelte';
	import StateLangEditor from './lib/components/StateLangEditor.svelte';
	import {
		compileSource,
		reportToMarkerData,
		type StateMachine,
		type Symbol as StateLangSymbol,
		Interpreter,
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

	let interpreter: Interpreter | null = null;

	let compiling = false;
	let isCompiledSuccessfully = false;

	const recompile = debounce(async () => {
		(await compileSource({ descriptor: 'input', text: program })).ifSome(({ reports, program }) => {
			if (program) {
				stateMachine = program.stateMachine ?? null;
				symbols = Object.values(program.symbols ?? {});
				interpreter = new Interpreter(program);
				isCompiledSuccessfully = true;
			}

			markers = reports.map(reportToMarkerData);
		});
		compiling = false;
	}, 500);

	$: {
		program;
		markers = [];
		isCompiledSuccessfully = false;
		compiling = true;
		recompile();
	}
</script>

<main>
	<StateLangEditor {markers} {symbols} style="flex: 1; width: 50%" bind:value={program} />
	{#if stateMachine}
		<StateGraph {stateMachine} isUpToDate={compiling || isCompiledSuccessfully} style="flex: 1" />
	{/if}
</main>

<style>
	main {
		display: flex;
		flex-direction: row;
		height: 100vh;
	}
</style>
