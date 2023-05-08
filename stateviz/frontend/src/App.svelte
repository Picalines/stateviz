<script lang="ts">
	import { circOut } from 'svelte/easing';
	import { fly } from 'svelte/transition';
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
	<div style:flex="1" style:position="relative">
		{#if stateMachine}
			<StateGraph {stateMachine} style="height: 100%" />
		{/if}
		{#if !compiling && !isCompiledSuccessfully}
			<div id="not-up-to-date-message" in:fly={{ duration: 100, easing: circOut, y: -10 }}>
				State graph is not up-to-date
			</div>
		{/if}
	</div>
</main>

<style>
	main {
		display: flex;
		flex-direction: row;
		height: 100vh;
	}

	#not-up-to-date-message {
		position: absolute;
		top: 1em;
		left: 1em;
		color: white;
		background: #ff5555;
		padding: 3px 6px 3px 6px;
		border-radius: 3px;
	}
</style>
