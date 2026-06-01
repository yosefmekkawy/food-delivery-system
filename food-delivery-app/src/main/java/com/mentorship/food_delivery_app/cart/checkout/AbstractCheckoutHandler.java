package com.mentorship.food_delivery_app.cart.checkout;

public abstract class AbstractCheckoutHandler implements CheckoutHandler {

    private CheckoutHandler next;

    @Override
    public void setNext(CheckoutHandler next) {
        this.next = next;
    }

    protected void proceed(CheckoutContext context) {
        if (next != null) {
            next.handle(context);
        }
    }
}
