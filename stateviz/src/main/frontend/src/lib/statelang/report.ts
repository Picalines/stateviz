import * as monaco from 'monaco-editor';
import { joinWithLast } from '../utils';
import type { Report } from './model';

const REPORT_MESSAGE_MAP: Record<Report['kind'], (report: Report) => string> = {
	INVALID_TOKEN: report => 'invalid token',
	UNEXPECTED_TOKEN: report => 'unexpected token',
	UNEXPECTED_END_OF_INPUT: report => 'unexpected end of input',
	END_OF_INPUT_EXPECTED: report => 'end of input expected',
	VALUE_EXPRESSION_EXPECTED: report => 'value expression expected',
	CONDITION_EXPECTED: report => 'condition expected',
	VARIABLE_EXPECTED: report => 'variable expected',
	VARIABLE_OR_CONSTANT_EXPECTED: report => 'variable or constant expected',
	AMBIGUOUS_DEFINITION: report => 'ambiguous definition',
	MISSING_STATE_DEFINITION: report => 'missing state definition',
	TOO_LITTLE_STATES: report => 'too little states',
	DUPLICATE_IDENTIFIER: report => `duplicate identifier '${report.info}'`,
	UNDEFINED_OPERATOR: report => `operator ${report.info} is not defined`,
	UNDEFINED_VARIABLE: report => `variable ${report.info} is not defined`,
	UNDEFINED_STATE: report => `state ${report.info} is not defined`,
	CONSTANT_ASSIGNMENT: report => 'assignment to constant',
	TYPE_ERROR: report => {
		const [actualType, expectedType] = (report.info ?? '? ?').split(' ');
		return `type error: ${actualType} was given, but ${expectedType} is expected`;
	},
	UNREACHABLE_CODE: report => 'unreachable code',
	UNREACHABLE_STATE: report => `state ${report.info} is unreachable`,
};

function humanReportMessage(report: Report): string {
	const lines = [REPORT_MESSAGE_MAP[report.kind](report)];

	if (report.unexpectedTokenKind) {
		lines.push(`unexpected ${report.unexpectedTokenKind}`);
	}

	if (report.expectedTokenKinds.length > 0) {
		lines.push(`expected ${joinWithLast(report.expectedTokenKinds, ', ', ' or ')}`);
	}

	return lines.map((line, i) => (i > 0 ? ' - ' + line : line)).join('\n');
}

const MARKER_SEVERITY_MAP: Record<Report['severity'], monaco.MarkerSeverity> = {
	INFO: monaco.MarkerSeverity.Info,
	WARNING: monaco.MarkerSeverity.Warning,
	ERROR: monaco.MarkerSeverity.Error,
};

export function reportToMarkerData(report: Report): monaco.editor.IMarkerData {
	return {
		startLineNumber: report.selection.start.line,
		startColumn: report.selection.start.column,
		endLineNumber: report.selection.end.line,
		endColumn: report.selection.end.column + 1,

		severity: MARKER_SEVERITY_MAP[report.severity],

		message: humanReportMessage(report),
	};
}
