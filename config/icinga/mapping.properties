#############################################################################
# DECOMAP / ICINGA MAPPING CONFIGURATION									#
#############################################################################
# This is the configuration file for the mapping of metadata:event based	#
# icinga modules. For information on each entry see the commentary.			#
#############################################################################

# default if-map-event-type to use for mapping
# possible values are: behavioral-change, cve, botnet-infection, excessive-flows, other, p2p, policy-violation, worm-infection
# fallback/default: behavioral-change
eventmapping.eventtype=behavioral-change

# Default description for the event-type other. Should describe the event type.
# Only needed If the if-map-event-type is set on other
eventmapping.othertypdef=unknown

# magnitude default value
# fallback/default: 100
eventmapping.magnitude=100

# significance default value
# possible values: informational, important, critical
# fallback/default: informational
eventmapping.significance=informational

# confidence default value
# fallback/default: 100
eventmapping.confidence=100

# The publish type for events. 
# true: all publishes to the map-server are made as update
# false: all publishes to the map-server are made as notify
# fallback/default: false
publish.update=false

# DiscovereyBy Link between the device and all IPs on which an event was discovered by this device
# true: send link
# false: don't send link
# fallback/default: false
publish.discoverdby=false