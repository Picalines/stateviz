<script lang="ts">
	import StateLangEditor from './lib/components/StateLangEditor.svelte';
	import { dedent } from './lib/utils';

	const initialProgram = dedent(`
		state {
			COUNTING,
			STOPPED,
		}

		const stop := 10;
		let count := 0;

		when COUNTING {
			count := count + 1;

			if count = stop {
				state := STOPPED;
			}

			assert count < stop;
		}
		`);
</script>

<main>
	<StateLangEditor value={initialProgram} />
</main>
