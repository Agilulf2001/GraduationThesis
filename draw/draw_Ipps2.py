import sys
sys.path.append('/home/agi/Dune/simulation')

rowsOneIv = 11
flnStr = str(sys.argv[1]) #"todraw/*Ipps_sIntv*"
#此图展示ost的accurate准确率、活跃流准确率、不活跃流准确率随INT流pps小范围变化而变化, flow源数据采用norm50, pps从50到120步长5, 实验次数为100
#./rruncmd.sh 100 "java -jar SimulationEx.jar --fin input/infile_Active_norm50 --flowNum 300 --dtime 10 -c 8 -o 0.37" "Ipps_sIntv" "--pps 50" "--pps 55" "--pps 60" "--pps 65" "--pps 70" "--pps 75" "--pps 80" "--pps 85" "--pps 90" "--pps 95" "--pps 100" "--pps 105" "--pps 110" "--pps 115" "--pps 120" "--pps 125"
indVarStr = "pps"

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
    xval.append(int(str[beg:end]))
    tmpy = []
    for j in range(1, rowsOneIv):
        str = fl.readline()
        beg = str.find(':') + 2
        tmpy.append(float(str[beg:]))
    yval.append(tmpy)

fl.close()

import matplotlib.pyplot as plt
item = 1
item2 = 8
item3 = 9
yitem = []
yitem2 = []
yitem3 = []
for tmpy in yval:
    yitem.append(tmpy[item])
    yitem2.append(tmpy[item2])
    yitem3.append(tmpy[item3])

plt.plot(xval, yitem, marker='.', linewidth=1.0)
plt.plot(xval, yitem2, marker='.', linewidth=1.0, color='purple')
plt.plot(xval, yitem3, marker='.', linewidth=1.0, color='orange')
plt.legend(labels=('overall', 'inactive', 'active'), loc='lower right')

plt.xticks([50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120, 125])
ytick = []
for i in range(36, 81, 2):
    ytick.append(i*0.01)
plt.yticks(ytick)
plt.xlabel('Packets per second of INT flow')
plt.ylabel('Flowsize accuracy')
# plt.show() # show() or save
plt.tight_layout()
plt.rcParams['savefig.dpi'] = 1000
plt.savefig('Ipps2.png')