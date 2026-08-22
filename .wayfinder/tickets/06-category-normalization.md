# Ticket: Category/normalization taxonomy — cross-chain product matching

**Labels**: `wayfinder:grilling`

## Question

How do we normalize product names across chains so "Arla Minimælk 1L" (Netto) matches "Arla Minimælk 1 liter" (Rema 1000)?
1. What normalization rules? (lowercase, remove units, brand alias mapping, Danish stemming)
2. Do we need a product master catalog, or just fuzzy search on normalized_name?
3. How to handle chain-specific brands (e.g., "Netto" brand vs "Rema 1000" brand)?
4. Category hierarchy: 2-level (category > subcategory) or 3-level? Who defines it — us, or infer from chains?
5. Unit normalization: "1L", "1 l", "1000ml", "1 kg", "500g" → standard unit + quantity

**Deliverable**: A design document (Markdown) with normalization pipeline spec, category taxonomy (CSV/JSON), and brand alias map. Recorded as resolution comment on this ticket.