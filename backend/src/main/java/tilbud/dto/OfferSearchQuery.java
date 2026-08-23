package tilbud.dto;

import java.time.LocalDate;
import java.util.List;

public class OfferSearchQuery {

    private String q;
    private List<String> chain;
    private String category;
    private Integer minPrice;
    private Integer maxPrice;
    private LocalDate validFrom;
    private LocalDate validTo;
    private String catalogId;
    private String sort;
    private Integer page = 0;
    private Integer size = 20;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public List<String> getChain() { return chain; }
    public void setChain(List<String> chain) { this.chain = chain; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }

    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public String getCatalogId() { return catalogId; }
    public void setCatalogId(String catalogId) { this.catalogId = catalogId; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}
