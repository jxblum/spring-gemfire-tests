
package org.lab.tests;

public class HelloWorld {

  protected static final String DEFAULT_GREETING = "World";

  public static void main(final String[] args) {
    System.out.printf("Hello %1$s!%n", getGreeting(args));
  }

  private static String getGreeting(final String[] args) {
    return getGreeting(args, DEFAULT_GREETING);
  }

  private static String getGreeting(final String[] args, final String defaultGreeting) {
    return (args != null && args.length > 0 ? args[0] : defaultGreeting);
  }

}
