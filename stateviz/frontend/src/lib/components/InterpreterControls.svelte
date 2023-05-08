<script lang="ts">
	import type { Interpreter } from '../statelang';
	import { delay } from '../utils';
	import PlaybackControls from './PlaybackControls.svelte';

	let className = '';
	export { className as class };
	export let style = '';

	export let interpreter: Interpreter;
</script>

<PlaybackControls
	started={$interpreter.started && !$interpreter.exited}
	class={className}
	{style}
	on:play={() => {
		interpreter.reset();
		interpreter.step();
	}}
	on:stop={() => interpreter.reset()}
	on:step={() => interpreter.step()}
	on:skip={async () => {
		while (!interpreter.exited) {
			interpreter.step();
			await delay(100);
		}
	}}
	on:restart={async () => {
		interpreter.reset();
		await delay(100);
		interpreter.step();
	}}
/>
