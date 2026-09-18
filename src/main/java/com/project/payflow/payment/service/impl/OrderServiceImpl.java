package com.project.payflow.payment.service.impl;

import com.project.payflow.common.enums.EventAggregateType;
import com.project.payflow.common.enums.OrderStatus;
import com.project.payflow.common.exception.BusinessRuleViolationException;
import com.project.payflow.common.exception.DuplicateResourceException;
import com.project.payflow.common.exception.ResourceNotFoundException;
import com.project.payflow.merchant.service.CustomerService;
import com.project.payflow.payment.dto.request.CreateOrderRequest;
import com.project.payflow.payment.dto.response.OrderResponse;
import com.project.payflow.payment.dto.response.PaymentResponse;
import com.project.payflow.payment.entity.OrderRecord;
import com.project.payflow.payment.entity.Payment;
import com.project.payflow.payment.mapper.OrderMapper;
import com.project.payflow.payment.mapper.PaymentMapper;
import com.project.payflow.payment.outbox.OutboxEventPublisher;
import com.project.payflow.payment.repository.OrderRepository;
import com.project.payflow.payment.repository.PaymentRepository;
import com.project.payflow.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CustomerService customerService;
    private final OutboxEventPublisher eventPublisher;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;

    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {

        if(request.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId, request.receipt())){
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order with receipt already exists: "+request.receipt());
        }

        UUID customerId = null;
        if(request.customer() != null){
            customerId = customerService.findOrCreate(merchantId,
                    request.customer().email(),
                    request.customer().name(),
                    request.customer().phone()
            );
        }

        OrderRecord order = OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())

                .merchantId(merchantId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt() != null ? request.expiresAt() :
                        LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order = orderRepository.save(order);

    eventPublisher.publish(EventAggregateType.ORDER, order.getId(), "ORDER_CREATED",
            Map.of( "orderId", order.getId().toString(),
                    "merchantId", merchantId.toString(),
                    "OrderStatus", order.getOrderStatus().name(),
                    "amountUnits", order.getAmount().getAmountUnits(),
                    "amountCurrency", order.getAmount().getCurrency()
                    )
            );

//        return new OrderResponse(order.getId(),
//                order.getMerchantId(),
//                order.getReceipt(), order.getAmount(),
//                order.getOrderStatus(), order.getAttempts(),
//                order.getNotes(), order.getExpiresAt(),
//                null);
        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        OrderRecord order=  orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

//        return new OrderResponse(order.getId(), order.getMerchantId(),
//                order.getReceipt(), order.getAmount(), order.getOrderStatus(),
//                order.getAttempts(), order.getNotes(), order.getExpiresAt(), null);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {

        OrderRecord order=  orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if(order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.PAID){
              throw new BusinessRuleViolationException("ORDER_CANNOT_CANCEL",
                      "Cannot cancel order with status: "+order.getOrderStatus().name());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        eventPublisher.publish(EventAggregateType.ORDER, order.getId(), "ORDER_CANCELLED",
                Map.of( "orderId", order.getId().toString(),
                        "merchantId", merchantId.toString(),
                        "OrderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getAmount().getAmountUnits(),
                        "amountCurrency", order.getAmount().getCurrency()
                )
        );

//        return new OrderResponse(order.getId(), order.getMerchantId(),
//                order.getReceipt(), order.getAmount(), order.getOrderStatus(),
//                order.getAttempts(), order.getNotes(), order.getExpiresAt(), null);

        return orderMapper.toResponse(order);
    }

    @Override
    public List<PaymentResponse> listPayments(UUID merchantId, UUID orderId) {

        OrderRecord order = orderRepository.findByIdAndMerchantId(orderId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        List<Payment>  paymentList = paymentRepository.findByOrder_Id(order);

//        return paymentList.stream().map(
//                payment -> paymentMapper.toResponse(payment)
//        ).collect(Collectors.toList());

        return paymentMapper.toResponseList(paymentList);
    }
}
