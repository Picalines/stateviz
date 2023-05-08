import axios from 'axios';
import { Optional } from '../utils';
import type { Report, CompiledProgram, SourceText } from './model';

export type CompilationResult = {
	reports: readonly Report[];
	program: CompiledProgram | null;
};

export async function compileSource(sourceText: SourceText): Promise<Optional<CompilationResult>> {
	try {
		const host = import.meta.env.DEV ? 'localhost:8080' : (location.host || 'localhost:8080');

		const response = await axios.post<CompilationResult>(
			`http://${host}/statelang/compile`,
			sourceText,
		);

		return Optional.some(response.data);
	} catch {
		return Optional.none();
	}
}
