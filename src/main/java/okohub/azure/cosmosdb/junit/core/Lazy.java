package okohub.azure.cosmosdb.junit.core;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author onurozcan
 */
public class Lazy<T> implements Supplier<T> {

  private final Supplier<T> delegate;

  private T instance;

  public Lazy(Supplier<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public T get() {
    if (Objects.isNull(instance)) {
      instance = delegate.get();
    }
    return instance;
  }
}
