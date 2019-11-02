package ru.bgcrm.model;

/**
 * Класс "пара".
 * 
 * @param <T1> первый тип
 * @param <T2> второй тип
 */
public class Pair<T1, T2> {
    private T1 first;
    private T2 second;

    public Pair() {
    }

    public Pair(final T1 first, final T2 second) {
        super();
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        final int hashFirst = first != null ? first.hashCode() : 0;
        final int hashSecond = second != null ? second.hashCode() : 0;
        return (hashFirst + hashSecond) * hashSecond + hashFirst;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Pair<?, ?>) {
            final Pair<?, ?> otherPair = (Pair<?, ?>) other;
            return ((this.first == otherPair.first || (this.first != null && otherPair.first != null && this.first.equals(otherPair.first)))
                    && (this.second == otherPair.second
                            || (this.second != null && otherPair.second != null && this.second.equals(otherPair.second))));
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    public T1 getFirst() {
        return first;
    }

    public void setFirst(final T1 first) {
        this.first = first;
    }

    public T2 getSecond() {
        return second;
    }

    public void setSecond(final T2 second) {
        this.second = second;
    }
}