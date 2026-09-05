package com.dailycodework.buynowdotcom.controller;

import com.dailycodework.buynowdotcom.dto.CartDto;
import com.dailycodework.buynowdotcom.model.Cart;
import com.dailycodework.buynowdotcom.response.ApiResponse;
import com.dailycodework.buynowdotcom.service.cart.ICartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("${api.prefix}/carts")
@RequiredArgsConstructor
public class CartController {
    private final ICartService cartService;
    private final ModelMapper modelMapper;

    @GetMapping("/{cartId}")
    public ResponseEntity<ApiResponse> getCart(@PathVariable Long cartId) {
        Cart cart = cartService.getCart(cartId);
        return ResponseEntity.ok(new ApiResponse("Success", modelMapper.map(cart, CartDto.class)));
    }

    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok(new ApiResponse("Cart cleared", null));
    }

    @GetMapping("/{cartId}/total-price")
    public ResponseEntity<ApiResponse> getTotalAmount(@PathVariable Long cartId) {
        BigDecimal total = cartService.getTotalPrice(cartId);
        return ResponseEntity.ok(new ApiResponse("Total price", total));
    }
}