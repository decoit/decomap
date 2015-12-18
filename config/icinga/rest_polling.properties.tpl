#############################################################################
# DECOMAP / ICINGA REST CONNECTION CONFIGURATION							#
#############################################################################
# This is the configuration file with all configurations needed for a		#
# functional polling from icinga over rest									#
#############################################################################

# Server ip address
icingaserver.ip=10.10.100.28

# Filepath to .cgi files
icingaserver.filepath=/cgi-bin/icinga/

# Username
icingaserver.username=icingaadmin

# Password
icingaserver.password=icinga

# Maximal number of events polled per poll.
# 0 stands for limit
# fallback/default: 50
icingaserver.lognumber=50
