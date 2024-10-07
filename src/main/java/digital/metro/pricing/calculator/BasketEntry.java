package digital.metro.pricing.calculator;

import java.math.BigDecimal;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class BasketEntry {

    @NotBlank(message = "Article ID must not be blank")
    private String articleId;

    @NotNull(message = "Quantity must not be null")
    private BigDecimal quantity;

    private BasketEntry() {
    }

    public BasketEntry(String articleId, BigDecimal quantity) {
        this.articleId = articleId;
        this.quantity = quantity;
    }

    public String getArticleId() {
        return articleId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasketEntry that)) return false;
        return Objects.equals(articleId, that.articleId) &&
                Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId, quantity);
    }
}
