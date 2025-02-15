package com.sports.PaymentDetail;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentDetailDTO {
    private Long paymentId;
    private String itemTitle;
    private Integer itemPrice;
    private Integer itemCount;
    private String itemUrl;
}