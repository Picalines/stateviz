<script lang="ts">
	import { onMount } from 'svelte';
	import * as vis from 'vis-network/standalone';
	import type { StateMachine } from '../statelang';

	let className = '';
	export { className as class };
	export let style = '';

	export let stateMachine: StateMachine;

	export let currentState: string | null;

	let container: HTMLDivElement | null = null;
	let network: vis.Network | null = null;

	const margin = { top: 10, right: 10, bottom: 10, left: 10 };

	const nodeFont = { multi: 'html' };

	const nodes = new vis.DataSet<{
		id: string;
		label: string;
		margin: { top: number; right: number; bottom: number; left: number };
		borderWidth: number;
		font: typeof nodeFont;
	}>();

	const edges = new vis.DataSet<{
		id: undefined;
		from: string;
		to: string;
		arrows: string;
	}>();

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
				},
			},
		);
	});

	$: if (network) {
		const states = new Set(stateMachine.states);

		for (const state of states) {
			const isCurrentState = state === currentState;

			nodes.update(
				{
					id: state,
					label: isCurrentState ? `<b>${state}</b>` : state,
					margin,
					font: nodeFont,
					borderWidth: isCurrentState ? 4 : 2,
				},
				state,
			);
		}

		for (const id of nodes.getIds()) {
			if (!states.has(id as string)) {
				nodes.remove(id);
			}
		}

		edges.clear();

		Object.entries(stateMachine.transitions)
			.map(([from, tos]) => tos.map(to => ({ id: undefined, from, to, arrows: 'to' })))
			.flat()
			.forEach(edge => edges.add(edge));
	}
</script>

<div class={className} {style} bind:this={container} />
