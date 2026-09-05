package com.dailycodework.buynowdotcom.service.order;

import com.dailycodework.buynowdotcom.dto.OrderDto;
import com.dailycodework.buynowdotcom.model.Order;
import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    OrderDto getOrder(Long orderId);
    List<OrderDto> getUserOrders(Long userId);
}