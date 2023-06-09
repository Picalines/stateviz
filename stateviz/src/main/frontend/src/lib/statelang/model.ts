export type SourceText = {
	descriptor: string;
	text: string;
};

export type SourceLocation = {
	line: number;
	column: number;
};

export type SourceSelection = {
	start: SourceLocation;
	end: SourceLocation;
};

export type Report = {
	selection: SourceSelection;
	kind:
		| 'INVALID_TOKEN'
		| 'UNEXPECTED_TOKEN'
		| 'UNEXPECTED_END_OF_INPUT'
		| 'END_OF_INPUT_EXPECTED'
		| 'VALUE_EXPRESSION_EXPECTED'
		| 'CONDITION_EXPECTED'
		| 'VARIABLE_EXPECTED'
		| 'VARIABLE_OR_CONSTANT_EXPECTED'
		| 'AMBIGUOUS_DEFINITION'
		| 'MISSING_STATE_DEFINITION'
		| 'TOO_LITTLE_STATES'
		| 'DUPLICATE_IDENTIFIER'
		| 'UNDEFINED_OPERATOR'
		| 'UNDEFINED_VARIABLE'
		| 'UNDEFINED_STATE'
		| 'CONSTANT_ASSIGNMENT'
		| 'TYPE_ERROR'
		| 'UNREACHABLE_CODE'
		| 'UNREACHABLE_STATE';
	info: string | null;
	severity: 'INFO' | 'WARNING' | 'ERROR';
	unexpectedTokenKind: string | null;
	expectedTokenKinds: string[];
};

export type StateMachineState = {
	name: string;
	attributes: Record<string, string>;
};

export type StateMachine = {
	initialState: StateMachineState;
	states: StateMachineState[];
	transitions: Record<string, string[]>;
};

export type UnaryOperator = 'PLUS' | 'MINUS' | 'NOT';

export type BinaryOperator =
	| 'PLUS'
	| 'MINUS'
	| 'MULTIPLY'
	| 'DIVIDE'
	| 'MODULO'
	| 'LESS'
	| 'LESS_OR_EQUAL'
	| 'GREATER'
	| 'GREATER_OR_EQUAL'
	| 'EQUALS'
	| 'NOT_EQUALS'
	| 'AND'
	| 'OR';

export type InstanceType = 'boolean' | 'number' | 'string' | 'unknown';

type TypedObject<Map extends Record<string, any>> = {
	[T in keyof Map]: { type: T } & Map[T];
}[keyof Map];

export type Instruction = TypedObject<{
	un_op: { operator: UnaryOperator };
	bin_op: { operator: BinaryOperator };
	exit: { success: boolean };
	jump: { destination: string };
	jump_ifn: { destination: string };
	label: { label: string };
	push: { value: string | number | boolean };
	store: { memoryKey: string };
	load: { memoryKey: string };
	src: { location: SourceLocation };
	state: { state: string };
}>;

export type Symbol = { id: string } & TypedObject<{
	state: { stateName: string };
	variable: { variableName: string; variableType: InstanceType };
	constant: { constantName: string; constantType: InstanceType };
}>;

export type CompiledProgram = {
	stateMachine: StateMachine;
	instructions: Instruction[];
	jumpTable: Record<string, number>;
	symbols: Record<string, Symbol>;
};

export const stateLangSyntax = {
	keywords: [
		'state',
		'let',
		'const',
		'when',
		'assert',
		'if',
		'else',
		'and',
		'or',
		'not',
		'true',
		'false',
	],
	operators: [':=', '!=', '=', '+', '-', '*', '/', '%'],
} as const;

export function locationEquals(a: SourceLocation, b: SourceLocation) {
	const { line: aLine, column: aColumn } = a;
	const { line: bLine, column: bColumn } = b;
	return aLine == bLine && aColumn == bColumn;
}
