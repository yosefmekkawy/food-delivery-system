package com.mentorship.food_delivery_app.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnOrderRequestDTO {

    @NotBlank(message = "Return reason is required")
    @Size(max = 255, message = "Return reason must be at most 255 characters")
    private String reason;
}
