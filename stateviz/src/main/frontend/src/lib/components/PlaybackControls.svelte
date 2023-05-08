<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import { circOut } from 'svelte/easing';
	import { fly } from 'svelte/transition';

	let className = '';
	export { className as class };
	export let style = '';

	export let started = false;

	const dispatch = createEventDispatcher<{
		play: MouseEvent;
		stop: MouseEvent;
		step: MouseEvent;
		skip: MouseEvent;
		stopSipping: MouseEvent;
		restart: MouseEvent;
	}>();
</script>

<div class={className} {style} in:fly={{ y: 5, duration: 100, easing: circOut }}>
	<button title={started ? 'stop' : 'run'} on:click={e => dispatch(started ? 'stop' : 'play', e)}>
		<i class="bi {started ? 'bi-stop' : 'bi-play'}" />
	</button>
	<button title="step" disabled={!started} on:click={e => dispatch('step', e)}>
		<i class="bi bi-skip-end" />
	</button>
	<button title="skip to end" disabled={!started} on:click={e => dispatch('skip', e)}>
		<i class="bi bi-skip-forward" />
	</button>
	<button title="restart" disabled={!started} on:click={e => dispatch('restart', e)}>
		<i class="bi bi-arrow-clockwise" />
	</button>
</div>

<style>
	div {
		--gap: 5px;
		--border-radius: 5px;

		display: flex;
		flex-direction: row;
		gap: var(--gap);
		background: #2d2d2d;
		padding: var(--gap);
		border: solid 1px rgb(100%, 100%, 100%, 20%);
		border-radius: var(--border-radius);
	}

	button {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 35px;
		height: 35px;
		font-size: 25px;
		border: none;
		border-radius: 5px;
		background-color: transparent;
		transition: background-color linear 0.1s;
	}

	button:not(:disabled) {
		cursor: pointer;
	}

	button:hover:not(:disabled) {
		background-color: rgb(100%, 100%, 100%, 10%);
	}

	button:active:not(:disabled) {
		transform: scale(0.9);
	}

	button:disabled {
		cursor: not-allowed;
		filter: grayscale();
		opacity: 0.5;
	}

	i.bi-play {
		color: rgb(26, 243, 26);
	}

	i.bi-stop {
		color: rgb(241, 68, 16);
	}

	i.bi-skip-end {
		color: rgb(243, 210, 26);
	}

	i.bi-skip-forward {
		color: rgb(243, 210, 26);
	}

	i.bi-arrow-clockwise {
		color: rgb(22, 193, 255);
		font-size: 22px;
	}
</style>
