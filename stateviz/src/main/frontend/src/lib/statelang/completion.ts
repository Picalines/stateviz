import * as monaco from 'monaco-editor';
import { stateLangSyntax, type Symbol as StateLangSymbol } from './model';
import { statelangId } from './monacoConfig';

const keywordSuggestions: Omit<monaco.languages.CompletionItem, 'range'>[] =
	stateLangSyntax.keywords.map(keyword => ({
		kind: monaco.languages.CompletionItemKind.Keyword,
		label: keyword,
		insertText: keyword,
		detail: 'keyword',
	}));

const SYMBOL_COMPLETION_MAP: {
	[T in StateLangSymbol['type']]: (
		symbol: StateLangSymbol & { type: T },
	) => Omit<monaco.languages.CompletionItem, 'range'>;
} = {
	state: ({ stateName }) => ({
		kind: monaco.languages.CompletionItemKind.EnumMember,
		label: stateName,
		insertText: stateName,
		detail: 'state',
	}),
	variable: ({ variableName, variableType }) => ({
		kind: monaco.languages.CompletionItemKind.Variable,
		label: variableName,
		insertText: variableName,
		detail: variableType,
	}),
	constant: ({ constantName, constantType }) => ({
		kind: monaco.languages.CompletionItemKind.Constant,
		label: constantName,
		insertText: constantName,
		detail: constantType,
	}),
};

export function registerStateLangCompletionProvider(
	symbols: readonly StateLangSymbol[],
): monaco.IDisposable {
	return monaco.languages.registerCompletionItemProvider(statelangId, {
		async provideCompletionItems(model, position) {
			const word = model.getWordUntilPosition(position);

			const range: monaco.IRange = {
				startLineNumber: position.lineNumber,
				startColumn: word.startColumn,
				endLineNumber: position.lineNumber,
				endColumn: word.endColumn,
			};

			const symbolSuggestions: Omit<monaco.languages.CompletionItem, 'range'>[] = symbols.map(
				symbol => SYMBOL_COMPLETION_MAP[symbol.type](symbol as any),
			);

			return {
				suggestions: [...keywordSuggestions, ...symbolSuggestions].map(suggestion => ({
					...suggestion,
					range,
				})),
			};
		},
	});
}
