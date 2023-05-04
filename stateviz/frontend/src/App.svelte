<script lang="ts">
	import StateLangEditor from './lib/StateLangEditor.svelte';

	const dedent = (str: string) => {
		const lines = str.split('\n').slice(1, -1);
		const minIndent = Math.min(
			...lines.map(line => line.length - line.trimStart().length).filter(indent => indent > 0),
		);
		return lines.map(line => line.slice(minIndent)).join('\n');
	};

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
