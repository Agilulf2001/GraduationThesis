package Sketch;

import Common.Constant;
import org.apache.log4j.Logger;

import java.util.Random;

class Cookie
{
    private int counterBits; //用来作为计数器的位数, 总位数等于counterBits + 1
    private int value;
    private int overFlowMask; //用来处理计数器部分的掩码
    /**
     *
     * @param counterBits < 31
     */
    public Cookie(int counterBits) {
        this.counterBits = counterBits; // 例如，7
        this.value = 0;
        this.overFlowMask = (1 << this.counterBits) - 1; // 01111111
        // System.out.printf("Cookie's overFlowMask is %d\n", this.overFlowMask);
    }

    public boolean isValid()
    {
        return (this.value & (1 << counterBits)) != 0;
    }

    public void setValidBit()
    {
        this.value = (this.value | (1 << this.counterBits));
    }

    public void clearValidBit()
    {
        this.value = (this.value & this.overFlowMask);
    }

    public void increment(int delta)
    { // 假设counterBits为7，则总位数为8
      // cookieCell的初始值为0000 0000，increment后为1000 0001
      // cookieCell的最大值为1111 1111
        this.clearValidBit();
        this.value = (this.value + delta) & this.overFlowMask;
        if(this.value != 0)
        {
            setValidBit();
        }
    }

    public void rightShift(int bits)
    {
        if(bits > counterBits)
        {
            this.value = 0;
            return;
        }
        this.clearValidBit();;
        this.value = (this.value >> bits);
        if(this.value != 0)
        {
            this.setValidBit();
        }
    }
    public int getCounterBits() {
        return counterBits;
    }

    public void setCounterBits(int counterBits) {
        assert counterBits <= Constant.MAX_COOKIE_COUNTER_BITS; // 30
        boolean valid = this.isValid();
        clearValidBit();
        this.counterBits = counterBits;
        this.overFlowMask = (1 << this.counterBits) - 1;
        this.value = (this.value & this.overFlowMask);
        if(valid)
        {
            setValidBit();
        }
        else
        {
            assert this.value == 0;
        }
    }

    //pjy add
    public int getValue() {
        return value;
    }
    //pjy add end
}

class Mask
{
    private int totalBits; //总位数，假设是8
    private int leftSetBits; //最左s位置为1，s >= 1，假设是4
    private int mask; //那么二进制则是11110000
    

    public int getValidBound()//pjy modify 此函数无意义
    {
        return (1 << 1);
    }

    public int getTotalBits() {
        return totalBits;
    }

    public int getMask() {
        return mask;
    }

    public int getLeftSetBits() {
        return leftSetBits;
    }

    public void setLeftSetBits(int leftSetBits) {
        this.leftSetBits = leftSetBits;
        setMask();
    }

    public void leftShiftMask(int bits)
    {
        if(leftSetBits - bits >= 1)
        {
            setLeftSetBits(leftSetBits - bits);
        }
        else
        {
            setLeftSetBits(1);
        }
    }

    public void rightShiftMask(int bits)
    {
        if(leftSetBits + bits <= totalBits)
        {
            setLeftSetBits(leftSetBits + bits);
        }
        else
        {
            setLeftSetBits(totalBits);
        }
    }

    /**
     * 左移一位, 相当于提高要求
     */
    public void leftShiftMask()
    {
        if(leftSetBits <= 1)
            return;
        this.mask = (this.mask ^ (1 << (totalBits - leftSetBits)));
        this.leftSetBits--;
    }

    /**
     * 右移一位，相当于降低要求
     */
    public void rightShiftMask()
    {
        if(leftSetBits >= totalBits)
            return;
        this.leftSetBits++;
        this.mask = (this.mask ^ (1 << (totalBits - leftSetBits)));
    }

    public Mask(int totalBits, int leftSetBits)
    {
        this.totalBits = totalBits;
        this.leftSetBits = leftSetBits;
        setMask();
    }
    private void setMask()
    {
        int t = (1 << leftSetBits) - 1;
        mask = (t << (totalBits - leftSetBits));
        // System.out.printf("tot:%d left:%d msk:%d\n", totalBits, leftSetBits, mask);
    }

    /**
     * 返回 2 ^ (b-1) + 2 ^ (b-s)
     * @return
     */
    public int getThreShold()
    {
        return (1 << (this.totalBits - 1)) + (1 << (this.totalBits - this.leftSetBits));
    }

    /**
     * 返回 2 ^ (b - 1)
     * @return
     */
    public int getThreShold2()
    {
        return (1 << (this.totalBits - 1));
    }
}


public class SuMaxScatterLetRMT extends SuMaxSketch{
    private Random random;
    private Cookie[][] cookie;
    private Mask mask;
    private int packetCounter; //INT包数目
    private int cellCounter;
    private Logger logger = Logger.getLogger("SuMaxScatterLetRMT.class");
    private boolean useSelfAdjust;
    private boolean useOST = false;
    private boolean useMask;
    private double adjustLowBound = 0.1;
    private double adjustUpBound = 0.9;
    private int adjustInterval = 0;

    public SuMaxScatterLetRMT clone()
    {
        return new SuMaxScatterLetRMT(this);
    }
    public Mask getMask() {
        return mask;
    }

    /**
     * 
     * @param rowNum
     * @param bitNum
     */
    public SuMaxScatterLetRMT(int rowNum, int bitNum) {
        super(rowNum, bitNum);
        initRandom();
        // System.out.printf("This is SuMaxScatterLetRMT, rowNum:%d, bitNum:%d\n", rowNum, bitNum);
    }

    public SuMaxScatterLetRMT(SuMaxScatterLetRMT letRMT)
    {
        super(letRMT.rowNum, letRMT.ArrayLengthBit);
        initRandom();
        // System.out.println("This is SuMaxScatterLetRMT");
        initCookie(letRMT.cookie[0][0].getCounterBits() + 1);
        if(letRMT.mask != null)
        {
            setMask(new Mask(letRMT.mask.getTotalBits(), letRMT.mask.getLeftSetBits()));
        }
        this.packetCounter = 0;
        this.cellCounter = 0;
        this.useSelfAdjust = letRMT.useSelfAdjust;
        this.adjustLowBound = letRMT.adjustLowBound;
        this.adjustUpBound = letRMT.adjustUpBound;
        this.adjustInterval = letRMT.adjustInterval;
        this.hashes = letRMT.hashes;
        this.useOST = letRMT.useOST;
    }

    public void setCookie(int cookieTotalBits)
    {
        initCookie(cookieTotalBits);
    }
    public void setOST()
    {
        // System.out.println("set ost_2");
        this.useOST = true;
    }
    public void setMask(Mask mask)
    {
        // System.out.println("set msk_2");
        this.useMask = true;
        this.mask = mask;
    }

    public void setSelfAdjust(double adjustLowBound, double adjustUpBound, int adjustInterval)
    {
        this.useSelfAdjust = true;
        this.adjustLowBound = adjustLowBound;
        this.adjustUpBound = adjustUpBound;
        this.adjustInterval = adjustInterval;
        packetCounter = 0;
        cellCounter = 0;
    }

    @Override
    public void handleSizeArray(int value) { // value is 1
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < Constant.SUMAX_ARRAYS_NUM; i++)
        {
            int index = this.hashIndex[i] % this.SUMAX_ARRAY_LENGTH;
            // 保证所有sizeCellArray[i][index]行中，值小的一定会被更新
            if(sizeCellArray[i][index] + value < min)
            {
                min = sizeCellArray[i][index] + value;
                sizeCellArray[i][index] = min;
                // logger.debug(String.format("Before Insert: Cell[%d][%d]: %x", i, index, cookie[i][index].getValue()));
                cookie[i][index].increment(Constant.INCREMENT_DELTA);
                // logger.debug(String.format("After Insert: Cell[%d][%d]: %x", i, index, cookie[i][index].getValue()));
            }
            else if(sizeCellArray[i][index] < min)
            {
                sizeCellArray[i][index] = min;
                // logger.debug(String.format("Before Insert: Cell[%d][%d]: %x", i, index, cookie[i][index].getValue()));
                cookie[i][index].increment(Constant.INCREMENT_DELTA);
                // logger.debug(String.format("After Insert: Cell[%d][%d]: %x", i, index, cookie[i][index].getValue()));
            }
            // logger.debug(String.format("sizeCellArray[%d][%d]: %d", i, index, sizeCellArray[i][index]));
        }
    }



    private void initRandom()
    {
        long seed = System.currentTimeMillis();
        if(Constant.DEBUG_FLAG)
            seed = 1;
        random = new Random(seed);
    }

    public void initCookie(int cookieTotalBits)
    {
        this.cookie = new Cookie[this.rowNum][this.SUMAX_ARRAY_LENGTH];
        for(int i = 0; i < rowNum; i++)
        {
            for(int j = 0; j < SUMAX_ARRAY_LENGTH; j++)
            {
                this.cookie[i][j] = new Cookie(cookieTotalBits - 1);
            }
        }
    }

    /**
     *
     * @param rowIndex 在哪一行查找
     * @param colAddress 以哪一列为起始地址
     * @param bound 寻找多大的范围，如2 ^ r
     * @return 返回相对偏移
     */
    private int getOffsetByCookieWithMask(int rowIndex, int colAddress, int bound)
    {
        int tmpIndex = 0;
        ++Constant.letSum;
        for(int i = 0; i < bound; i++)
        {
            int colIndex = (colAddress + i) % SUMAX_ARRAY_LENGTH;
            int tmp = mask.getMask() & cookie[rowIndex][colIndex].getValue();
            ++Constant.regAcceTimes;
            if(tmp >= mask.getThreShold()) //上限
            {
                tmpIndex = i;
                ++Constant.overThreshLetSum;
                break;
            }
            else if(tmp >= mask.getThreShold2()) //下限
            {
                tmpIndex = i;
            }
        }
        if(this.useSelfAdjust) {
            int tmpv1 = mask.getMask() & cookie[rowIndex][(colAddress + tmpIndex) % SUMAX_ARRAY_LENGTH].getValue();
            int tmpv2 = mask.getThreShold();
            if(tmpv1 >= tmpv2)
                cellCounter++;
        }
        return tmpIndex;
    }

    private int getOffsetByCookieNoMask(int rowIndex, int colAddress, int bound)
    {// 只取最大，但仍有可能有的流不被更新。只要流一直活跃，它不被更新到的概率趋近于0
     // 流不被"及时"更新，是影响重建sketch的准确率的根本因素
     // 选取Offset的算法的最终目的是为了尽可能提高reconstructed-sketch的准确率
        long max = Long.MIN_VALUE;
        int maxIndex = 0;
        for(int i = 0; i < bound; i++)
        {
            int colIndex = (colAddress + i) % this.SUMAX_ARRAY_LENGTH;
            if((long)(this.cookie[rowIndex][colIndex].getValue()) > max)
            {
                max = this.cookie[rowIndex][colIndex].getValue();
                maxIndex = i;
            }
        }
        return maxIndex;
    }
    
    // private int getOffsetByCookieOST(int rowIndex, int colAddress, int bound)
    // {
    //     int tmpIndex = 0, max = -1, tmpThresh = (int)(bound*(2+2*Constant.OSTRATIO));
    //     ++Constant.regAcceTimes;
    //     ++Constant.letSum;
    //     for (int i = 0; i < bound; i++) {
    //         int colIndex = (colAddress + i) % SUMAX_ARRAY_LENGTH;
    //         int tmp = cookie[rowIndex][colIndex].getValue();
    //         if(tmp >= tmpThresh) //上限
    //         {
    //             tmpIndex = i;
    //             ++Constant.overThreshLetSum;
    //             break;
    //         }
    //         else if(tmp > max) //下限
    //         {
    //             tmpIndex = i;
    //             max = tmp;
    //         }
    //     }
    //     return tmpIndex;
    // }

    private int getOffsetByCookieOST(int rowIndex, int colAddress, int bound)
    {
        int max = -1;
        int maxIndex = 0;
        int bound_1 = (int)(bound * Constant.OSTRATIO);
        for(int i = 0; i < bound_1; i++)
        {
            int colIndex = (colAddress + i) % this.SUMAX_ARRAY_LENGTH;
            if((this.cookie[rowIndex][colIndex].getValue()) > max)
            {
                max = this.cookie[rowIndex][colIndex].getValue();
                maxIndex = i;
            }
        }
        // if 
        // System.out.printf("%d\n", max);
        Constant.regAcceTimes += bound_1;
        for (int i = bound_1; i < bound; i++)
        {
            int colIndex = (colAddress + i) % this.SUMAX_ARRAY_LENGTH;
            ++Constant.regAcceTimes;
            if((this.cookie[rowIndex][colIndex].getValue()) >= max)
            {
                maxIndex = i;
                break;
            }
        }
        return maxIndex;
    }

    /**
     * 自适应调节Mask
     */
    private void selfAdjustMask()
    {
        if(this.packetCounter > this.adjustInterval)
        {
            double tmp = (double)this.cellCounter / (double)this.rowNum / (double)this.packetCounter;
            if(tmp < this.adjustLowBound)
            {
                this.mask.rightShiftMask();
                //this.mask.rightShiftMask(2);
            }
            else if(tmp > this.adjustUpBound)
            {
                this.mask.leftShiftMask();
                //this.mask.leftShiftMask(2);
            }
            // logger.debug("Mask leftSetBits:" + this.mask.getLeftSetBits());
            // System.out.printf("packet C: %d\n", this.packetCounter);
            // System.out.printf("cell   C: %d\n", this.cellCounter);
            // System.out.printf("Interval: %d\n", this.adjustInterval);
            this.cellCounter = 0;
            this.packetCounter = 0;
        }
    }



    public Sketchlet getSketchlet(Sketchlet sketchlet)//pjy modify
    {
        // System.out.println("using ost_2");
        // if (this.useOST) System.out.println("set ost_1");
        if(this.useSelfAdjust)
        {
            this.packetCounter++;
            this.selfAdjustMask();// 每个INT包携带的cell的数目大于阈值或者小于阈值的时候才进行调节
        }
        assert sketchlet instanceof Scatterlet;
        int absAddress = Math.abs(random.nextInt()) % SUMAX_ARRAY_LENGTH;
        Scatterlet scatterlet = (Scatterlet)sketchlet;
        scatterlet.setAddress(absAddress);
        for(int i = 0; i < this.rowNum; i++)
        {
            int offset;
            if(this.useOST){
                // System.out.println("using ost_3");
                offset = getOffsetByCookieOST(i, absAddress, Constant.BOUND);}
            else if(this.useMask)
                offset = getOffsetByCookieWithMask(i, absAddress, Constant.BOUND);
            else
                offset = getOffsetByCookieNoMask(i, absAddress, Constant.BOUND);
            // getOffsetByOST(i, absAddress, Constant.BOUND);
            int colIndex = (absAddress + offset) % SUMAX_ARRAY_LENGTH;
            scatterlet.setOffsetByIndex(i, offset);
            scatterlet.setValueByIndex(i, this.sizeCellArray[i][colIndex]);
            // 流很稀疏时，有值的cookie[][]也很稀疏，造成Sketch-INT方法准确率低
            // logger.debug(String.format("Before \tTaking: Cell[%d][%d]: %x", i, colIndex, this.cookie[i][colIndex].getValue()));
            this.cookie[i][colIndex].rightShift(Constant.RIGHT_SHIFT_BITS); // 2
            // logger.debug(String.format("After \tTaking: Cell[%d][%d]: %x", i, colIndex, this.cookie[i][colIndex].getValue()));
        }
        return scatterlet;
    }

    /**
     * 通过sketchlet更新sketch，只在终端进行，所以没有更新bitmap或cookie
     * @param sketchlet 
     */
    public void setBySketchlet(Sketchlet sketchlet)
    {
        assert sketchlet instanceof SuMaxScatterLet;
        Scatterlet scatterlet = (Scatterlet) sketchlet;
        int absAddress = scatterlet.getAddress();
        for(int i = 0; i < this.rowNum; i++)
        {
            this.sizeCellArray[i][(absAddress + scatterlet.getOffsetByIndex(i)) % SUMAX_ARRAY_LENGTH] = scatterlet.getValueByIndex(i);
        }
    }
}
