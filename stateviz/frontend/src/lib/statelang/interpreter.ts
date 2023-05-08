import type {
	CompiledProgram,
	Instruction,
	InstanceType,
	SourceLocation,
	UnaryOperator,
	BinaryOperator,
} from './model';

type JsInstanceTypeMap = {
	boolean: boolean;
	number: number;
	string: string;
};

type ValidInstanceType = Exclude<InstanceType, 'unknown'>;

type JsInstanceType = JsInstanceTypeMap[ValidInstanceType];

type AllPairs<Elements extends string> = {
	[T in Elements]: { [U in Elements]: [T, U] }[Elements];
}[Elements];

export class Interpreter {
	readonly #instructions: Instruction[];

	#currentInstruction = 0;

	readonly #stack: JsInstanceType[] = [];

	readonly #memory: Map<string, JsInstanceType> = new Map();

	#location: SourceLocation = { line: 1, column: 1 };

	#exitStatus: boolean | null = null;

	readonly #instructionMap: {
		[T in Instruction['type']]: (instruction: Instruction & { type: T }) => void;
	};

	constructor(compiledProgram: CompiledProgram) {
		this.#instructions = compiledProgram.instructions;

		const unaryOperatorMap: {
			[T in ValidInstanceType as `${UnaryOperator}_${T}`]?: (instance: JsInstanceTypeMap[T]) => any;
		} = {
			NOT_boolean: bool => !bool,
			PLUS_number: num => num,
			MINUS_number: num => -num,
		};

		const binaryOperatorMap: {
			[T in AllPairs<ValidInstanceType> as `${T[0]}_${BinaryOperator}_${T[1]}`]?: (
				a: JsInstanceTypeMap[T[0]],
				b: JsInstanceTypeMap[T[1]],
			) => JsInstanceType;
		} = {
			boolean_AND_boolean: (a, b) => a && b,
			boolean_OR_boolean: (a, b) => a || b,
			boolean_EQUALS_boolean: (a, b) => a === b,
			boolean_NOT_EQUALS_boolean: (a, b) => a !== b,
			number_PLUS_number: (a, b) => a + b,
			number_MINUS_number: (a, b) => a - b,
			number_MULTIPLY_number: (a, b) => a * b,
			number_DIVIDE_number: (a, b) => a / b,
			number_MODULO_number: (a, b) => a % b,
			number_EQUALS_number: (a, b) => a === b,
			number_NOT_EQUALS_number: (a, b) => a !== b,
			number_GREATER_number: (a, b) => a > b,
			number_GREATER_OR_EQUAL_number: (a, b) => a >= b,
			number_LESS_number: (a, b) => a < b,
			number_LESS_OR_EQUAL_number: (a, b) => a <= b,
			string_PLUS_string: (a, b) => a + b,
			string_EQUALS_string: (a, b) => a === b,
			string_NOT_EQUALS_string: (a, b) => a !== b,
		};

		this.#instructionMap = {
			push: ({ value }) => this.#stack.push(value),
			load: ({ memoryKey }) => this.#stack.push(this.#memory.get(memoryKey)!),
			store: ({ memoryKey }) => this.#memory.set(memoryKey, this.#stack.pop()!),
			label: () => {},
			jump: ({ destination }) => {
				this.#currentInstruction = compiledProgram.jumpTable[destination] ?? this.#currentInstruction;
			},
			jump_ifn: ({ destination }) => {
				if (this.#stack.pop() === false) {
					this.#instructionMap['jump']({ type: 'jump', destination });
				}
			},
			src: ({ location }) => (this.#location = location),
			exit: ({ success }) => (this.#exitStatus = success),
			un_op: ({ operator }) => {
				const instance = this.#stack.pop()!;
				const evalOperator = (unaryOperatorMap as any)[`${operator}_${typeof instance}`];
				this.#stack.push(evalOperator(instance));
			},
			bin_op: ({ operator }) => {
				const b = this.#stack.pop()!;
				const a = this.#stack.pop()!;
				const evalOperator = (binaryOperatorMap as any)[`${typeof a}_${operator}_${typeof b}`];
				this.#stack.push(evalOperator(a, b));
			},
		};
	}

	get memory(): ReadonlyMap<string, JsInstanceType> {
		return this.#memory;
	}

	get exited(): boolean {
		return this.#exitStatus !== null;
	}

	get exitStatus(): boolean | null {
		return this.#exitStatus;
	}

	get location(): SourceLocation {
		return this.#location;
	}

	interpret(): SourceLocation {
		const lastLocation = this.#location;

		while (!this.exited && locationEquals(lastLocation, this.#location)) {
			const instruction = this.#instructions[this.#currentInstruction++];
			(this.#instructionMap as any)[instruction.type](instruction);
		}

		return this.#location;
	}
}

function locationEquals(a: SourceLocation, b: SourceLocation) {
	const { line: aLine, column: aColumn } = a;
	const { line: bLine, column: bColumn } = b;
	return aLine == bLine && aColumn == bColumn;
}
