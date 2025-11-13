package ru.yandex.practicum.commerce.interaction.client.shopping.cart.exception;

public class ProductInShoppingCartNotInWarehouse extends RuntimeException {
  public ProductInShoppingCartNotInWarehouse(String message) {
    super(message);
  }
}
