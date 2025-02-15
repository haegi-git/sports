package com.sports.Payment;

import com.sports.Cart.Cart;
import com.sports.Cart.CartRepository;
import com.sports.Cart.DTO.CartDTO;
import com.sports.Item.Item;
import com.sports.Item.ItemRepository;
import com.sports.Payment.DTO.PaymentDTO;
import com.sports.PaymentDetail.PaymentDetail;
import com.sports.PaymentDetail.PaymentDetailDTO;
import com.sports.PaymentDetail.PaymentDetailRepository;
import com.sports.user.entito.User;
import com.sports.user.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import com.sports.Payment.DTO.PaymentResponseDTO;
// 기타 필요한 import

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final UserContextService userContextService;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;

    public PaymentResponseDTO processPayment(Long userId, List<CartDTO> cartDTOs, String paymentMethod, String deliveryAddress, String phoneNumber, String name) {
        // 유저 정보와 체크된 장바구니 항목들을 가져옵니다.
        User user = userContextService.findById(userId);

        if (cartDTOs.isEmpty()) {
            throw new RuntimeException("결제할 장바구니 항목이 없습니다.");
        }

        long totalPrice = 0;
        List<PaymentDetail> paymentDetails = new ArrayList<>();

        for (CartDTO cartDTO : cartDTOs) {
            if (cartDTO.isChecked()) {
                PaymentDetail paymentDetail = createPaymentDetail(cartDTO);
                paymentDetails.add(paymentDetail);

                // 총 금액 계산
                totalPrice += (long) cartDTO.getItemPrice() * (long) cartDTO.getCount();
            }
        }

        // Payment 객체 생성 및 설정
        Payment payment = new Payment();
        payment.setPaymentMethod(paymentMethod);
        payment.setUserId(userId);
        payment.setDeliveryAddress(deliveryAddress);
        payment.setPhoneNumber(phoneNumber);
        payment.setName(name);
        payment.setPaymentDetails(paymentDetails);
        payment.setTotalPrice(totalPrice);
        payment.setPaymentWhether(true);

        // Payment 객체 저장
        paymentRepository.save(payment);

        // 결제 상세 항목에 PaymentId를 설정
        for (PaymentDetail paymentDetail : paymentDetails) {
            paymentDetail.setPayment(payment);  // PaymentDetail에 Payment 설정
            // 결제 상세 항목을 저장
            paymentDetailRepository.save(paymentDetail);
        }

        // 재고 업데이트 및 장바구니 결제 상태 변경
        for (CartDTO cartDTO : cartDTOs) {
            if (cartDTO.isChecked()) {
                Long cartId = cartDTO.getCartId();  // CartDTO에서 cartId 가져오기
                Cart cart = cartRepository.findById(cartId)
                        .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다. cartId: " + cartId));

                // 장바구니의 결제 상태를 true로 설정
                cart.setPaymentStatus(true);
                cartRepository.save(cart);
            }
        }

        // 재고 업데이트
        for (PaymentDetail paymentDetail : paymentDetails) {
            Long itemId = paymentDetail.getItemId();
            int itemCount = paymentDetail.getItemCount();

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("아이템을 찾을 수 없습니다. itemId: " + itemId));

            int newStock = item.getStock() - itemCount;

            if (newStock < 0) {
                throw new RuntimeException("재고가 부족합니다: " + item.getTitle());
            }

            item.setStock(newStock);
            itemRepository.save(item);
        }

        // PaymentDTO로 변환 후 PaymentResponseDTO로 래핑
        PaymentDTO paymentDTO = convertToPaymentDTO(payment);
        String paymentStatusMessage = "결제가 성공적으로 완료되었습니다.";  // 메시지 설정

        // PaymentResponseDTO로 반환
        return new PaymentResponseDTO(paymentDTO, paymentStatusMessage);
    }

    private void updateStockAndCartStatus(List<CartDTO> cartDTOs, List<PaymentDetail> paymentDetails) {
        for (CartDTO cartDTO : cartDTOs) {
            if (cartDTO.isChecked()) {
                Long cartId = cartDTO.getCartId();
                Cart cart = cartRepository.findById(cartId)
                        .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다. cartId: " + cartId));
                cart.setPaymentStatus(true);
                cartRepository.save(cart);
            }
        }

        for (PaymentDetail paymentDetail : paymentDetails) {
            Long itemId = paymentDetail.getItemId();
            int itemCount = paymentDetail.getItemCount();
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("아이템을 찾을 수 없습니다. itemId: " + itemId));
            int newStock = item.getStock() - itemCount;
            if (newStock < 0) {
                throw new RuntimeException("재고가 부족합니다: " + item.getTitle());
            }
            item.setStock(newStock);
            itemRepository.save(item);
        }
    }

    private PaymentDTO convertToPaymentDTO(Payment payment) {
        List<PaymentDetailDTO> paymentDetailDTOs = createPaymentDetailDTOs(payment.getPaymentDetails());

        String paymentStatusMessage = payment.isPaymentWhether() ? "결제 완료" : "결제 실패";

        return new PaymentDTO(
                payment.getId(),
                payment.getPaymentMethod(),
                payment.isPaymentWhether(),
                payment.getPaymentTime(),
                payment.getTotalPrice(),
                paymentDetailDTOs,
                paymentStatusMessage,
                payment.getDeliveryAddress(),
                payment.getPhoneNumber(),
                payment.getName()
        );
    }

    private PaymentDetail createPaymentDetail(CartDTO cartDTO) {
        PaymentDetail paymentDetail = new PaymentDetail();
        paymentDetail.setItemTitle(cartDTO.getItemTitle());
        paymentDetail.setItemPrice(cartDTO.getItemPrice());
        paymentDetail.setItemCount(cartDTO.getCount());
        paymentDetail.setItemId(cartDTO.getItemId());
        paymentDetail.setItemUrl(cartDTO.getItemImgUrl());
        return paymentDetail;
    }

    private List<PaymentDetailDTO> createPaymentDetailDTOs(List<PaymentDetail> paymentDetails) {
        List<PaymentDetailDTO> paymentDetailDTOs = new ArrayList<>();
        if (paymentDetails != null) {
            for (PaymentDetail paymentDetail : paymentDetails) {
                if (paymentDetail != null) {
                    PaymentDetailDTO detailDTO = new PaymentDetailDTO(
                            paymentDetail.getPayment().getId(),
                            paymentDetail.getItemTitle(),
                            paymentDetail.getItemPrice(),
                            paymentDetail.getItemCount(),
                            paymentDetail.getItemUrl()
                    );
                    paymentDetailDTOs.add(detailDTO);
                }
            }
        }
        return paymentDetailDTOs;
    }

    public List<PaymentResponseDTO> getPaymentsByUser(Long userId) {
        List<Payment> payments = paymentRepository.findByUserIdWithDetails(userId);

        List<PaymentResponseDTO> paymentResponseDTOs = new ArrayList<>();
        for (Payment payment : payments) {
            PaymentDTO paymentDTO = convertToPaymentDTO(payment);
            String message = "결제 내역 조회 성공";
            paymentResponseDTOs.add(new PaymentResponseDTO(paymentDTO, message));
        }

        return paymentResponseDTOs;
    }
}
