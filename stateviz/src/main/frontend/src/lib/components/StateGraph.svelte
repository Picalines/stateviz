<script lang="ts">
	import { onMount } from 'svelte';
	import * as vis from 'vis-network/standalone';
	import type { StateMachine, StateMachineState } from '../statelang';

	let className = '';
	export { className as class };
	export let style = '';

	export let stateMachine: StateMachine;

	export let currentState: string | null;

	let container: HTMLDivElement | null = null;
	let network: vis.Network | null = null;

	const nodes = new vis.DataSet<
		vis.NodeOptions & {
			id: string;
		}
	>();

	const edges = new vis.DataSet<
		vis.EdgeOptions & {
			id: undefined;
		}
	>();

	onMount(() => {
		network = new vis.Network(
			container!,
			{ nodes, edges },
			{
				nodes: {
					shape: 'box',
					color: {
						background: 'white',
						border: 'grey',
					},
					font: {
						multi: 'html',
					},
					margin: { top: 10, right: 10, bottom: 10, left: 10 },
				},
			},
		);
	});

	$: if (network) {
		const states: Map<string, StateMachineState> = stateMachine.states.reduce((map, state) => {
			map.set(state.name, state);
			return map;
		}, new Map());

		for (const state of states.values()) {
			const isCurrentState = state.name === currentState;

			const stateDisplayName = String(
				'label' in state.attributes ? state.attributes.label : state.name,
			);

			nodes.update(
				{
					id: state.name,
					label: isCurrentState ? `<b>${stateDisplayName}</b>` : stateDisplayName,
					borderWidth: isCurrentState ? 4 : 2,
					font: {
						size: Number(state.attributes.fontSize ?? 20)
					}
				},
				state.name,
			);
		}

		for (const stateName of nodes.getIds()) {
			if (!states.has(stateName as string)) {
				nodes.remove(stateName);
			}
		}

		edges.clear();

		Object.entries(stateMachine.transitions)
			.map(([from, tos]) => tos.map(to => ({ id: undefined, from, to, arrows: 'to' })))
			.flat()
			.forEach(edge => edges.add(edge));
	}
</script>

<div class={className} {style}>
	<div id="vis-network" bind:this={container} />
	<i
		id="reset-btn"
		class="bi bi-fullscreen-exit"
		on:click={() => network?.fit()}
		on:keypress={() => network?.fit()}
	/>
</div>

<style>
	#vis-network {
		height: 100%;
	}

	#reset-btn {
		position: absolute;
		bottom: 20px;
		right: 20px;
		z-index: 10;
		color: white;
		background: #2d2d2d;
		--size: 50px;
		width: var(--size);
		height: var(--size);
		text-align: center;
		vertical-align: middle;
		line-height: var(--size);
		border-radius: 100%;
		transition: scale linear 0.05s;
	}

	#reset-btn:hover {
		cursor: pointer;
	}

	#reset-btn:active {
		scale: 90%;
	}
</style>
