package br.com.senac.projectdevalle.catalog.domain.offer;

import java.time.DayOfWeek;

// RF08 — oferta recorrente (ex.: "toda quinta-feira") ou pontual (safra, pescado do dia).
public record Recurrence(RecurrenceType type, DayOfWeek dayOfWeek) {

    public Recurrence {
        if (type == RecurrenceType.RECURRING && dayOfWeek == null) {
            throw new IllegalArgumentException("dayOfWeek is required for recurring offers");
        }
        if (type == RecurrenceType.ONE_TIME && dayOfWeek != null) {
            throw new IllegalArgumentException("dayOfWeek must not be set for one-time offers");
        }
    }

    public static Recurrence oneTime() {
        return new Recurrence(RecurrenceType.ONE_TIME, null);
    }

    public static Recurrence weekly(DayOfWeek dayOfWeek) {
        return new Recurrence(RecurrenceType.RECURRING, dayOfWeek);
    }
}
