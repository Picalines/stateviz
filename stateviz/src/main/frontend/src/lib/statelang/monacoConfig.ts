import * as monaco from 'monaco-editor';
import { stateLangSyntax } from './model';

export const statelangId = 'statelang';

monaco.languages.register({ id: statelangId });

monaco.languages.setMonarchTokensProvider(statelangId, {
	keywords: stateLangSyntax.keywords,

	operators: stateLangSyntax.operators,

	operatorSymbols: new RegExp(
		`[${[...new Set(stateLangSyntax.operators.join(''))].join('').replaceAll('-', '\\-')}]+`,
	),

	tokenizer: {
		root: [
			// identifiers and keywords
			[
				/[a-zA-Z_][a-zA-Z0-9_]*/,
				{
					cases: {
						'@keywords': 'keyword',
						'@default': 'variable.parameter',
					},
				},
			],

			// whitespace
			{ include: '@whitespace' },

			// strings
			[/"([^"\\]|\\.)*$/, 'string.invalid'], // non-teminated string
			[/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],

			// delimiters and operators
			[/[{}()]/, '@brackets'],
			[/[<>](?!@operatorSymbols)/, '@brackets'],
			[
				/@operatorSymbols/,
				{
					cases: {
						'@operators': 'operator',
						'@default': '',
					},
				},
			],

			// numbers
			[/(\d+(\.\d+)?|\.\d+)(\w*)/, 'number.float'],

			// delimiter: after number because of .\d floats
			[/[;,.]/, 'delimiter'],
		],

		comment: [[/#.*$/, 'comment']],

		string: [
			[/[^\\"]+/, 'string'],
			[/\\./, 'string.escape.invalid'],
			[/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }],
		],

		whitespace: [
			[/[ \t\r\n]+/, 'white'],
			[/#.*$/, 'comment'],
		],
	},
});

monaco.languages.setLanguageConfiguration(statelangId, {
	comments: {
		lineComment: '#',
	},

	autoClosingPairs: [
		{ open: '(', close: ')' },
		{ open: '{', close: '}' },
	],

	brackets: [
		['(', ')'],
		['{', '}'],
	],
});
