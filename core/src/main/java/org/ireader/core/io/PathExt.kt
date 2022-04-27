package org.ireader.core.io


import okio.Path


val Path.nameWithoutExtension
  get() = name.substringBeforeLast(".")

val Path.extension
  get() = name.substringAfterLast(".")


fun Path.setLastModified(epoch: Long) {
  toFile().setLastModified(epoch)
}