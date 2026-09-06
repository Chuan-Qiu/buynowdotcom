package com.dailycodework.buynowdotcom.service.cart;

import com.dailycodework.buynowdotcom.model.Cart;
import com.dailycodework.buynowdotcom.model.User;
import com.dailycodework.buynowdotcom.repository.CartItemRepository;
import com.dailycodework.buynowdotcom.repository.CartRepository;
import com.dailycodework.buynowdotcom.service.user.IUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final IUserService userService;

    /**
     * Every cart lookup funnels through here, so the ownership check placed in
     * this method also protects {@link CartItemService}, which resolves its cart
     * the same way. The cart id in the URL is never trusted on its own.
     */
    @Override
    public Cart getCart(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
        assertOwnedByAuthenticatedUser(cart);
        return cart;
    }

    @Override
    @Transactional
    public void clearCart(Long id) {
        Cart cart = getCart(id);
        cartItemRepository.deleteAllByCartId(id);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Override
    public BigDecimal getTotalPrice(Long id) {
        Cart cart = getCart(id);
        return cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Cart getCartByUserId(Long userId) {
        User authenticated = userService.getAuthenticatedUser();
        if (!authenticated.getId().equals(userId)) {
            throw new AccessDeniedException("You may only access your own cart");
        }
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));
    }

    private void assertOwnedByAuthenticatedUser(Cart cart) {
        User authenticated = userService.getAuthenticatedUser();
        if (cart.getUser() == null || !cart.getUser().getId().equals(authenticated.getId())) {
            throw new AccessDeniedException("This cart does not belong to the authenticated user");
        }
    }
}
