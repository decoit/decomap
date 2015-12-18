#########################################################################
# DECOMAP / LDAP FILE-POLLING CONFIGURATION 							#
#########################################################################	
# This is the configuration file contains all settings needed for a		#
# functional openvpn polling over a log-file							#
#########################################################################

# flag for activating daily-log-rotate mode
# fallback/default: false
logrotate=false

# date-pattern used in log-filename when daily-log-rotate is active
# set a $ into the file path, which is replaced be the now date in the given rotate pattern format
rotatepattern=yyyy-MM-dd

# file-path to log-file
#
# if log-rotate is activated, you can use the "$"-char to mark the 
# place where the rotate-pattern is inserted into the filename
filepath=/var/log/syslog

# perform file-exists check before starting polling-thread
# fallback/default: false
precheck=false