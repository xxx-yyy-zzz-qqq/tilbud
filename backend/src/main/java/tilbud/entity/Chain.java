package tilbud.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chains")
public class Chain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dealer_id", nullable = false, unique = true, length = 10)
    private String dealerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String website;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 6)
    private String color;

    @Column(length = 2)
    private String country = "DK";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Chain() {}

    public Chain(String dealerId, String name) {
        this.dealerId = dealerId;
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getDealerId() { return dealerId; }
    public String getName() { return name; }
    public String getWebsite() { return website; }
    public String getLogoUrl() { return logoUrl; }
    public String getColor() { return color; }
    public String getCountry() { return country; }
    public Instant getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setWebsite(String website) { this.website = website; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setColor(String color) { this.color = color; }
    public void setCountry(String country) { this.country = country; }
}
