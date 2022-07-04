package org.bgerp.plugin.clb.team.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.bgcrm.model.Pair;

public class PartyBalance {
    /** Payment transition matrix. */
    private final Map<Key, BigDecimal> matrix;

    /**
     * Constructor.
     * @param paymentAmounts key - member ID, value - given amount.
     */
    public PartyBalance(List<Pair<Integer, BigDecimal>> paymentAmounts) {
        final int cnt = paymentAmounts.size();
        if (cnt == 0) {
            matrix = Collections.emptyMap();
            return;
        }

        matrix = new HashMap<>(cnt * cnt);

        // member spent amounts
        final List<BigDecimal> amounts = paymentAmounts.stream()
            .map(Pair::getSecond)
            .collect(Collectors.toList());

        // total sum
        final BigDecimal sum = amounts.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // average for a member
        final BigDecimal avg = sum.divide(new BigDecimal(cnt), RoundingMode.HALF_UP);

        // how mach to pay for each of members
        final List<BigDecimal> deltas = amounts.stream()
            .map(amount -> avg.subtract(amount))
            .collect(Collectors.toList());

        for (int i1 = 0; i1 < cnt - 1; i1++)
            for (int i2 = i1 + 1; i2 < cnt; i2++) {
                int id1 = paymentAmounts.get(i1).getFirst();
                BigDecimal delta1 = deltas.get(i1);

                int id2 = paymentAmounts.get(i2).getFirst();
                BigDecimal delta2 = deltas.get(i2);

                // no transition make sense
                if (delta1.compareTo(BigDecimal.ZERO) == 0 || delta2.compareTo(BigDecimal.ZERO) == 0)
                    continue;

                // both have same sign
                if (delta1.multiply(delta2).compareTo(BigDecimal.ZERO) > 0)
                    continue;

                if (!transition(deltas, i1, id1, delta1, i2, id2, delta2))
                    transition(deltas, i2, id2, delta2, i1, id1, delta1);
            }
    }

    /**
     * Returns payment transition from {@code fromId} to {@code toId}.
     * @param fromId
     * @param toId
     * @return positive transition or {@code null} if missing.
     */
    public BigDecimal get(int fromId, int toId) {
        return matrix.get(new Key(fromId, toId));
    }

    /**
     * Transition between deltas with different signs.
     * @param deltas deltas to modify.
     * @param indexFrom array index from.
     * @param idFrom member ID from.
     * @param deltaFrom delta from.
     * @param indexTo array index to.
     * @param idTo member ID to.
     * @param deltaTo delta to.
     * @return {@code deltaFrom} was positive and transition to {@code deltaTo} happened.
     */
    private boolean transition(List<BigDecimal> deltas, int indexFrom, int idFrom, BigDecimal deltaFrom, int indexTo, int idTo, BigDecimal deltaTo) {
        if (deltaFrom.compareTo(BigDecimal.ZERO) < 0)
            return false;

        BigDecimal amount = deltaFrom.abs().min(deltaTo.abs());
        deltas.set(indexFrom, deltaFrom.subtract(amount));
        deltas.set(indexTo, deltaTo.add(amount));

        matrix.put(new Key(idFrom, idTo), amount);

        return true;
    }

    private static class Key {
        private final int fromId;
        private final int toId;

        Key(int fromId, int toId) {
            this.fromId = fromId;
            this.toId = toId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + fromId;
            result = prime * result + toId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (fromId != other.fromId)
                return false;
            if (toId != other.toId)
                return false;
            return true;
        }
    }
}
