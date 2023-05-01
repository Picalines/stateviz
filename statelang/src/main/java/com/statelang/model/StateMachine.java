package com.statelang.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

public final class StateMachine {

    @Getter
    private final Set<String> states;

    @Getter
    private final Map<String, Set<String>> transitions;

    @Getter
    private String state;

    @Builder
    private StateMachine(@Singular Set<String> states, String initialState, Map<String, Set<String>> transitions) {
        Preconditions.checkArgument(!states.isEmpty());

        state = initialState;

        this.states = Collections.unmodifiableSet(states);

        var transitionsCopy = new HashMap<String, Set<String>>();

        for (var state : transitions.keySet()) {
            transitionsCopy.put(state, Collections.unmodifiableSet(transitions.get(state)));
        }

        this.transitions = Collections.unmodifiableMap(transitionsCopy);
    }

    public void performTransition(String newState) {
        Preconditions.checkArgument(states.contains(newState), "undefined state");

        var possibleNewStates = transitions.get(state);
        Preconditions.checkState(possibleNewStates != null, "no transitions from current state");
        Preconditions.checkState(possibleNewStates.contains(newState), "no transition to newState");

        state = newState;
    }

    public static final class StateMachineBuilder {

        StateMachineBuilder() {
            transitions = new HashMap<>();
            states = new ArrayList<>();
        }

        public StateMachineBuilder initialState(String initialState) {
            this.initialState = initialState;
            return this;
        }

        public StateMachineBuilder transition(String from, String to) {
            Preconditions.checkState(states.contains(from), "undefined from state");
            Preconditions.checkState(states.contains(to), "undefined to state");

            transitions
                .computeIfAbsent(from, key -> new HashSet<>())
                .add(to);

            return this;
        }

        public List<String> definedStates() {
            return Collections.unmodifiableList(states);
        }

        @SuppressWarnings("unused")
        private StateMachineBuilder transitions(Map<String, Set<String>> transitions) {
            this.transitions = transitions;
            return this;
        }
    }
}
