In order to run the project tests, you must first perform the following...

1.  Install the GemFire 7.x distribution.
2.  Set GEMFIRE_HOME to the installation directory.
3.  Launch gfsh.
4.  Start a GemFire Locator using the following Gfsh command...

gfsh> start locator --name=locatorX --port=11235 --log-level=config --bind-address=10.113.227.77 --J=-Dgemfire.jmx-manager-http-port=9090

The --bind-address option must be set if on a wireless/VPN network, set to the wireless/VPN NIC.

The --J=-Dgemfire.jmx-manager-http-port option is optional and need not be set.
