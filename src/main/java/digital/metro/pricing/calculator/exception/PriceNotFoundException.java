package digital.metro.pricing.calculator.exception;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(String articleId) {
        super("Price not found for article: " + articleId);
    }
}
