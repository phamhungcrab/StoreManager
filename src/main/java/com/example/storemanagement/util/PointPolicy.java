package com.example.storemanagement.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Quy tắc quy đổi tiền → điểm. Đổi số này để thay chính sách.
 * Ví dụ mặc định: 1 điểm cho mỗi 10.000đ doanh thu hợp lệ. */
public final class PointPolicy {
    private PointPolicy() {}

    public static final BigDecimal VND_PER_POINT = new BigDecimal("10000");

    /**
     * Tính điểm từ số tiền (>=0). Làm tròn xuống để tránh cộng dư.
     */
    public static int calcPoints(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) return 0;
        return amount.divide(VND_PER_POINT, 0, RoundingMode.FLOOR).intValue();
    }
}