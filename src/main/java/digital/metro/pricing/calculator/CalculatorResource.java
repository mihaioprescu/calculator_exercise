package digital.metro.pricing.calculator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/calculator")
public class CalculatorResource {

    private BasketCalculatorService basketCalculatorService;

    @Autowired
    public CalculatorResource(BasketCalculatorService basketCalculatorService) {
        this.basketCalculatorService = basketCalculatorService;
    }

    @PostMapping("/calculate-basket")
    public ResponseEntity<BasketCalculationResult> calculateBasket(@RequestBody Basket basket) {
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
            @PathVariable String articleId,
            @RequestParam String customerId) {
        try {
            BigDecimal price = basketCalculatorService.calculateArticlePrice(new BasketEntry(articleId, BigDecimal.ONE), customerId);
            return ResponseEntity.ok(price);
        } catch (PriceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
