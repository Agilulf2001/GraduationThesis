import sys
sys.path.append('/home/agi/Dune/simulation/draw')

rowsOneIv = 11
flnStr = str(sys.argv[1]) # "todraw/*ostRatio*"
#此图展示ost的accurate准确率、活跃流准确率、不活跃流准确率随ost ratio变化而变化, flow源数据采用norm50, dtime从10到100, 实验次数为100
#./rruncmd.sh 100 "java -jar SimulationEx.jar --fin input/infile_Active_norm50 --flowNum 300 --dtime 10 --pps 200 -c 8" "ostRatio" "-o 0.04" "-o 0.08" "-o 0.12" "-o 0.16" "-o 0.20" "-o 0.24" "-o 0.28" "-o 0.32" "-o 0.36" "-o 0.40" "-o 0.44" "-o 0.48" "-o 0.52" "-o 0.56" "-o 0.60" "-o 0.64" "-o 0.68" "-o 0.72" "-o 0.76" "-o 0.80" "-o 1.00" ""
indVarStr = "-o"

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
    xval.append(float(str[beg:end]))
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
item4 = 3
yitem = []
yitem2 = []
yitem3 = []
yitem4 = []
for tmpy in yval:
    yitem.append(tmpy[item])
    yitem4.append(tmpy[item4]/100000)
    yitem2.append(tmpy[item2])
    yitem3.append(tmpy[item3])

fig = plt.figure()
ax = fig.add_subplot()
ax.plot(xval, yitem, marker='.', linewidth=1.0)
ax.plot(xval, yitem2, marker='.', linewidth=1.0, color='purple')
ax.plot(xval, yitem3, marker='.', linewidth=1.0, color='orange')
ax.set_xlabel('OST stopping point')
ax.set_ylabel('Flowsize accuracy')

tick = []
for i in range(5, 101, 5):
    tick.append(i*0.01)
ax.set_yticks(tick)

ax2 = plt.twinx()
ax2.plot(xval, yitem4, marker='+', linewidth=1.0, color='black')
ax2.set_ylabel('Register access times')

# plt.legend(labels=('overall', 'inactive', 'active'), loc='lower right')

tick = []
for i in range(0, 101, 10):
    tick.append(i*0.01)
plt.xticks(tick)
plt.text(1.15, 2.7, "×10^5", ha='right')

fig.legend(labels=('overall', 'inactive', 'active', 'register access'), loc=1, bbox_to_anchor=(1,0.3), bbox_transform=ax.transAxes)

# plt.show() # show() or save
plt.tight_layout()
plt.rcParams['savefig.dpi'] = 1000
plt.savefig('ostRatio.png')