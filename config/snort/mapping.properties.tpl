#############################################################################
# DECOMAP / SNORT MAPPING CONFIGURATION 									#
#############################################################################
# This is the configuration file for the mapping of metadata:event based	#
# snort modules. For information on each entry see the commentary.			#
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


# Mapping between snort-events and if-map-events.
# Insert a snort-events into one if-map-event entry. The Event-Type of this snort 
# event will be mapped as the selected if-map-event.
# Additionaly, take notice that all events which are not defined
# will automatically be assigned to ifmap-event-type "other" by the
# ifmap-client! 
eventmapping.p2p=
eventmapping.cve=
eventmapping.botnet_infection=
eventmapping.worm_infection=
eventmapping.excessive_flows=
eventmapping.behavioral_change=
eventmapping.policy_violation=policy-violation,Potential Corporate Privacy Violation,sdf,Sensitive Data was Transmitted Across the Network,inappropriate-content,Inappropriate Content was Detected,Attempted Information Leak,Attempted Denial of Service
eventmapping.other=icmp-event,Generic ICMP event,default-login-attempt,Attempt to Login By a Default Username and Password,misc-attack,Misc Attack,misc-activity,Misc activity,web-application-attack,Web Application Attack,web-application-activity,Access to a Potentially Vulnerable Web Application,protocol-command-decode,Generic Protocol Command Decode,non-standard-protocol,Detection of a Non-Standard Protocol or Event,denial-of-service,Detection of a Denial of Service Attack,network-scan,Detection of a Network Scan,unusual-client-port-connection,A Client was Using an Unusual,trojan-activity,A Network Trojan was Detected,tcp-connection,A TCP Connection was Detected,system-call-detect,A System Call was Detected,suspicious-login,An Attempted Login Using a Suspicious Username was Detected,suspicious-filename-detect,A Suspicious Filename was Detected,string-detect,A Suspicious String was Detected,shellcode-detect,Executable Code was Detected,rpc-portmap-decode,Decode of an RPC Query,successful-admin,Successful Administrator Privilege Gain,attempted-admin,Attempted Administrator Privilege Gain,successful-user,Successful User Privilege Gain,unsuccessful-user,Unsuccessful User Privilege Gain,attempted-user,Attempted User Privilege Gain,successful-dos,Denial of Service,attempted-dos,successful-recon-largescale,Large Scale Information Leak,successful-recon-limited,Information Leak,attempted-recon,bad-unknown,Potentially Bad Traffic,unknown,Unknown Traffic,not-suspicious,Not Suspicious Traffic

# IF-MAP-Events to send to server 
# Only with true marked event-types gone be send to the map-server.
# The rest is skipped
eventlog.p2p=true
eventlog.cve=true
eventlog.botnet_infection=true
eventlog.behavioral_change=true
eventlog.excessive_flows=true
eventlog.other=true
eventlog.policy_violation=true
eventlog.worm_infection=true

# Mapping between snort-priorities and if-map-significance
# Take note, that not registered snort-priorities are mapped as informational
significancemapping.critical=1
significancemapping.important=2
significancemapping.informational=3,4,0