package com.thirdplace.ThirdPlaceDatabaseService;

/**
 * A simple record to abstract a where filter
 */
public record WhereFilter(
    String leftHandSide,
    Operator operator,
    String rightHandSide
) {

    public enum Operator {

        EQUAL("="),
        NOT_EQUAL("<>"),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        GREATER_THAN_OR_EQUAL_TO(">="),
        LESS_THAN_OR_EQUAL_TO("<="),
        LIKE("LIKE"),
        NOT_LIKE("NOT LIKE"),
        ILIKE("ILIKE"),
        NOT_ILIKE("NOT ILIKE"),
        IN("IN"),
        NOT_IN("NOT IN"),
        IS_NULL("IS NULL"),
        IS_NOT_NULL("IS NOT NULL");

        private final String operator;

        Operator(final String operator) {
            this.operator = operator;
        }

        public String getValue() {
            return operator;
        }
    }

    private static final String FORMATTER = "%s %s %s";

    @Override
    public String toString() {
        return String.format(FORMATTER, leftHandSide, operator.getValue(), rightHandSide);
    }
}