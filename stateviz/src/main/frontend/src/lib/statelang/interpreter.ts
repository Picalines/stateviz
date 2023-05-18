import { writable, type Readable } from 'svelte/store';
import { Decimal } from 'decimal.js';
import {
	type CompiledProgram,
	type Instruction,
	type InstanceType,
	type SourceLocation,
	type UnaryOperator,
	type BinaryOperator,
	locationEquals,
} from './model';

type JsInstanceTypeMap = {
	boolean: boolean;
	number: Decimal;
	string: string;
};

type ValidInstanceType = Exclude<InstanceType, 'unknown'>;

type JsInstanceType = JsInstanceTypeMap[ValidInstanceType];

type AllPairs<Elements extends string> = {
	[T in Elements]: { [U in Elements]: [T, U] }[Elements];
}[Elements];

export class RuntimeError extends Error {}

export type InterpretationInfo = {
	get memory(): ReadonlyMap<string, JsInstanceType>;
	get started(): boolean;
	get running(): boolean;
	get exited(): boolean;
	get exitStatus(): boolean | null;
	get error(): RuntimeError | null;
	get state(): string | null;
	get location(): SourceLocation;
};

export class Interpreter implements InterpretationInfo, Readable<InterpretationInfo> {
	readonly #subscribe: Readable<InterpretationInfo>['subscribe'];
	readonly #updateSubscribers: () => void;

	readonly #instructions: Instruction[];

	#currentInstruction = -1;
	readonly #stack: JsInstanceType[] = [];
	#memory: Map<string, JsInstanceType> = new Map();
	#state: string | null = null;
	#location: SourceLocation = { line: 1, column: 1 };
	#exitStatus: boolean | null = null;
	#error: RuntimeError | null = null;

	readonly #instructionMap: {
		[T in Instruction['type']]: (instruction: Instruction & { type: T }) => void;
	};

	constructor(compiledProgram: CompiledProgram) {
		const { subscribe, set } = writable(this);
		this.#subscribe = subscribe;
		this.#updateSubscribers = () => set(this);

		this.#instructions = compiledProgram.instructions;

		const unaryOperatorMap: {
			[T in ValidInstanceType as `${UnaryOperator}_${T}`]?: (instance: JsInstanceTypeMap[T]) => any;
		} = {
			NOT_boolean: bool => !bool,
			PLUS_number: num => num,
			MINUS_number: num => num.negated(),
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
			number_PLUS_number: (a, b) => a.plus(b),
			number_MINUS_number: (a, b) => a.minus(b),
			number_MULTIPLY_number: (a, b) => a.mul(b),
			number_DIVIDE_number: (a, b) => {
				if (b.isZero()) {
					throw new RuntimeError('zero division');
				}
				return a.div(b);
			},
			number_MODULO_number: (a, b) => {
				if (b.isZero()) {
					throw new RuntimeError('zero division');
				}
				return a.mod(b);
			},
			number_EQUALS_number: (a, b) => a.equals(b),
			number_NOT_EQUALS_number: (a, b) => !a.equals(b),
			number_GREATER_number: (a, b) => a.greaterThan(b),
			number_GREATER_OR_EQUAL_number: (a, b) => a.greaterThanOrEqualTo(b),
			number_LESS_number: (a, b) => a.lessThan(b),
			number_LESS_OR_EQUAL_number: (a, b) => a.lessThanOrEqualTo(b),
			string_PLUS_string: (a, b) => a + b,
			string_EQUALS_string: (a, b) => a === b,
			string_NOT_EQUALS_string: (a, b) => a !== b,
		};

		this.#instructionMap = {
			push: ({ value }) => this.#stack.push(typeof value == 'number' ? new Decimal(value) : value),
			load: ({ memoryKey }) => this.#stack.push(this.#memory.get(memoryKey)!),
			store: ({ memoryKey }) => this.#memory.set(memoryKey, this.#stack.pop()!),
			label: () => {},
			jump: ({ destination }) => {
				this.#currentInstruction =
					compiledProgram.jumpTable[destination] ?? this.#currentInstruction;
			},
			jump_ifn: ({ destination }) => {
				if (this.#stack.pop() === false) {
					this.#instructionMap['jump']({ type: 'jump', destination });
				}
			},
			state: ({ state }) => (this.#state = state),
			src: ({ location }) => (this.#location = location),
			exit: ({ success }) => (this.#exitStatus = success),
			un_op: ({ operator }) => {
				const instance = this.#stack.pop()!;
				const type = instance instanceof Decimal ? 'number' : typeof instance;
				const evalOperator = (unaryOperatorMap as any)[`${operator}_${type}`];
				this.#stack.push(evalOperator(instance));
			},
			bin_op: ({ operator }) => {
				const b = this.#stack.pop()!;
				const a = this.#stack.pop()!;
				const aType = a instanceof Decimal ? 'number' : typeof a;
				const bType = b instanceof Decimal ? 'number' : typeof b;
				const evalOperator = (binaryOperatorMap as any)[`${aType}_${operator}_${bType}`];
				this.#stack.push(evalOperator(a, b));
			},
		};
	}

	get memory(): ReadonlyMap<string, JsInstanceType> {
		return this.#memory;
	}

	get started(): boolean {
		return this.#currentInstruction > -1;
	}

	get running(): boolean {
		return this.started && !this.exited;
	}

	get exited(): boolean {
		return this.#exitStatus !== null;
	}

	get exitStatus(): boolean | null {
		return this.#exitStatus;
	}

	get error(): RuntimeError | null {
		return this.#error;
	}

	get state(): string | null {
		return this.#state;
	}

	get location(): SourceLocation {
		return this.#location;
	}

	get subscribe(): Readable<InterpretationInfo>['subscribe'] {
		return this.#subscribe;
	}

	step(): SourceLocation {
		const lastLocation = this.#location;

		while (!this.exited && locationEquals(lastLocation, this.#location)) {
			const instruction = this.#instructions[++this.#currentInstruction];

			try {
				(this.#instructionMap as any)[instruction.type](instruction);
			}
			catch (error: unknown) {
				if (!(error instanceof RuntimeError)) {
					throw error;
				}

				this.#error = error;
				this.#exitStatus = false;
			}
		}

		this.#updateSubscribers();
		return this.#location;
	}

	reset() {
		this.#currentInstruction = -1;
		this.#exitStatus = null;
		this.#error = null;
		this.#state = null;
		this.#location = { line: 1, column: 1 };
		this.#memory = new Map();

		this.#updateSubscribers();
	}
}
