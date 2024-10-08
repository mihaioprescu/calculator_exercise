package digital.metro.pricing.calculator;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import digital.metro.pricing.calculator.exception.PriceNotFoundException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CalculatorResourceTest {

    @Mock
    private BasketCalculatorService basketCalculatorService;

    @InjectMocks
    private CalculatorResource calculatorResource;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(calculatorResource).build();
    }

    @Test
    public void testCalculateBasketSuccess() throws Exception {
        Basket basket = new Basket("customer-1", Set.of(new BasketEntry("article-1", BigDecimal.ONE)));
        BasketCalculationResult result = new BasketCalculationResult("customer-1", Map.of("article-1", BigDecimal.valueOf(10.00)), BigDecimal.valueOf(10.00));

        when(basketCalculatorService.calculateBasket(basket)).thenReturn(result);

        mockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(basket)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.totalAmount").value(10.00));
    }

    @Test
    public void testCalculateBasketPriceNotFound() throws Exception {
        Basket basket = new Basket("customer-1", Set.of(new BasketEntry("article-1", BigDecimal.ONE)));

        when(basketCalculatorService.calculateBasket(basket)).thenThrow(new PriceNotFoundException("article-1"));

        mockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(basket)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCalculateBasketPriceServerError() throws Exception {
        Basket basket = new Basket("customer-1", Set.of(new BasketEntry("article-1", BigDecimal.ONE)));

        when(basketCalculatorService.calculateBasket(basket)).thenThrow(new RuntimeException());

        mockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(basket)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetArticlePriceSuccess() throws Exception {
        String articleId = "article-1";
        BigDecimal price = BigDecimal.valueOf(10.00);

        when(basketCalculatorService.getArticlePrice(articleId)).thenReturn(price);

        mockMvc.perform(get("/calculator/article/{articleId}", articleId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("10.0"));
    }

    @Test
    public void testGetArticlePriceNotFound() throws Exception {
        String articleId = "article-1";

        when(basketCalculatorService.getArticlePrice(articleId)).thenThrow(new PriceNotFoundException("Price not found"));

        mockMvc.perform(get("/calculator/article/{articleId}", articleId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetArticlePriceForCustomerSuccess() throws Exception {
        String articleId = "article-1";
        String customerId = "customer-1";
        BigDecimal price = BigDecimal.valueOf(10.00);

        when(basketCalculatorService.getArticlePriceForCustomer(articleId, customerId)).thenReturn(price);

        mockMvc.perform(get("/calculator/getarticlepriceforcustomer")
                        .param("articleId", articleId)
                        .param("customerId", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("10.0"));
    }

    @Test
    public void testGetArticlePriceForCustomerNotFound() throws Exception {
        String articleId = "article-1";
        String customerId = "customer-1";

        when(basketCalculatorService.getArticlePriceForCustomer(articleId, customerId)).thenThrow(new PriceNotFoundException("Price not found"));

        mockMvc.perform(get("/calculator/getarticlepriceforcustomer")
                        .param("articleId", articleId)
                        .param("customerId", customerId))
                .andExpect(status().isNotFound());
    }
}
