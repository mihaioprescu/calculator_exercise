package digital.metro.pricing.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import digital.metro.pricing.calculator.exception.PriceNotFoundException;

@Component
public class BasketCalculatorService {

    private PriceRepository priceRepository;

    @Autowired
    public BasketCalculatorService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    public BigDecimal getArticlePrice(String articleId) {
        BigDecimal listPrice = priceRepository.getpricebyarticleId(articleId);
        if (Objects.isNull(listPrice)) {
            throw new PriceNotFoundException(articleId);
        }
        return listPrice;
    }

    public BigDecimal getArticlePriceForCustomer(String articleId, String customerId) {
        BigDecimal customerPrice = priceRepository.getPriceByArticleIdAndCustomerId(articleId, customerId);
        if (Objects.nonNull(customerPrice)) {
            return customerPrice;
        }
        return getArticlePrice(articleId);
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

        if (Objects.nonNull(customerId)) {
            BigDecimal customerPrice = priceRepository.getPriceByArticleIdAndCustomerId(articleId, customerId);
            if (Objects.nonNull(customerPrice)) {
                return customerPrice.multiply(be.getQuantity());
            }
        }

        return getArticlePrice(articleId).multiply(be.getQuantity());
    }
}
