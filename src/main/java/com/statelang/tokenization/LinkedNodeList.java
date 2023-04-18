package com.statelang.tokenization;

import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;

class LinkedNodeList<T> {

    public final class Node {
        public final T value;

        private Node next;

        private Node previous;

        private boolean removed = false;

        private Node(T value) {
            this.value = value;
        }

        private LinkedNodeList<T> list() {
            return LinkedNodeList.this;
        }

        public Node next() {
            return next;
        }

        public Node previous() {
            return previous;
        }
    }

    private Node first;

    private Node last;

    public Node first() {
        return first;
    }

    public Node last() {
        return last;
    }

    public Node addLast(T value) {
        if (first == null) {
            return first = last = new Node(value);
        }

        var newNode = new Node(value);
        last.next = newNode;
        return last = newNode;
    }

    public Node addBefore(Node node, T value) {
        Preconditions.checkNotNull(node, "node is null");

        if (node.removed || node.list() != this) {
            throw new NoSuchElementException();
        }

        var newNode = new Node(value);

        if (node.previous == null) {
            first = newNode;
        }

        newNode.next = node;
        newNode.previous = node.previous;
        node.previous = newNode;
        return newNode;
    }

    public void remove(Node node) {
        Preconditions.checkNotNull(node, "node is null");

        if (node.removed) {
            throw new NoSuchElementException();
        }

        node.removed = true;

        if (node == first) {
            first = node.next;
        }

        if (node.previous != null) {
            node.previous.next = node.next;
        }

        if (node.next != null) {
            node.next.previous = node.previous;
        }
    }

    public void removeFirst() {
        remove(first);
    }
}
