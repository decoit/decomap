# Application version
application.version=0.2

# polling intervall in seconds
application.polling.interval=20

# application component. At the moment possible: 
# SNORT_ASCIILOG
# SNORT_AD_FILE
# NAGIOS_SOCKET
# OPENVPN_FILE
# IPTABLES_FILE
# RADIUS_FILE
# LDAP_FILE
# ICINGA_REST 
application.component=LDAP_FILE

# if false, sending old events.
# If true, sending only events after the client start
# Not working for all components!
application.messaging.sendold=false

# client ip-address
application.ipaddress=10.10.100.28

# is this client a service
application.isservice=true


# Only if client is service:

# service port
application.serviceport=389

# service type
application.servicetype=ldap

# service name
application.servicename=ldap

# administrative domain
application.administrativdomain=ldapDomain
#############Only if client is service end###################

# polling configuration file path
application.pollingconfig.path=config/ldap/rest_polling.properties

# mapping configuration file path
application.mappingconfig.path=config/ldap/mapping.properties

# regex configuration file path
application.regexconfig.path=config/ldap/regex.properties

# polling-result-filter confuguration file path
application.pollresultfilterconfig.path=config/iptables/enforcement.properties	

########################################

# Map-server url
mapserver.url=https://10.10.100.11:8443/

# Map-server keystore path
mapserver.keystore.path=/keystore/iptablesmap.jks

# Map-server keystore password
mapserver.keystore.password=iptablesmap

# Map-server truststore path
mapserver.truststore.path=/keystore/iptablesmap.jks

# Map-server truststore password
mapserver.truststore.password=iptablesmap

# Having map-service authentication
mapserver.basicauth.enabled=true

# Map-server username
# Only if map-server authentication is true
mapserver.basicauth.user=test

# Map-server password
# Only if map-server authentication is true
mapserver.basicauth.password=test


