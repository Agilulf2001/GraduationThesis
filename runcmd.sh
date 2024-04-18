#!/bin/bash
# run the command several times
# ./runcmd2.sh "java -jar SimulationEx.jar --fin input/infile_norm10 -c 8 -o 0.37 --flowNum 200 --dtime 10 --pps 80" 10

command=$1
looptimes=$2
cnt=$looptimes
aveSum=0
accSum=0
cliSum=0
rAcSum=0
noUSum=0
nTUSum=0
eAcSum=0
mAcSum=0
nAcAcc=0
actAcc=0

eAcCnt=$2
mAcCnt=$2
nAcCnt=$2
actCnt=$2

while(( $looptimes>0 ))
do
    string=$($command)
    aveSum=$(echo "$aveSum ${string:15:17}" |awk '{printf("%.15f",$1+$2)}')
    accSum=$(echo "$accSum ${string:48:17}" |awk '{printf("%.15f",$1+$2)}')
    cliSum=$(echo "$cliSum ${string:81:17}" |awk '{printf("%.15f",$1+$2)}')
    rAcSum=$(echo "$rAcSum ${string:114:17}" |awk '{printf("%017d",$1+$2)}')
    noUSum=$(echo "$noUSum ${string:147:17}" |awk '{printf("%.15f",$1+$2)}')
    nTUSum=$(echo "$nTUSum ${string:180:17}" |awk '{printf("%.15f",$1+$2)}')
    if test ${string:213:1} = "-"
    then
        (( --eAcCnt ))
    else
        eAcSum=$(echo "$eAcSum ${string:213:17}" |awk '{printf("%.15f",$1+$2)}')
    fi
    if test ${string:246:1} = "-"
    then
        (( --mAcCnt ))
    else
        mAcSum=$(echo "$mAcSum ${string:246:17}" |awk '{printf("%.15f",$1+$2)}')
    fi
    if test ${string:279:1} = "-"
    then
        (( --nAcCnt ))
    else
        nAcAcc=$(echo "$nAcAcc ${string:279:17}" |awk '{printf("%.15f",$1+$2)}')
    fi
    if test ${string:312:1} = "-"
    then
        (( --actCnt ))
    else
        actAcc=$(echo "$actAcc ${string:312:17}" |awk '{printf("%.15f",$1+$2)}')
    fi
    let "looptimes--"
done

echo "$aveSum $cnt" |awk '{printf("aveRate: %.15f\n",$1/$2)}'
echo "$accSum $cnt" |awk '{printf("accRate: %.15f\n",$1/$2)}'
echo "$cliSum $cnt" |awk '{printf("colRate: %.15f\n",$1/$2)}'
echo "$rAcSum $cnt" |awk '{printf("regAces: %017d\n",$1/$2)}'
echo "$noUSum $cnt" |awk '{printf("NoURate: %.15f\n",$1/$2)}'
echo "$nTUSum $cnt" |awk '{printf("NoTUpdt: %.15f\n",$1/$2)}'
echo "$eAcSum $eAcCnt" |awk '{printf("HipsAcc: %.15f\n",$1/$2)}'
echo "$mAcSum $mAcCnt" |awk '{printf("LopsAcc: %.15f\n",$1/$2)}'
echo "$nAcAcc $nAcCnt" |awk '{printf("NActAcc: %.15f\n",$1/$2)}'
echo "$actAcc $actCnt" |awk '{printf("ActvAcc: %.15f\n",$1/$2)}'