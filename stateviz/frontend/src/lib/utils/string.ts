export function dedent(str: string) {
	const lines = str.split('\n').slice(1, -1);

	const lineIndents = lines.map(line => line.length - line.trimStart().length);

	const minIndent = Math.min(...lineIndents.filter(indent => indent > 0));

	return lines.map(line => line.slice(minIndent)).join('\n');
}

export function joinWithLast(array: readonly unknown[], separator: string, lastSeparator: string) {
	if (array.length <= 2) {
		return array.join(lastSeparator);
	}

	return array.slice(0, -1).join(separator) + lastSeparator + String(array.at(-1));
}
