/*
 * Copyright 2014 Accenture
 */
package com.tkmtwo.mt;

/**
 * Things we want to know about a Tenant.
 *
 * @param <T> - The datatype of the IDs.
 */
public interface Tenant<T> {
  T getId();
  String getName();
}
