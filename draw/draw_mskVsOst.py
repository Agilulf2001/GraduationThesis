import sys
sys.path.append('/home/agi/Dune/simulation')
import matplotlib.pyplot as plt

rowsOneIv = 11
flnStr = str(sys.argv[1]) #"todraw/*mskVsOst*"
#此图展示ost、msk算法在accurate准确率、活跃流准确率、不活跃流准确率方面随dtime变化而变化的差异, flow源数据采用norm50, dtime从1到30, 实验200次
#./rruncmd.sh 200 "java -jar SimulationEx.jar --fin input/infile_Active_norm50 --pps 80 -c 8 --flowNum 300" "mskVsOst_norm50" "-o 0.37 --dtime 1" "-o 0.37 --dtime 2" "-o 0.37 --dtime 3" "-o 0.37 --dtime 4" "-o 0.37 --dtime 5" "-o 0.37 --dtime 6" "-o 0.37 --dtime 7" "-o 0.37 --dtime 8" "-o 0.37 --dtime 9" "-o 0.37 --dtime 10" "-o 0.37 --dtime 11" "-o 0.37 --dtime 12" "-o 0.37 --dtime 13" "-o 0.37 --dtime 14" "-o 0.37 --dtime 15" "-o 0.37 --dtime 16" "-o 0.37 --dtime 17" "-o 0.37 --dtime 18" "-o 0.37 --dtime 19" "-o 0.37 --dtime 20" "-o 0.37 --dtime 21" "-o 0.37 --dtime 22" "-o 0.37 --dtime 23" "-o 0.37 --dtime 24" "-o 0.37 --dtime 25" "-o 0.37 --dtime 26" "-o 0.37 --dtime 27" "-o 0.37 --dtime 28" "-o 0.37 --dtime 29" "-o 0.37 --dtime 30" "-m 8 2 --dtime 1" "-m 8 2 --dtime 2" "-m 8 2 --dtime 3" "-m 8 2 --dtime 4" "-m 8 2 --dtime 5" "-m 8 2 --dtime 6" "-m 8 2 --dtime 7" "-m 8 2 --dtime 8" "-m 8 2 --dtime 9" "-m 8 2 --dtime 10" "-m 8 2 --dtime 11" "-m 8 2 --dtime 12" "-m 8 2 --dtime 13" "-m 8 2 --dtime 14" "-m 8 2 --dtime 15" "-m 8 2 --dtime 16" "-m 8 2 --dtime 17" "-m 8 2 --dtime 18" "-m 8 2 --dtime 19" "-m 8 2 --dtime 20" "-m 8 2 --dtime 21" "-m 8 2 --dtime 22" "-m 8 2 --dtime 23" "-m 8 2 --dtime 24" "-m 8 2 --dtime 25" "-m 8 2 --dtime 26" "-m 8 2 --dtime 27" "-m 8 2 --dtime 28" "-m 8 2 --dtime 29" "-m 8 2 --dtime 30"

indVarStr = "--dtime"

fl = open(flnStr, "r")
lines = sum(1 for _ in fl)
fl.seek(0, 0)
indVarNum = lines//(rowsOneIv*2)

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

yitem = []
yitem2 = []
yitem3 = []
for tmpy in yval:
    yitem.append(tmpy[item])
    yitem2.append(tmpy[item2])
    yitem3.append(tmpy[item3])

plt.plot(xval, yitem, marker='*', linewidth=1.0, color='green')
plt.plot(xval, yitem2, marker='*', linewidth=1.0, color='red')
plt.plot(xval, yitem3, marker='*', linewidth=1.0, color='brown')


plt.legend(labels=('overall-ost', 'inactive-ost', 'active-ost', 'overall-msk', 'inactive-msk', 'active-msk'), loc='lower right')

plt.xticks(range(1,31), rotation=45)
ytick = []
for i in range(4, 97, 4):
    ytick.append(i*0.01)
plt.yticks(ytick)
plt.xlabel('Run Time (million time units)')
plt.ylabel('Flowsize accuracy')

# plt.show() # show() or save
plt.tight_layout()
plt.rcParams['savefig.dpi'] = 1000
plt.savefig('mskVsOst.png')