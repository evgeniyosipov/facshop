package ru.evgeniyosipov.facshop.store.handlers;

import ru.evgeniyosipov.facshop.events.OrderEvent;

public interface IOrderHandler {

    public void onNewOrder(OrderEvent event);

}
