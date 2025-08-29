package com.kltn.event;

import com.kltn.model.Product;
import com.kltn.model.User;
import lombok.*;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class NotificationEvent extends ApplicationEvent {
    private Product product;
    private User user;
    private String message;

    // Constructor chấp nhận tất cả các tham số
    public NotificationEvent(Object source, Product product, User user, String message) {
        super(source); // Đây là tham số bắt buộc đối với constructor của ApplicationEvent
        this.product = product;
        this.user = user;
        this.message = message;
    }
}
