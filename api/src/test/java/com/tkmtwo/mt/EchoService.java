package com.tkmtwo.mt;

import org.springframework.security.access.prepost.PreAuthorize;

public interface EchoService {
  
  String sayHello();

  @PreAuthorize("hasRole('MT_' + #tenant.getId() + '_USER')")
  String sayHello(Tenant tenant);
  
}

