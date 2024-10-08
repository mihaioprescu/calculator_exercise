package digital.metro.pricing.calculator;

import java.util.Objects;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Basket {

    private String customerId;

    @NotNull(message = "Entries must not be null")
    @Size(min = 1, message = "Basket must contain at least one entry")
    @Valid
    private Set<BasketEntry> entries;

    private Basket() {
    }

    public Basket(String customerId, Set<BasketEntry> entries) {
        this.customerId = customerId;
        this.entries = entries;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Set<BasketEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Basket)) return false;
        Basket basket = (Basket) o;
        return Objects.equals(customerId, basket.customerId) &&
                Objects.equals(entries, basket.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, entries);
    }
}
