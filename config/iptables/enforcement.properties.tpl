#############################################################################
# DECOMAP / IP-TABLES ENFORCEMENT CONFIGURATION								#
#############################################################################
# This is the configuration file for the enforcement rules for the 			#
# ip-tables module															#
#############################################################################


######################################################################## 
# 2) ENFORCEMENT-CHECK-CONFIGURATION                                   #
########################################################################
# Matcher for reasons to enforce ip-addresses
#
# Look for Strings that either matches exactly (m) a predefined string
# or that contains a predefined string (c)
#
# Separate each filter by ,
#
# check the table below for details on how to define rules
#                                          
# +-------------------------------------------------------------------------------------------------------------+           
# | syntax for defining rules            : (metadata-type)_(attribute-name)_(filter-rule)[(filter-string)]    	|
# |	# note that everything between () should be replaced by the wished values								  	|
# | metadata-type 						 : type of the metadata. For example: event			     				|
# | attribute-name						 : Attribute name from the given metadata type. For example: name 		|
# | filter-rules					     : c=contains or m=matches                        						|
# | filter-value:                        : value to search for                             						|
# +-------------------------------------------------------------------------------------------------------------+
resultprocessor=event_name_c[portscan],event_name_c[Portscan],event_name_m[Portscan]


######################################################################## 
# 2) ALLOWANCE-CHECK-CONFIGURATION                                     #
########################################################################

# mapping of attributes, that leads to an allowance if containing specified values. 
# Check the table below for details on how to define rules and which event-attributes can be 
# used with which operation.
#
# Separate each filter by ,
#
# check the table below for details on how to define rules
#
# +-------------------------------------------------------------------------------------------------------------+           
# | syntax for defining rules            : (metadata-type)_(attribute-name)_(filter-rule)[(filter-string)]    	|
# |	# note that everything between () should be replaced by the wished values								  	|
# | metadata-type 						 : type of the metadata. For example: event			     				|
# | attribute-name						 : Attribute name from the given metadata type. For example: name 		|
# | filter-rules					     : c=contains or m=matches                        						|
# | filter-value                         : value to search for                             						|
# | example								 : role_name_c[financ],role_name_m[employee]                          	|
# +-------------------------------------------------------------------------------------------------------------+
allowprocessor=

# startscript which is started once at client start. Full path!
startscript=initialize_rules.sh
