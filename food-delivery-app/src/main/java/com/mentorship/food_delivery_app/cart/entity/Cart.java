package com.mentorship.food_delivery_app.cart.entity;

import com.mentorship.food_delivery_app.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_customer_id", nullable = false)
    private Customer customer;

    @Column(name = "current_rest_id")
    private Long restaurantId;

    @Column(name = "notes")
    private String notes;

    @Column(name = "is_locked")
    private Boolean isLocked = false;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
    }

    public void clearItems() {
        items.clear();
    }
}
