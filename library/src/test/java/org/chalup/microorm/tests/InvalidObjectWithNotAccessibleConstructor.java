package org.chalup.microorm.tests;

import android.provider.BaseColumns;

public class InvalidObjectWithNotAccessibleConstructor {
  private InvalidObjectWithNotAccessibleConstructor() {
  }

  @Column(BaseColumns._ID)
  long id;
}
