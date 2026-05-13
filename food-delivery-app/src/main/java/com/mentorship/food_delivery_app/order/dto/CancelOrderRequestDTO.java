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
public class CancelOrderRequestDTO {

    @NotBlank(message = "Cancel reason is required")
    @Size(max = 255, message = "Cancel reason must be at most 255 characters")
    private String reason;
}
