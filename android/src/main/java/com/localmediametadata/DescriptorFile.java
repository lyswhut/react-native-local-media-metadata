package com.localmediametadata;

import androidx.annotation.NonNull;

import java.io.File;

class DescriptorFile extends File {
  private final String rawName;
  DescriptorFile(String path, String name) {
    super(path);
    this.rawName = name;
  }

  @NonNull
  @Override
  public String getName() {
    return rawName;
  }
}
