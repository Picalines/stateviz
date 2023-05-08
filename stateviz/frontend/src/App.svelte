<script lang="ts">
	import { circOut } from 'svelte/easing';
	import { fly } from 'svelte/transition';
	import * as monaco from 'monaco-editor';
	import { debounce, dedent } from './lib/utils';
	import InterpreterControls from './lib/components/InterpreterControls.svelte';
	import MemoryDisplay from './lib/components/MemoryDisplay.svelte';
	import StateGraph from './lib/components/StateGraph.svelte';
	import StateLangEditor from './lib/components/StateLangEditor.svelte';
	import {
		compileSource,
		reportToMarkerData,
		type StateMachine,
		type Symbol as StateLangSymbol,
		Interpreter,
		type InterpretationInfo,
	} from './lib/statelang';

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

	let interpreterInfo: InterpretationInfo | null = null;
	let interpreterUnsubscribe: (() => void) | null = null;

	let compiling = false;
	let isCompiledSuccessfully = false;

	const recompile = debounce(async () => {
		markers = [];
		isCompiledSuccessfully = false;
		compiling = true;
		interpreter = null;
		interpreterUnsubscribe?.();

		(await compileSource({ descriptor: 'input', text: program })).ifSome(({ reports, program }) => {
			if (program) {
				stateMachine = program.stateMachine ?? null;
				symbols = Object.values(program.symbols ?? {});
				interpreter = new Interpreter(program);
				isCompiledSuccessfully = true;

				interpreterUnsubscribe = interpreter.subscribe(info => (interpreterInfo = info));
			}

			markers = reports.map(reportToMarkerData);
		});

		compiling = false;
	}, 500);

	$: {
		program;
		recompile();
	}

	const currentLineDecorationOptions: monaco.editor.IModelDecorationOptions = {
		isWholeLine: true,
		className: 'currentLineDecoration',
	};

	function createCurrentLineDecoration(info: InterpretationInfo | null) {
		if (!info) {
			return [];
		}

		const { line } = info.location;
		const range = new monaco.Range(line, 1, line, 1);

		return info.started && !info.exited ? [{ range, options: currentLineDecorationOptions }] : [];
	}
</script>

<main style:position="relative">
	<StateLangEditor
		{markers}
		{symbols}
		readOnly={interpreterInfo?.running ?? false}
		style="flex: 1; width: 50%"
		bind:value={program}
		decorations={createCurrentLineDecoration(interpreterInfo)}
	/>
	<div style:flex="1" style:position="relative">
		{#if stateMachine}
			<StateGraph
				{stateMachine}
				currentState={interpreterInfo?.state ?? null}
				style="height: 100%"
			/>
		{/if}
		{#if !compiling && !isCompiledSuccessfully}
			<div id="not-up-to-date-message" in:fly={{ duration: 100, easing: circOut, y: -10 }}>
				State graph is not up-to-date
			</div>
		{/if}
	</div>
	{#if interpreter}
		<InterpreterControls
			{interpreter}
			style="position: absolute; left: 25%; bottom: 1rem; transform: translateX(-50%)"
		/>
	{/if}
	{#if interpreter?.started && interpreterInfo}
		<MemoryDisplay
			memory={interpreterInfo.memory}
			style="position: absolute; bottom: 1rem; left: calc(50% + 1rem)"
		/>
	{/if}
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

	:global(.currentLineDecoration) {
		background: rgb(249, 217, 146, 30%);
	}
</style>
