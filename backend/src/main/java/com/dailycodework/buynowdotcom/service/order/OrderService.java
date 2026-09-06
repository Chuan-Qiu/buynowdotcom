package com.dailycodework.buynowdotcom.service.order;

import com.dailycodework.buynowdotcom.dto.OrderDto;
import com.dailycodework.buynowdotcom.enums.OrderStatus;
import com.dailycodework.buynowdotcom.model.Cart;
import com.dailycodework.buynowdotcom.model.Order;
import com.dailycodework.buynowdotcom.model.OrderItem;
import com.dailycodework.buynowdotcom.model.Product;
import com.dailycodework.buynowdotcom.model.User;
import com.dailycodework.buynowdotcom.repository.OrderRepository;
import com.dailycodework.buynowdotcom.repository.ProductRepository;
import com.dailycodework.buynowdotcom.service.cart.ICartService;
import com.dailycodework.buynowdotcom.service.user.IUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ICartService cartService;
    private final IUserService userService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Order placeOrder(Long userId) {
        assertIsAuthenticatedUser(userId);
        Cart cart = cartService.getCartByUserId(userId);
        Order order = createOrder(cart);
        List<OrderItem> orderItems = createOrderItems(order, cart);
        order.setOrderItems(new ArrayList<>(orderItems));
        order.setTotalAmount(calculateTotalAmount(orderItems));
        Order savedOrder = orderRepository.save(order);
        clearCart(cart);
        return savedOrder;
    }

    private Order createOrder(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());
        return order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart) {
        return cart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            product.setInventory(product.getInventory() - cartItem.getQuantity());
            productRepository.save(product);
            return new OrderItem(order, product, cartItem.getUnitPrice(), cartItem.getQuantity());
        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void clearCart(Cart cart) {
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
    }

    @Override
    public OrderDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        assertIsAuthenticatedUser(order.getUser() == null ? null : order.getUser().getId());
        return convertToDto(order);
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        assertIsAuthenticatedUser(userId);
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    private OrderDto convertToDto(Order order) {
        return modelMapper.map(order, OrderDto.class);
    }

    /**
     * Orders are addressed by ids that appear in the URL. Those ids identify the
     * resource but must never be treated as proof of ownership, so every entry
     * point compares them against the authenticated principal.
     */
    private void assertIsAuthenticatedUser(Long userId) {
        User authenticated = userService.getAuthenticatedUser();
        if (userId == null || !userId.equals(authenticated.getId())) {
            throw new AccessDeniedException("You may only access your own orders");
        }
    }
}
