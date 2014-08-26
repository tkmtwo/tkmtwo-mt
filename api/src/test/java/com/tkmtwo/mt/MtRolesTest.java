/*
 * Copyright 2014 Accenture
 */
package com.tkmtwo.mt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * Test using role names with Tenant IDs in them.
 *
 * See EchoService.java for the method-level access expressions.
 *
 */
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class MtRolesTest
  extends AbstractJUnit4SpringContextTests {

  @Autowired
  private AuthenticationProvider authenticationProvider;
  
  @Autowired
  private EchoService echoService;
  
  private Tenant tenantOne;
  private Tenant tenantTwo;
  
  
  @Before
  public void setUp() {
    tenantOne = new TenantImpl("TenantOne", "Tenant One");
    tenantTwo = new TenantImpl("TenantTwo", "Tenant Two");
  }

  
  private void login(String uname, String passwd)
  {
    Authentication auth =
      new UsernamePasswordAuthenticationToken(uname, passwd);
    auth = authenticationProvider.authenticate(auth);

    SecurityContext securityContext = SecurityContextHolder.getContext();
    securityContext.setAuthentication(auth);
  }
  private String getPrincipal() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    if (securityContext == null) { return null; }
    
    Authentication authentication = securityContext.getAuthentication();
    if (authentication == null) { return null; }
    
    Object principal = authentication.getPrincipal();
    if (principal == null) { return null; }

    String userName = null;
    if (principal instanceof UserDetails) {
      userName = ((UserDetails)principal).getUsername();
    } else {
      userName = principal.toString();
    }
    return userName;
    
    //return authentication.getPrincipal().toString();
  }

    
  private void logout()
  {
    SecurityContextHolder.clearContext();
  }


  
  /*
   * Just confess our two tenants
   */
  @Test
  public void test00ConfessTestData() {
    System.out.println();
    System.out.println("Test Data");
    System.out.println("TenantOne is: " + tenantOne.toString());
    System.out.println("TenantTwo is: " + tenantTwo.toString());
    System.out.println();
    confessLogin("user.one", "password");
    confessLogin("user.two", "password");
    confessLogin("user.both", "password");
  }

  public void confessLogin(String uname, String passwd) {
    login(uname, passwd);

    System.out.println();
    System.out.println("Uname is    : " + uname);
    System.out.println("Principal is: " + getPrincipal());
    for (GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
      System.out.println(String.format("  Principal %-20s has authority %-20s", getPrincipal(), ga.getAuthority()));
    }
    logout();
  }



  /*
   * Ensure that sayHello() is open to public and sayHello(Tenant t) is not
   */
  @Test(expected = AuthenticationCredentialsNotFoundException.class)
  public void test10SecuredAndNot() {
    
    //Calling with no tenant should be ok...
    assertTrue(echoService.sayHello().startsWith("Hello"));

    //Calling with a tenant should NOT be ok since we didn't log in
    assertTrue(echoService.sayHello(tenantOne).startsWith("Hello"));
    fail("Should not have allowed without being logged in.");
  }
  
  
  /*
   * sayHello() should be open, sayHello(null) should throw and IllegalArgumentException
   */  
  @Test
  public void test11NullTenant() {
    Tenant tenantNull = null;

    login("user.one", "password");
    assertEquals("Hello from unsecured/no tenant.",
                 echoService.sayHello());
    try {
      assertTrue(echoService.sayHello(tenantNull).startsWith("Hello"));
      fail("Should not have allowed to call sayHello(null)");
    } catch (IllegalArgumentException iae) {
      ;
    } catch (Throwable t) {
      fail("Should have thrown and IllegalArgumentException, not a " + t.getClass().getName());
    } finally {
      logout();
    }
  }
  
  
  
  
  
  

  
  /*
   * Ensure user.both can call sayHello() and sayHello(Tenant t) for both tenants
   */  
  @Test
  public void test20UserBoth() {
    login("user.both", "password");
    
    assertEquals("Hello from unsecured/no tenant.",
                 echoService.sayHello());
    assertEquals("Hello from secured TenantOne",
                 echoService.sayHello(tenantOne));
    assertEquals("Hello from secured TenantTwo",
                 echoService.sayHello(tenantTwo));
    logout();
  }

  /*
   * Ensure user.one can call sayHello() and sayHello(Tenant t) for tenantOne but not tenantTwo
   */  
  @Test
  public void test21UserOne() {
    login("user.one", "password");
    
    assertEquals("Hello from unsecured/no tenant.",
                 echoService.sayHello());
    assertEquals("Hello from secured TenantOne",
                 echoService.sayHello(tenantOne));
    try {
      assertEquals("Hello from secured TenantTwo",
                   echoService.sayHello(tenantTwo));
      fail("Should not have allowed user.one to access TenantTwo");
    } catch (AccessDeniedException adex) {
      ; //do nothing
    } finally {
      logout();
    }
  }

  /*
   * Ensure user.two can call sayHello() and sayHello(Tenant t) for tenantTwo but not tenantOne
   */  
  @Test
  public void test22UserTwo() {
    login("user.two", "password");
    
    assertEquals("Hello from unsecured/no tenant.",
                 echoService.sayHello());
    assertEquals("Hello from secured TenantTwo",
                 echoService.sayHello(tenantTwo));
    try {
      assertEquals("Hello from secured TenantOne",
                   echoService.sayHello(tenantOne));
      fail("Should not have allowed user.two to access TenantOne");
    } catch (AccessDeniedException adex) {
      ; //do nothing
    } finally {
      logout();
    }
  }

  
  
  
}
