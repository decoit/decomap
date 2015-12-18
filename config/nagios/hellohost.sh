# !/bin/bash
# /usr/lib/nagios/plugins/eventhandler/hellohost.sh

# incomming macros (in-order):
# $HOSTNAME$ $HOSTADDRESS$ $HOSTSTATE$ $HOSTSTATETYPE$ 

# varaibles for params
HOSTNAME=$1
HOSTADDRESS=$2
HOSTSTATE=$3
HOSTSTATETYPE=$4

# target address of IfMapNagios-Client
TARGETADDRESS='10.10.100.22'
TARGETPORT=6666

# logging
ENBLOG=1
CURTIME=$(date +"%Y/%m/%d-%T")
LOGFILEPATH='/usr/lib/nagios/plugins/eventhandler/out-host.log'

# output macros to log file
log()
{
	# open/create file
	touch $LOGFILEPATH

	# log makro-data
	echo  "** HOST HAS FIRED EVENT AT: $CURTIME " >> $LOGFILEPATH
	echo  "Hostname     : $HOSTNAME" >> $LOGFILEPATH
	echo  "Hostaddress  : $HOSTADDRESS" >> $LOGFILEPATH
	echo  "Hoststate    : $HOSTSTATE" >> $LOGFILEPATH
	echo  "hoststatetype: $HOSTSTATETYPE" >> $LOGFILEPATH

	# log string that was send to server 
	echo "-> Line Send to Server $TARGETADDRESS : $TARGETPORT at $CURTIME" >> "$LOGFILEPATH"
	echo "$OUTMSG" >> "$LOGFILEPATH"
	echo  "---> END OF LOG!\n" >> "$LOGFILEPATH"
}

########## MAIN ##########

# switch http-host state (doesnt has any real use a.tm.)
case "$1" in
OK)
	# host available again
	echo  "-> HOST IS AVAILABLE AGAIN"	
	;;
WARNING)
	# host warning
	echo  "-> HOST IN AN ALERTING STATE!"
	;;
UNKNOWN)
	# unknown state
	echo  "-> HOST IN AN UNKNOWN STATE!"
	;;
CRITICAL)
	# critical state
	echo  "-> HOST IN AN CRITICAL STATE!" 
	;;
esac


# build output string that will be send to server
OUTMSG="source=host;timestamp=$CURTIME;address=$HOSTADDRESS;name=$HOSTNAME;state=$HOSTSTATE;statetype=$HOSTSTATETYPE;"

# call loging method if logging is activated
if [ $ENBLOG = 1 ]
  then log $HOSTNAME $HOSTADDRESS $HOSTSTATE $HOSTSTATETYPE $CURTIME $LOGFILEPATH $OUTMSG
fi

# send macros to server
echo "$OUTMSG" | netcat "$TARGETADDRESS" "$TARGETPORT" -q10

# bye bye
exit 0

