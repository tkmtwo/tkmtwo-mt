package com.tkmtwo.mt;

import static com.google.common.base.Preconditions.checkNotNull;
import org.springframework.security.access.prepost.PreAuthorize;

public final class EchoServiceImpl
  implements EchoService {
  
  public String sayHello() {
    return "Hello from unsecured/no tenant.";
  }
  

  public String sayHello(Tenant tenant) {
    checkNotNull(tenant, "Need a tenant.");
    return "Hello from secured " + tenant.getId();
  }
  
}

