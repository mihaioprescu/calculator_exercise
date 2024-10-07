package digital.metro.pricing.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import digital.metro.pricing.calculator.exception.PriceNotFoundException;

@Component
public class BasketCalculatorService {

    private PriceRepository priceRepository;

    @Autowired
    public BasketCalculatorService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    public BigDecimal getArticlePrice(String articleId, String customerId) {
        return calculateArticlePrice(new BasketEntry(articleId, BigDecimal.ONE), customerId);
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
        if (Objects.isNull(listPrice)) {
            throw new PriceNotFoundException("Price not found for article: " + articleId);
        }

        return listPrice.multiply(be.getQuantity());
    }
}
