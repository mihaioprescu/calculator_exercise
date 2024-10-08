package digital.metro.pricing.calculator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import digital.metro.pricing.calculator.exception.PriceNotFoundException;

public class BasketCalculatorServiceTest {

    @Mock
    private PriceRepository mockPriceRepository;

    private BasketCalculatorService service;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        service = new BasketCalculatorService(mockPriceRepository);
    }


    @Test
    public void testGetArticlePrice() {
        // GIVEN
        String articleId = "article-1";
        BigDecimal price = new BigDecimal("34.29");
        Mockito.when(mockPriceRepository.getpricebyarticleId(articleId)).thenReturn(price);

        // WHEN
        BigDecimal result = service.getArticlePrice(articleId);

        // THEN
        Assertions.assertThat(result).isEqualByComparingTo(price);
    }

    @Test
    public void testGetArticlePriceNotFound() {
        // GIVEN
        String articleId = "article-1";
        Mockito.when(mockPriceRepository.getpricebyarticleId(articleId)).thenReturn(null);

        // THEN
        Assertions.assertThatThrownBy(() -> {
            service.getArticlePrice(articleId);
        }).isInstanceOf(PriceNotFoundException.class);
    }

    @Test
    public void testGetArticlePriceForCustomer() {
        // GIVEN
        String articleId = "article-1";
        String customerId = "customer-1";
        BigDecimal price = new BigDecimal("34.29");
        Mockito.when(mockPriceRepository.getPriceByArticleIdAndCustomerId(articleId, customerId)).thenReturn(price);

        // WHEN
        BigDecimal result = service.getArticlePriceForCustomer(articleId, customerId);

        // THEN
        Assertions.assertThat(result).isEqualByComparingTo(price);
    }

    @Test
    public void testGetArticlePriceForCustomerNotFound() {
        // GIVEN
        String articleId = "article-1";
        String customerId = "customer-1";
        BigDecimal price = new BigDecimal("34.29");
        Mockito.when(mockPriceRepository.getPriceByArticleIdAndCustomerId(articleId, customerId)).thenReturn(null);
        Mockito.when(mockPriceRepository.getpricebyarticleId(articleId)).thenReturn(price);

        // WHEN
        BigDecimal result = service.getArticlePriceForCustomer(articleId, customerId);

        // THEN
        Assertions.assertThat(result).isEqualByComparingTo(price);
    }

    @Test
    public void testCalculateArticlePrice() {
        // GIVEN
        String articleId = "article-1";
        BigDecimal price = new BigDecimal("34.29");
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal expectedPrice = new BigDecimal("68.58");
        Mockito.when(mockPriceRepository.getpricebyarticleId(articleId)).thenReturn(price);

        // WHEN
        BigDecimal result = service.calculateArticlePrice(new BasketEntry(articleId, quantity), null);

        // THEN
        Assertions.assertThat(result).isEqualByComparingTo(expectedPrice);
    }

    @Test
    public void testCalculateArticlePriceForCustomer() {
        // GIVEN
        String articleId = "article-1";
        BigDecimal standardPrice = new BigDecimal("34.29");
        BigDecimal customerPrice = new BigDecimal("29.99");
        String customerId = "customer-1";

        Mockito.when(mockPriceRepository.getpricebyarticleId(articleId)).thenReturn(standardPrice);
        Mockito.when(mockPriceRepository.getPriceByArticleIdAndCustomerId(articleId, customerId)).thenReturn(customerPrice);

        // WHEN
        BigDecimal result = service.calculateArticlePrice(new BasketEntry(articleId, BigDecimal.ONE), "customer-1");

        // THEN
        Assertions.assertThat(result).isEqualByComparingTo(customerPrice);
    }

    @Test
    public void testCalculateBasket() {
        // GIVEN
        Basket basket = new Basket("customer-1", Set.of(
                new BasketEntry("article-1", BigDecimal.ONE),
                new BasketEntry("article-2", BigDecimal.ONE),
                new BasketEntry("article-3", BigDecimal.ONE)));

        Map<String, BigDecimal> prices = Map.of(
                "article-1", new BigDecimal("1.50"),
                "article-2", new BigDecimal("0.29"),
                "article-3", new BigDecimal("9.99"));

        Mockito.when(mockPriceRepository.getpricebyarticleId("article-1")).thenReturn(prices.get("article-1"));
        Mockito.when(mockPriceRepository.getpricebyarticleId("article-2")).thenReturn(prices.get("article-2"));
        Mockito.when(mockPriceRepository.getpricebyarticleId("article-3")).thenReturn(prices.get("article-3"));

        // WHEN
        BasketCalculationResult result = service.calculateBasket(basket);

        // THEN
        Assertions.assertThat(result.getCustomerId()).isEqualTo("customer-1");
        Assertions.assertThat(result.getPricedBasketEntries()).isEqualTo(prices);
        Assertions.assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("11.78"));
    }

    @Test
    public void testCalculateBasketWithQuantitiesAndCustomerPrices() {
        // GIVEN
        Basket basket = new Basket("customer-1", Set.of(
                new BasketEntry("article-1", BigDecimal.valueOf(2)),
                new BasketEntry("article-2", BigDecimal.ONE),
                new BasketEntry("article-3", BigDecimal.valueOf(3))));

        Map<String, BigDecimal> prices = Map.of(
                "article-1", new BigDecimal("1.50"),
                "article-2", new BigDecimal("0.29"),
                "article-3", new BigDecimal("10"));

        Mockito.when(mockPriceRepository.getpricebyarticleId("article-1")).thenReturn(prices.get("article-1"));
        Mockito.when(mockPriceRepository.getpricebyarticleId("article-2")).thenReturn(prices.get("article-2"));
        Mockito.when(mockPriceRepository.getpricebyarticleId("article-3")).thenReturn(prices.get("article-3"));

        Mockito.when(mockPriceRepository.getPriceByArticleIdAndCustomerId("article-3", "customer-1")).thenReturn((new BigDecimal(9)));

        // WHEN
        BasketCalculationResult result = service.calculateBasket(basket);

        // THEN

        Map<String, BigDecimal> expectedPrices = Map.of(
                "article-1", new BigDecimal("3.00"),
                "article-2", new BigDecimal("0.29"),
                "article-3", new BigDecimal("27"));

        Assertions.assertThat(result.getCustomerId()).isEqualTo("customer-1");
        Assertions.assertThat(result.getPricedBasketEntries()).isEqualTo(expectedPrices);
        Assertions.assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("30.29"));
    }

    @Test
    public void testCalculateBasketPriceNotFound() {
        // GIVEN
        Basket basket = new Basket("customer-1", Set.of(
                new BasketEntry("article-1", BigDecimal.ONE),
                new BasketEntry("article-2", BigDecimal.ONE),
                new BasketEntry("article-3", BigDecimal.ONE)));

        Map<String, BigDecimal> prices = Map.of(
                "article-1", new BigDecimal("1.50"),
                "article-2", new BigDecimal("0.29"));

        Mockito.when(mockPriceRepository.getpricebyarticleId("article-1")).thenReturn(prices.get("article-1"));
        Mockito.when(mockPriceRepository.getpricebyarticleId("article-2")).thenReturn(prices.get("article-2"));
        Mockito.when(mockPriceRepository.getpricebyarticleId("article-3")).thenReturn(prices.get("article-3"));

        // THEN
        Assertions.assertThatThrownBy(() -> {
            service.calculateBasket(basket);
        }).isInstanceOf(PriceNotFoundException.class);
    }
}
