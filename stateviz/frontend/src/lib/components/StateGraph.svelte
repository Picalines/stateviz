<script lang="ts">
	import { onMount } from 'svelte';
	import * as vis from 'vis-network/standalone';
	import type { StateMachine } from '../statelang';

	export let stateMachine: StateMachine;

	let className = '';
	export { className as class };
	export let style = '';

	let container: HTMLDivElement | null = null;
	let network: vis.Network | null = null;

	const nodes = new vis.DataSet<{ id: string; label: string }>();
	const edges = new vis.DataSet<{
		id: undefined;
		from: string;
		to: string;
		arrows: string;
	}>();

	onMount(() => {
		if (container) {
			const options: vis.Options = {
				nodes: { shape: 'box' },
			};
			network = new vis.Network(container, { nodes, edges }, options);
		}
	});

	$: if (network) {
		const states = new Set(stateMachine.states);

		for (const state of states) {
			if (nodes.get(state) === null) {
				nodes.add({ id: state, label: state });
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
