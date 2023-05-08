export function debounce<Args extends any[]>(
	func: (...args: Args) => unknown,
	timeout: number,
): (...args: Args) => void {
	let timer: number | undefined;
	return (...args) => {
		window.clearTimeout(timer);
		timer = window.setTimeout(() => func(...args), timeout);
	};
}
