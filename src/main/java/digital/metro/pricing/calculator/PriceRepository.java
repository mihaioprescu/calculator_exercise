package digital.metro.pricing.calculator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * A dummy implementation for testing purposes. In production, we would get real prices from a database.
 */
@Component
public class PriceRepository {

    private static final BigDecimal MIN_PRICE = BigDecimal.valueOf(0.50);
    private static final BigDecimal MAX_PRICE = BigDecimal.valueOf(30.00);

    private final Map<String, BigDecimal> prices = new HashMap<>();
    private final Random random = new Random();

    public BigDecimal getPriceByArticleId(String articleId) {
        return prices.computeIfAbsent(articleId, key -> randomPrice());
    }

    public Optional<BigDecimal> getPriceByArticleIdAndCustomerId(String articleId, String customerId) {
        BigDecimal listPrice = getPriceByArticleId(articleId);
        BigDecimal discountedPrice = getCustomerPrice(listPrice, customerId);
        return Optional.ofNullable(discountedPrice);
    }

    private BigDecimal getCustomerPrice(BigDecimal price, String customerId) {
        switch (customerId) {
            case "customer-1":
                return price.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP);
            case "customer-2":
                return price.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP);
            default:
                return null;
        }
    }

    private BigDecimal randomPrice() {
        double randomPrice = MIN_PRICE.doubleValue() + random.nextDouble() * (MAX_PRICE.doubleValue() - MIN_PRICE.doubleValue());
        return BigDecimal.valueOf(randomPrice).setScale(2, RoundingMode.HALF_UP);
    }
}
