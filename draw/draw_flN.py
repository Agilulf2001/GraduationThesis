import sys
sys.path.append('/home/agi/Dune/simulation')

rowsOneIv = 11
flnStr = str(sys.argv[1]) # "todraw/*flN*"
flnStr2 = str(sys.argv[2])
#此图展示ost的accurate准确率、未Hash碰撞的比例随flN变化而变化, flow源数据采用norm100, flN从50到1250, 实验次数为100
#./rruncmd.sh 100 "java -jar SimulationEx.jar --fin input/infile_Active_norm100 --dtime 10 --pps 200 -c 8 -o 0.37" "flN" "--flowNum 50" "--flowNum 100" "--flowNum 150" "--flowNum 200" "--flowNum 250" "--flowNum 300" "--flowNum 350" "--flowNum 400" "--flowNum 450" "--flowNum 500" "--flowNum 550" "--flowNum 600" "--flowNum 650" "--flowNum 700" "--flowNum 750" "--flowNum 800" "--flowNum 850" "--flowNum 900" "--flowNum 950" "--flowNum 1000" "--flowNum 1050" "--flowNum 1100" "--flowNum 1150" "--flowNum 1200" "--flowNum 1250"
indVarStr = "flowNum"

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

fl = open(flnStr2, "r")
yval2 = []
for i in range(0, indVarNum):
    str = fl.readline()
    # beg = str.find(indVarStr) + len(indVarStr) + 1
    # end = str.find(']')
    # xval.append(int(str[beg:end]))
    tmpy = []
    for j in range(1, rowsOneIv):
        str = fl.readline()
        beg = str.find(':') + 2
        tmpy.append(float(str[beg:]))
    yval2.append(tmpy)

fl.close()

import matplotlib.pyplot as plt
item = 1
item2 = 2
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

fig = plt.figure()
ax = fig.add_subplot()
ax.plot(xval, yitem, marker='.', linewidth=1.0)
ax.plot(xval, yitem3, marker='*', linewidth=1.0, color='green')

ax.set_xlabel('Number of flows')
ax.set_ylabel('Flowsize accuracy')
ytick = []
for i in range(55, 85, 2):
    ytick.append(i*0.01)
ax.set_yticks(ytick)

ax2 = plt.twinx()
ax2.plot(xval, yitem2, marker='.', linewidth=1.0, color='black')
ax2.plot(xval, yitem4, marker='*', linewidth=1.0, color='gray')
ax2.set_ylabel('Hash collision rate')
# ytick = []
# for i in range(92, 101):
#     ytick.append(i*0.01)
# ax2.set_yticks(ytick)

plt.xticks([50, 150, 250, 350, 450, 550, 650, 750, 850, 950, 1050, 1150, 1250])
fig.legend(labels=('ost accuracy', 'msk accuracy', 'ost hash collision', 'msk hash collision'), loc=1, bbox_to_anchor=(0.65,1), bbox_transform=ax.transAxes)

# plt.show() # show() or save
plt.tight_layout()
plt.rcParams['savefig.dpi'] = 1000
plt.savefig('flN.png')