<script lang="ts">
	import { onMount } from 'svelte';
	import { circOut } from 'svelte/easing';
	import { fly } from 'svelte/transition';
	import * as vis from 'vis-network/standalone';
	import type { StateMachine } from '../statelang';

	let className = '';
	export { className as class };
	export let style = '';

	export let stateMachine: StateMachine;

	let container: HTMLDivElement | null = null;
	let network: vis.Network | null = null;

	const margin = { top: 10, right: 10, bottom: 10, left: 10 };

	const nodes = new vis.DataSet<{
		id: string;
		label: string;
		margin: { top: number; right: number; bottom: number; left: number };
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
					borderWidth: 2,
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
			if (nodes.get(state) === null) {
				nodes.add({ id: state, label: state, margin });
			}
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
