# !/bin/bash
# /usr/lib/nagios/plugins/eventhandler/helloservice.sh

# incomming macros (in-order):
# $SERVICESTATE$ $SERVICESTATETYPE$ $SERVICEATTEMPT$ $SERVICEDESC$ $SERVICELATENCY$ 
# $SERVICEEXECUTIONTIME$ $SERVICEDURATION$ $SERVICEDOWNTIME$ $HOSTADDRESS$ 

# varaibles for params
SERVICESTATE=$1
SERVICESTATETYPE=$2
SERVICEATTEMPT=$3
SERVICEDESC=$4
SERVICELATENCY=$5
SERVICEEXECUTIONTIME=$6
SERVICEDURATION="$7 $8 $9 $10"
SERVICEDOWNTIME=$11
HOSTADDRESS=$12

# target address of IfMapNagios-Client
TARGETADDRESS='10.10.100.22'
TARGETPORT=6666

# logging
ENBLOG=1
CURTIME=$(date +"%Y/%m/%d-%T")
LOGFILEPATH='/usr/lib/nagios/plugins/eventhandler/out-service.log'

# output macros to log file
log()
{
	# open/create file
	touch $LOGFILEPATH

	# log makro-data
	echo  "** SERVICE HAS FIRED EVENT AT: $CURTIME " >> $LOGFILEPATH
	echo  "Servicestate    : $SERVICESTATE" >> $LOGFILEPATH
	echo  "Servicestatetype: $SERVICESTATETYPE" >> $LOGFILEPATH
	echo  "Serviceattempt  : $SERVICEATTEMPT" >> $LOGFILEPATH
	echo  "Servicedesc     : $SERVICEDESC" >> $LOGFILEPATH
	echo  "Servicelatency  : $SERVICELATENCY" >> $LOGFILEPATH
	echo  "Service X-Time  : $SERVICEEXECUTIONTIME" >> $LOGFILEPATH
	echo  "Serviceduration : $SERVICEDURATION" >> $LOGFILEPATH
	echo  "Servicedowntime : $SERVICEDOWNTIME" >> $LOGFILEPATH
	echo  "Hostaddress     : $HOSTADDRESS" >> $LOGFILEPATH
	
	# log string that was send to server 
	echo "-> Line Send to Server $TARGETADDRESS : $TARGETPORT at $CURTIME" >> "$LOGFILEPATH"
	echo "$OUTMSG" >> "$LOGFILEPATH"
	echo  "---> END OF LOG!\n" >> "$LOGFILEPATH"
}


########## MAIN ##########

# switch http-service state (doesnt has any real use a.tm.)
case "$1" in
OK)
	# service available again
	echo  "-> SERVICE IS AVAILABLE AGAIN " 
	;;
WARNING)
	# service warning
	echo  "-> SERVICE IN AN ALERTING STATE! " 
	;;
UNKNOWN)
	# unknown state
	echo  "-> SERVICE IN AN UNKNOWN STATE! " 
	;;
CRITICAL)
	# critical state
	echo  "-> SERVICE IN AN CRITICAL STATE! " 
	;;
esac

# build output string that will be send to server
OUTMSG="source=service;timestamp=$CURTIME;address=$HOSTADDRESS;state=$SERVICESTATE;statetype=$SERVICESTATETYPE;attempt=$SERVICEATTEMPT;description=$SERVICEDESC;latency=$SERVICELATENCY;executiontime=$SERVICEEXECUTIONTIME;duration=$SERVICEDURATION;downtime=$SERVICEDOWNTIME;"

# call loging method if logging is activated
if [ $ENBLOG = 1 ]
  then log $SERVICESTATE $SERVICESTATETYPE $SERVICEATTEMPT $SERVICEDESC $SERVICELATENCY $SERVICEEXECUTIONTIME $SERVICEDURATION $SERVICEDOWNTIME $CURTIME $HOSTADDRESS $LOGFILEPATH $OUTMSG
fi

# send macros to server
echo $OUTMSG | netcat $TARGETADDRESS $TARGETPORT -q10

# bye bye
exit 0


