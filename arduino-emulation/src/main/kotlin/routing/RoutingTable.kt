package routing

import util.SimpleLogger

/**
 * A class representing a routing table for a network of four nodes.
 *
 * @author <a href="mailto:tyw1@aber.ac.uk">Tyler Lewis [tyw1@aber.ac.uk]</a>
 */
class RoutingTable {
    /**
     * The routing table, mapping destination routes to device names.
     */
    private val routingTable: MutableMap<Int, String> = run {
        val table = mutableMapOf<Int, String>()
        for (i in 0..3) {
            table[i] = "UNKNOWN"
        }
        table
    }

    /**
     * Get the routing table.
     *
     * @return The routing table.
     */
    fun getTable(): Map<Int, String> {
        return routingTable
    }

    /**
     * Get the device name for a given route.
     *
     * @param route The route to get the device name for.
     * @return The device name for the given route.
     */
    fun getDevice(route: Int): String {
        require(route in 0..3) { "Route must be between 0 and 3" }
        if (routingTable[route] == null) routingTable[route] = "UNKNOWN"
        return routingTable[route] ?: "UNKNOWN"
    }

    /**
     * Set the device name for a given route.
     *
     * @param route The route to set the device name for.
     * @param name The device name to set.
     */
    fun setDevice(route: Int, name: String) {
        require(route in 0..3) { "Route must be between 0 and 3" }
        routingTable[route] = name
    }

    /**
     * Get the route for a given device name.
     *
     * @param name The device name to get the route for.
     * @return The route for the given device name. Returns -1 if the device name is not found.
     */
    fun getRoute(name: String): Int {
        for ((route, device) in routingTable) {
            if (device == name) {
                return route
            }
        }
        return -1
    }

    /**
     * Clear the routing table.
     */
    fun clear() {
        for (i in 0..3) {
            routingTable[i] = "UNKNOWN"
        }
    }

    override fun toString(): String {
        StringBuilder().apply {
            append("Routing Table:\n")
            for ((destination, route) in routingTable) {
                append("- Destination: $destination, Route: $route\n")
            }
            return toString()
        }
    }
}