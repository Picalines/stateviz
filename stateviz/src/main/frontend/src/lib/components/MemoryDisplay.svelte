<script lang="ts">
	import { circOut } from 'svelte/easing';
	import { fly } from 'svelte/transition';
	import { Decimal } from 'decimal.js';

	let className = '';
	export { className as class };
	export let style = '';

	export let memory: ReadonlyMap<string, any>;
</script>

{#if memory.size > 0}
	<div id="container" class={className} {style} in:fly={{ y: 5, duration: 100, easing: circOut }}>
		<div>
			{#each [...memory.keys()] as key}
				<div>{key}</div>
			{/each}
		</div>
		<div>
			{#each [...memory.values()] as value}
				<div class="i-{value instanceof Decimal ? 'number' : typeof value}">{value}</div>
			{/each}
		</div>
	</div>
{/if}

<style>
	#container {
		background: #1e1e1e;
		padding: 0.5rem 1rem 0.5rem 1rem;
		display: flex;
		flex-direction: row;
		gap: 1rem;
		color: white;
		border: solid 1px rgb(100%, 100%, 100%, 20%);
		border-radius: 5px;
	}

	#container > div {
		display: flex;
		flex-direction: column;
	}

	.i-boolean {
		color: #da70d6;
	}

	.i-number {
		color: #569cd6;
	}

	.i-string {
		color: #ce7f55;
	}
</style>
