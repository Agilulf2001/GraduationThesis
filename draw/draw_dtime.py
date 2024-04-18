import sys
sys.path.append('/home/agi/Dune/simulation/draw')

rowsOneIv = 11
flnStr = str(sys.argv[1]) # "todraw/*dtime*"
flnStr2 = str(sys.argv[2])
#此图展示ost的accurate与rough准确率随dtime变化而变化, flow源数据采用norm100, dtime从10到100, 实验次数为100
#./rruncmd.sh 100 "java -jar SimulationEx.jar --fin input/infile_Active_norm100 --flowNum 300 --pps 200 -c 8 -o 0.37" "dtime" "--dtime 10" "--dtime 15" "--dtime 20" "--dtime 25" "--dtime 30" "--dtime 35" "--dtime 40" "--dtime 45" "--dtime 50" "--dtime 55" "--dtime 60" "--dtime 65" "--dtime 70" "--dtime 75" "--dtime 80" "--dtime 85" "--dtime 90" "--dtime 95" "--dtime 100"
indVarStr = "dtime"

fl = open(flnStr, "r")
lines = sum(1 for _ in fl)
fl.seek(0, 0)
indVarNum = lines//rowsOneIv

xval = []
yval = [] # indVarNum * (rowsOneIv-1)
for i in range(0, indVarNum):
    str = fl.readline()
    beg = str.find(indVarStr) + len(indVarStr) + 1
    end = str.find(']')
    xval.append(str[beg:end])
    tmpy = []
    for j in range(1, rowsOneIv):
        str = fl.readline()
        beg = str.find(':') + 2
        tmpy.append(float(str[beg:]))
    yval.append(tmpy)
fl.close()

fl = open(flnStr2, "r")
fl.seek(0, 0)
# xval2 = []
yval2 = [] # indVarNum * (rowsOneIv-1)
for i in range(0, indVarNum):
    str = fl.readline()
    # beg = str.find(indVarStr) + len(indVarStr) + 1
    # end = str.find(']')
    # xval2.append(str[beg:end])
    tmpy = []
    for j in range(1, rowsOneIv):
        str = fl.readline()
        beg = str.find(':') + 2
        tmpy.append(float(str[beg:]))
    yval2.append(tmpy)
fl.close()

import matplotlib.pyplot as plt
item = 1   
item2 = 0
yitem = []
yitem2 = []
for tmpy in yval:
    yitem.append(tmpy[item])
    yitem2.append(tmpy[item2])

yitem3 = []
yitem4 = []
for tmpy in yval2:
    yitem3.append(tmpy[item])
    yitem4.append(tmpy[item2])

plt.plot(xval, yitem, marker='.', linewidth=1.0)
plt.plot(xval, yitem2, marker='.', linewidth=1.0, color='purple')
plt.plot(xval, yitem3, marker='*', linewidth=1.0, color='green')
plt.plot(xval, yitem4, marker='*', linewidth=1.0, color='orange')
plt.legend(labels=('accurate ost', 'rough ost', 'accurate msk', 'rough msk'), loc='lower right')

ytick = []
for i in range(50, 101, 2):
    ytick.append(i*0.01)

plt.yticks(ytick)
plt.xlabel('Run Time (million time units)')
plt.ylabel('Flowsize accuracy')
# plt.rcParams['figure.dpi'] = 300
# plt.show() # show() or save
plt.tight_layout()
plt.rcParams['savefig.dpi'] = 1000
plt.savefig(indVarStr+'.png')