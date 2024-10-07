package digital.metro.pricing.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import digital.metro.pricing.calculator.exception.PriceNotFoundException;

@RestController
@RequestMapping("/calculator")
public class CalculatorResource {

    private BasketCalculatorService basketCalculatorService;

    @Autowired
    public CalculatorResource(BasketCalculatorService basketCalculatorService) {
        this.basketCalculatorService = basketCalculatorService;
    }

    @PostMapping("/calculate-basket")
    public ResponseEntity<BasketCalculationResult> calculateBasket(@Valid @RequestBody Basket basket) {
        try {
            BasketCalculationResult result = basketCalculatorService.calculateBasket(basket);
            return ResponseEntity.ok(result);
        } catch (PriceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/article/{articleId}/price")
    public ResponseEntity<BigDecimal> getArticlePrice(
            @PathVariable @NotBlank String articleId,
            @RequestParam(required = false) String customerId) {
        try {
            BigDecimal price = basketCalculatorService.getArticlePrice(articleId, customerId);
            return ResponseEntity.ok(price);
        } catch (PriceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
