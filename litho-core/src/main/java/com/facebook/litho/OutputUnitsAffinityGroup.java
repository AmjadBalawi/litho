/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.facebook.litho;

/**
 * A family of output units ({@link LayoutOutput}s or {@link MountItem}s) generated for the same
 * {@link Component}. Used by {@link LayoutState}, {@link MountState} and {@link TransitionManager}
 * to group items subjected to same {@link Transition} set to their originative {@link Component}
 */
public class OutputUnitsAffinityGroup<T> {
  private final Object[] mContent = new Object[5];
  private short mSize = 0;

  public void add(@OutputUnitType int type, T value) {
    if (value == null) {
      throw new IllegalArgumentException("value should not be null");
    }

    if (mContent[type] != null) {
      throw new RuntimeException("Already contains unit for type " + typeToString(type));
    }

    if ((mContent[OutputUnitType.HOST] != null) || (type == OutputUnitType.HOST && mSize > 0)) {
      throw new RuntimeException(
          "OutputUnitType.HOST unit should be the only member of an OutputUnitsAffinityGroup");
    }

    mContent[type] = value;
    mSize++;
  }

  public void replace(@OutputUnitType int type, T value) {
    if (value != null && mContent[type] != null) {
      mContent[type] = value;
    } else if (value != null && mContent[type] == null) {
      add(type, value);
    } else if (value == null && mContent[type] != null) {
      mContent[type] = null;
      mSize--;
    }
  }

  public T get(@OutputUnitType int type) {
    return (T) mContent[type];
  }

  public int size() {
    return mSize;
  }

  public boolean isEmpty() {
    return mSize == 0;
  }

  public @OutputUnitType int typeAt(int index) {
    if (index < 0 || index >= mSize) {
      throw new IndexOutOfBoundsException("index=" + index + ", size=" + mSize);
    }
    int i = 0, j = 0;
    while (j <= index) {
      if (mContent[i] != null) {
        j++;
      }
      i++;
    }
    return i - 1;
  }

  public T getAt(int index) {
    return get(typeAt(index));
  }

  public T getMostSignificantUnit() {
    if (mContent[OutputUnitType.HOST] != null) {
      return get(OutputUnitType.HOST);
    } else if (mContent[OutputUnitType.CONTENT] != null) {
      return get(OutputUnitType.CONTENT);
    } else if (mContent[OutputUnitType.BACKGROUND] != null) {
      return get(OutputUnitType.BACKGROUND);
    } else if (mContent[OutputUnitType.FOREGROUND] != null) {
      return get(OutputUnitType.FOREGROUND);
    } else {
      return get(OutputUnitType.BORDER);
    }
  }

  public void clean() {
    for (int i = 0; i < mContent.length; i++) {
      mContent[i] = null;
    }
    mSize = 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OutputUnitsAffinityGroup<?> that = (OutputUnitsAffinityGroup<?>) o;
    if (mSize != that.mSize) {
      return false;
    }
    for (int i = 0; i < mContent.length; i++) {
      if (mContent[i] != that.mContent[i]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    for (int index = 0; index < size(); ++index) {
      final @OutputUnitType int type = typeAt(index);
      final T unit = getAt(index);
      sb.append("\n\t").append(typeToString(type)).append(": ").append(unit.toString());
    }
    return sb.toString();
  }

  private static String typeToString(@OutputUnitType int type) {
    switch (type) {
      case OutputUnitType.CONTENT:
        return "CONTENT";

      case OutputUnitType.BACKGROUND:
        return "BACKGROUND";

      case OutputUnitType.FOREGROUND:
        return "FOREGROUND";

      case OutputUnitType.HOST:
        return "HOST";

      case OutputUnitType.BORDER:
        return "BORDER";

      default:
        return null;
    }
  }
}
