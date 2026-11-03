package br.com.oficina.inventory.domain;

public enum StockMovementType {
    INBOUND(1),
    OUTBOUND(-1),
    RESERVATION(-1),
    REVERSAL(1);

    private final int sign;

    StockMovementType(int sign) {
        this.sign = sign;
    }

    public int sign() {
        return sign;
    }
}
