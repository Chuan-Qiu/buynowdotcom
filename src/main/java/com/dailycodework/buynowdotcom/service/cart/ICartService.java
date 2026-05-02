package com.dailycodework.buynowdotcom.service.cart;

import com.dailycodework.buynowdotcom.model.Cart;
import java.math.BigDecimal;

public interface ICartService {
    Cart getCart(Long id);
    void clearCart(Long id);
    BigDecimal getTotalPrice(Long id);
    Cart getCartByUserId(Long userId);
}