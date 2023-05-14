package com.statelang.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

public final class StateMachine {

    public record State(String name, Map<String, String> attributes) {}

    @Getter
    private final Set<State> states;

    @Getter
    private final Map<String, Set<String>> transitions;

    @Getter
    private State initialState;

    @Builder
    private StateMachine(@Singular Set<State> states, String initialStateName, Map<String, Set<String>> transitions) {
        Preconditions.checkArgument(!states.isEmpty());

        this.states = Collections.unmodifiableSet(states);

        this.initialState = getStateByName(initialStateName);

        var transitionsCopy = new HashMap<String, Set<String>>();

        for (var state : transitions.keySet()) {
            transitionsCopy.put(state, Collections.unmodifiableSet(transitions.get(state)));
        }

        this.transitions = Collections.unmodifiableMap(transitionsCopy);
    }

    public State getStateByName(String name) {
        return states.stream()
            .filter(state -> state.name.equals(name))
            .findFirst()
            .orElseThrow();
    }

    public static final class StateMachineBuilder {

        StateMachineBuilder() {
            transitions = new HashMap<>();
            states = new ArrayList<>();
        }

        public StateMachineBuilder initialState(String initialState) {
            Preconditions
                .checkState(states.stream().anyMatch(s -> s.name.equals(initialState)), "undefined initial state");
            this.initialStateName = initialState;
            return this;
        }

        public StateMachineBuilder transition(String from, String to) {
            Preconditions.checkArgument(from != null, "from is null");
            Preconditions.checkArgument(to != null, "to is null");
            Preconditions.checkState(states.stream().anyMatch(s -> s.name.equals(from)), "undefined from state");
            Preconditions.checkState(states.stream().anyMatch(s -> s.name.equals(to)), "undefined to state");

            transitions
                .computeIfAbsent(from, key -> new HashSet<>())
                .add(to);

            return this;
        }

        public Optional<State> definedInitialState() {
            return states.stream()
                .filter(state -> state.name.equals(initialStateName))
                .findFirst();
        }

        public List<State> definedStates() {
            return Collections.unmodifiableList(states);
        }

        @SuppressWarnings("unused")
        private StateMachineBuilder transitions(Map<String, Set<String>> transitions) {
            this.transitions = transitions;
            return this;
        }
    }
}
