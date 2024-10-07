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
import java.util.Collections;
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
        // GIVEN
        Basket basket = new Basket("customer-1", Set.of(new BasketEntry("article-1", BigDecimal.ONE)));
        BasketCalculationResult result = new BasketCalculationResult("customer-1", Map.of("article-1", BigDecimal.valueOf(10.00)),
                BigDecimal.valueOf(10.00));

        when(basketCalculatorService.calculateBasket(basket)).thenReturn(result);

        //WHEN
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
        // GIVEN
        Basket basket = new Basket("customer-1", Set.of(new BasketEntry("article-1", BigDecimal.ONE)));

        when(basketCalculatorService.calculateBasket(basket)).thenThrow(new PriceNotFoundException("Price not found"));

        //WHEN
        mockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(basket)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCalculateBasketNoItems() throws Exception {
        // GIVEN
        Basket basket = new Basket("customer-1", Collections.emptySet());

        //WHEN
        mockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(basket)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCalculateBasketInvalidBasketItem() throws Exception {
        // GIVEN
        Basket basket = new Basket("customer-1", Set.of(new BasketEntry("", BigDecimal.ONE)));

        //WHEN
        mockMvc.perform(post("/calculator/calculate-basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(basket)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetArticlePriceSuccess() throws Exception {
        // GIVEN
        String articleId = "article-1";
        String customerId = "customer-1";
        BigDecimal price = BigDecimal.valueOf(10.00);

        when(basketCalculatorService.getArticlePrice(articleId, customerId)).thenReturn(price);

        //WHEN
        mockMvc.perform(get("/calculator/article/{articleId}/price", articleId)
                        .param("customerId", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("10.0"));
    }

    @Test
    public void testGetArticlePriceNotFound() throws Exception {
        // GIVEN
        String articleId = "article-1";
        String customerId = "customer-1";

        when(basketCalculatorService.getArticlePrice(articleId, customerId)).thenThrow(new PriceNotFoundException("Price not found"));

        //WHEN
        mockMvc.perform(get("/calculator/article/{articleId}/price", articleId)
                        .param("customerId", customerId))
                .andExpect(status().isNotFound());
    }
}
