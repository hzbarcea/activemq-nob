/**
 */
package org.apache.activemq.nob.supervisor;



/**
 * JAX-RS ControlCenter root resource
 */
public final class SupervisorConstants {

	public static final String STATUS_UNKNOWN = "UNKNOWN";
	public static final String STATUS_NEW = "NEW";
	public static final String STATUS_COMMISSIONED = "COMMISSIONED";
	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DECOMISSIONED = "DECOMISSIONED";

	private SupervisorConstants() {
		// Utility
	}
}
