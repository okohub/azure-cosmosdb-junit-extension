package io.github.okohub.azure.cosmosdb.junit.sync;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * @author onurozcan
 */
final class Partitioner<T> extends AbstractList<List<T>> {

  private final List<T> list;

  private final int chunkSize;

  private Partitioner(List<T> list, int chunkSize) {
    this.list = new ArrayList<>(list);
    this.chunkSize = chunkSize;
  }

  static <T> Partitioner<T> ofSize(List<T> list, int chunkSize) {
    return new Partitioner<>(list, chunkSize);
  }

  @Override
  public List<T> get(int index) {
    int start = index * chunkSize;
    int end = Math.min(start + chunkSize, list.size());

    if (start > end) {
      throw new IndexOutOfBoundsException("Index " + index + " is out of the list range <0," + (size() - 1) + ">");
    }

    return new ArrayList<>(list.subList(start, end));
  }

  @Override
  public int size() {
    return (int) Math.ceil((double) list.size() / (double) chunkSize);
  }
}
