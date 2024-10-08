package digital.metro.pricing.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/calculator")
@Validated
public class CalculatorResource {

    private BasketCalculatorService basketCalculatorService;

    @Autowired
    public CalculatorResource(BasketCalculatorService basketCalculatorService) {
        this.basketCalculatorService = basketCalculatorService;
    }

    @PostMapping("/calculate-basket")
    public ResponseEntity<BasketCalculationResult> calculateBasket(@Valid @RequestBody Basket basket) {
        BasketCalculationResult result = basketCalculatorService.calculateBasket(basket);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/article/{articleId}")
    public ResponseEntity<BigDecimal> getArticlePrice(
            @PathVariable @NotBlank String articleId) {
        BigDecimal price = basketCalculatorService.getArticlePrice(articleId);
        return ResponseEntity.ok(price);
    }

    @GetMapping("/getarticlepriceforcustomer")
    public ResponseEntity<BigDecimal> getArticlePriceForCustomer(@RequestParam String articleId, @RequestParam String customerId) {
        BigDecimal price = basketCalculatorService.getArticlePriceForCustomer(articleId, customerId);
        return ResponseEntity.ok(price);
    }

}
