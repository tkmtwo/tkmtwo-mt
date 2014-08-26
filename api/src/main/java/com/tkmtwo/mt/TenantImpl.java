/*
 * Copyright 2014 Accenture
 */
package com.tkmtwo.mt;

import com.google.common.base.Objects;

/**
 * Simple impl.
 */
public final class TenantImpl
  implements Tenant<String> {
  
  private String id;
  private String name;
  
  public TenantImpl(String i, String n) {
    setId(i);
    setName(n);
  }
  
  public String getId() { return id; }
  public void setId(String s) { id = s; }
  
  public String getName() { return name; }
  public void setName(String s) { name = s; }
  
  
  @Override
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (!(o instanceof TenantImpl)) { return false; }

    TenantImpl impl = (TenantImpl) o;

    return Objects.equal(getId(), impl.getId());
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("id", getId())
      .add("name", getName())
      .toString();
  }
  
}
