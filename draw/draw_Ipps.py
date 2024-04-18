import sys
sys.path.append('/home/agi/Dune/simulation')

rowsOneIv = 11
flnStr = str(sys.argv[1]) #"todraw/*Ipps*"
#此图展示ost的accurate准确率、未及时更新比例随INT流pps变化而变化, flow源数据采用norm100, pps从50到1250, 实验次数为100
#./rruncmd.sh 100 "java -jar SimulationEx.jar --fin input/infile_Active_norm100 --flowNum 300 --dtime 10 -c 8 -o 0.37" "Ipps" "--pps 50" "--pps 100" "--pps 150" "--pps 200" "--pps 250" "--pps 300" "--pps 350" "--pps 400" "--pps 450" "--pps 500" "--pps 550" "--pps 600" "--pps 650" "--pps 700" "--pps 750" "--pps 800" "--pps 850" "--pps 900" "--pps 950" "--pps 1000" "--pps 1050" "--pps 1100" "--pps 1150" "--pps 1200" "--pps 1250"
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
item2 = 4
item4 = 6
item5 = 7
yitem = []
yitem2 = []
yitem4 = []
yitem5 = []
for tmpy in yval:
    yitem.append(tmpy[item])
    yitem2.append(tmpy[item2])
    yitem4.append(tmpy[item4])
    yitem5.append(tmpy[item5])

fig = plt.figure()
ax = fig.add_subplot()
ax.plot(xval, yitem2, marker='.', linewidth=1.0, color='red')
ax.set_xlabel('Packets per second of INT flows')
ax.set_ylabel('Not updated rate')
ytick = []
for i in range(0, 64, 3):
    ytick.append(i*0.01)
ax.set_yticks(ytick)

ax2 = plt.twinx()
ax2.plot(xval, yitem, marker='.', linewidth=1.0)
ax2.plot(xval, yitem4, marker='3', linewidth=1.0, color='black')
ax2.plot(xval, yitem5, marker='4', linewidth=1.0, color='gray')
ax2.set_ylabel('Flowsize accuracy')
ytick = []
for i in range(36, 101, 3):
    ytick.append(i*0.01)
ax2.set_yticks(ytick)

plt.xticks([50, 150, 250, 350, 450, 550, 650, 750, 850, 950, 1050, 1150, 1250])
fig.legend(labels=('not updated rate', 'overall accuracy', 'accuracy of flows with high pps', 'accuracy of flows with high pps'), loc=1, bbox_to_anchor=(1,0.3), bbox_transform=ax.transAxes)

# plt.show() # show() or save
plt.tight_layout()
plt.rcParams['savefig.dpi'] = 1000
plt.savefig('Ipps.png')