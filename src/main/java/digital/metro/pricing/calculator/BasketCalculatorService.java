package digital.metro.pricing.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BasketCalculatorService {

    private PriceRepository priceRepository;

    @Autowired
    public BasketCalculatorService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    public BasketCalculationResult calculateBasket(Basket basket) {
        Map<String, BigDecimal> pricedArticles = basket.getEntries().stream()
                .collect(Collectors.toMap(
                        BasketEntry::getArticleId,
                        entry -> calculateArticlePrice(entry, basket.getCustomerId())));

        BigDecimal totalAmount = pricedArticles.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BasketCalculationResult(basket.getCustomerId(), pricedArticles, totalAmount);
    }

    public BigDecimal calculateArticlePrice(BasketEntry be, String customerId) {
        String articleId = be.getArticleId();

        if (customerId != null) {
            Optional<BigDecimal> customerPrice = priceRepository.getPriceByArticleIdAndCustomerId(articleId, customerId);
            if (customerPrice.isPresent()) {
                return customerPrice.get().multiply(be.getQuantity());
            }
        }
        BigDecimal listPrice = priceRepository.getPriceByArticleId(articleId);

        return listPrice.multiply(be.getQuantity());
    }
}
