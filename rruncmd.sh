#!/bin/bash
# run several types of commands cnt times and output the result by the date
# ./epr.sh 100 "java -jar SimulationEx.jar --fin input/infile_Active_norm50 --flowNum 200 --dtime 10 --pps 200 -c 8" "mskost" "-m 6 6" "-o 0.37"

paras=()
i=0
for para in "$@"
do
    paras[$i]=$para
    (( ++i ))
done

cnt=${paras[0]}
cmd=${paras[1]}
indval=${paras[2]}
tstmp=$(date)
echo "begin: $tstmp"
flname="out/${tstmp:6:2}${tstmp:10:2}_${tstmp:18:8}_${indval}#${cnt}times"

i=3
while (( i < $# ))
do
    curcmd=$cmd" "${paras[$i]}
    echo "run [$curcmd] $cnt times" >> "$flname"
    echo "$(./runcmd.sh "$curcmd" $cnt)" >> "$flname"
    (( ++i ))
done

echo "done:  $(date)"