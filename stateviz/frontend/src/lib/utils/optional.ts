export class Optional<T> {
	readonly #hasValue: boolean;
	readonly #value: T;

	static readonly #none = new Optional<any>(false);

	private constructor(hasValue: boolean, value?: T) {
		this.#hasValue = hasValue;
		this.#value = value as T;
	}

	static some<T>(value: T): Optional<T> {
		return new Optional(true, value);
	}

	static none<T>(): Optional<T> {
		return this.#none;
	}

	static fromNullable<T>(nullable: T | undefined | null): Optional<T> {
		return nullable !== undefined && nullable !== null ? Optional.some(nullable) : Optional.none();
	}

	static someIf<T>(condition: boolean, value: T): Optional<T> {
		return condition ? Optional.some(value) : Optional.none();
	}

	static async awaited<T>(promise: Promise<Optional<T>>): Promise<T> {
		return (await promise).get();
	}

	get isSome(): boolean {
		return this.#hasValue;
	}

	get isNone(): boolean {
		return !this.#hasValue;
	}

	flatMap<U>(mapper: (value: T) => Optional<U>): Optional<U> {
		return this.#hasValue ? mapper(this.#value) : Optional.none();
	}

	map<U>(mapper: (value: T) => U): Optional<U> {
		return this.flatMap(value => Optional.some(mapper(value)));
	}

	filter(predicate: (value: T) => boolean): Optional<T> {
		return this.#hasValue && predicate(this.#value) ? this : Optional.none();
	}

	filterNonNullable(): Optional<NonNullable<T>> {
		return this.filter(value => value !== null && value !== undefined) as Optional<NonNullable<T>>;
	}

	orElseGet<U>(defaultValueGetter: () => U): T | U {
		return this.#hasValue ? this.#value : defaultValueGetter();
	}

	orElse<U>(defaultValue: U): T | U {
		return this.orElseGet(() => defaultValue);
	}

	orThrow(errorCreator: () => any): T {
		if (!this.#hasValue) {
			throw errorCreator();
		}

		return this.#value;
	}

	get(): T {
		return this.orThrow(
			() => new Error(`${Optional.name}.${this.get.name} called on empty instance`),
		);
	}

	match<TS, TN>(onSome: (value: T) => TS, onNone: () => TN): TS | TN {
		return this.#hasValue ? onSome(this.#value) : onNone();
	}

	ifSome(callback: (value: T) => void): void {
		if (this.#hasValue) {
			callback(this.#value);
		}
	}

	ifNone(callback: () => void): void {
		if (!this.#hasValue) {
			callback();
		}
	}
}
