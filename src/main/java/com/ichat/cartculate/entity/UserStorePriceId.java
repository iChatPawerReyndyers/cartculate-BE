package com.ichat.cartculate.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStorePriceId implements Serializable {
    private Long userId;
    private Long itemId;
    private Long storeId;
}
