package JAVA_SIM;

import Common.*;
import Sketch.*;
import Utility.Hash;
import Utility.Md5;
import Utility.Sha256;
import Utility.Util;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.log4j.BasicConfigurator;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SimulationEx {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        ArgumentParser parser = ArgumentParsers.newFor("Main").build().defaultHelp(true);
        parser.addArgument("--fin").help("data file path");
        // parser.addArgument("--fout").help("result file path");
        // parser.addArgument("--sketch").choices("Column", "ScatterBitmap", "ScatterRMT").help("the sketch to be used");
        parser.addArgument("--flowNum").type(Integer.class).help("simulation flow num");
        parser.addArgument("--dtime").type(Integer.class).help("persist time(s)");
        parser.addArgument("--pps").type(Integer.class).help("INT flow pps");
        parser.addArgument("-o", "--ost").type(Double.class).help("Optimal Stopping Theory");
        parser.addArgument("-m", "--mask").type(Integer.class).nargs(2).help("mask arguments: total_bits left_set_bits");
        parser.addArgument("-a", "--adjust").type(Double.class).nargs(2).help("self adjusting arguments: low_bound up_bound");
        parser.addArgument("-c", "--cookie").type(Integer.class).help("Cookie total bits");
        parser.addArgument("-I", "--interval").type(Integer.class).help("Adjusting interval");
        parser.addArgument("--boundBits").type(Integer.class).help("bound bits, e.x., bits=6, bound = 2 ^ 6 = 64");
        parser.addArgument("-r", "--ratio").type(Double.class).help("Active or Not ratio");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        String in_file_name = ns.getString("fin");
        String flstr = ns.getString("flowNum");
        while (flstr.length() < 4) flstr = "0"+flstr;
        String dtstr = ns.getString("dtime");
        while (dtstr.length() < 4) dtstr = "0"+dtstr;
        String ppstr = ns.getString("pps");
        while (ppstr.length() < 4) ppstr = "0"+ppstr;
        // String out_file_name = "out/fl_"+flstr+"#dt_"+dtstr+"#Ipps_"+ppstr;//ns.getString("fout");

        String sketch_name = "ScatterRMT";//ns.getString("sketch");
        int simulate_flow_num = ns.getInt("flowNum");
        int persist_time = ns.getInt("dtime");
        // if (persist_time > 120)
            // System.exit(1); // Out of heap Memory
        int INT_pps = ns.getInt("pps");
        FactorySketch factory;
        if(ns.get("boundBits") != null)
        {
            Constant.BOUND = (1 << ns.getInt("boundBits"));
        }
        else
        {
            Constant.BOUND = 64;
        }
        // System.out.println("Bound: " + Constant.BOUND);
        String outStr = "";
        outStr += ("Bound: " + Constant.BOUND + "\n");
        if(sketch_name.trim().equals("Column"))
        {
            factory = new FactoryColumnSketch();
        }
        else if(sketch_name.trim().equals("ScatterBitmap"))
        {
            factory = new FactoryScatterSketch();
        }
        else if (sketch_name.trim().equals("ScatterRMT"))
        {
            FactoryScatterSketchRMT sketchRMT = new FactoryScatterSketchRMT();
            sketchRMT.setSketch(Constant.SUMAX_ARRAYS_NUM, Constant.SUMAX_ARRAY_BITNUM);
            if(ns.get("cookie") != null)    // 使用ScatterRMT必须使用Cookie
            {
                sketchRMT.setCookie(ns.getInt("cookie"));
                outStr += ("Using cookie: " + ns.getString("cookie") + "\n");
            }
            if(ns.get("ost") != null)   // 使用ost则不使用Alg2以及Nomask
            {
                // System.out.println("using ost_1");
                sketchRMT.setOST();
                // out_file_name += "#ost";
                Constant.OSTRATIO = ns.getDouble("ost");
            }
            else if(ns.get("mask") != null)  // 使用Alg2
            {
                List<Integer> maskArgs = ns.getList("mask");
                if (maskArgs.get(0) < maskArgs.get(1)) System.exit(1);
                sketchRMT.setMask(maskArgs.get(0), maskArgs.get(1));
            }
            if(ns.get("adjust") != null)
            {
                List<Double> adjustArgs = ns.getList("adjust");
                int adjustInterval = Constant.UPDATE_NUMBER;
                if(ns.get("interval") != null)
                    adjustInterval = ns.getInt("interval");
                sketchRMT.setSelfAdjust(adjustArgs.get(0), adjustArgs.get(1), adjustInterval);
                outStr += ("Using self-adjusting: ajLowBound: " + adjustArgs.get(0)
                            + ", ajUpBound: " + adjustArgs.get(1) 
                            + ", ajInterval: " + adjustInterval + "\n");
            }
            factory = sketchRMT;
        }
        else
        {
            System.out.println("Sketch name must be Column or Scatter_bitmap or Scatter_RMT");
            return;
        }
        Hash[] hashes = new Hash[Constant.SUMAX_ARRAYS_NUM];
        hashes[0] = new Md5();
        if(Constant.DEBUG_FLAG)
        {
            byte[] seed = Util.long2Bytes(1);
            hashes[0].setSeed(seed);
        }
        hashes[1] = new Sha256();
        if(Constant.DEBUG_FLAG)
        {
            byte[] seed = Util.long2Bytes(1);
            hashes[1].setSeed(seed);
        }
        //use the same hash functions
        SuMaxSketch sketch = factory.getSuMaxSketch();
        sketch.setHashes(hashes);
        Switch simSwitch = new Switch(sketch);

        sketch = factory.getSuMaxSketch();
        sketch.setHashes(hashes);
        Receiver receiver = new Receiver(sketch);

        //生成sketchlet，复用
        Sketchlet sketchlet = factory.getSketchLet();
        int eleph= 0;

        ArrayList<FlowInfo> datas = new ArrayList<>(simulate_flow_num);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(in_file_name));
            List<Integer> simList = null;
            int totalFlowNum = Integer.parseInt(reader.readLine());
            assert totalFlowNum >= simulate_flow_num;
            simList = Util.getRandomList(totalFlowNum, simulate_flow_num, 0.01*eleph);
            assert simList.size() == simulate_flow_num;

            int currentLine = 0;
            int curPos = 0;
            int nextParseLine = simList.get(curPos);
            do {
                String line = reader.readLine();
                if(line == null)
                    break;
                if(currentLine == nextParseLine)
                {
                    datas.add(Util.parseFlowInfoFromLine(line));
                    curPos++;
                    if(curPos >= simulate_flow_num)
                        break;
                    nextParseLine = simList.get(curPos);
                }
                currentLine++;
            }while (true);
            assert datas.size() == simulate_flow_num;
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //为每个流提前计算好各行的hash值
        for(FlowInfo info: datas)
        {
            info.flowID.setHashValues(hashes);
        }
        Packet_Generator generator = new Packet_Generator(INT_pps, persist_time, datas);
        Packet packet = new Packet(false);
        ArrayList<FlowInfo> infos;
        while ((infos = generator.GetInfos()) != null)
        {
            int packetNum = infos.size();
            for(int i = 0; i < packetNum; i++)
            {   // 核心步骤
                packet.packetClear();
                FlowInfo flowInfo = infos.get(i);
                packet.packetSet(flowInfo, 0);
                simSwitch.process(packet, sketchlet);
                receiver.Receive_Packet(packet);
            }
        }
        // try {
            // BufferedWriter writer = new BufferedWriter(new FileWriter(out_file_name));
            SuMaxSketch receiverSketch = receiver.getSketch();
            double sentsum = 0, querysum = 0, realsum = 0, maxsum = 0;
            double[][] sent = new double[simulate_flow_num][2]; // 2 is rowNum of sketch
            double[][] recv = new double[simulate_flow_num][2];
            int[] ppsRange = new int[simulate_flow_num];
            boolean[] isActiv = new boolean[simulate_flow_num];
            double N0sent = 0, N1sent = 0, N0sum = 0, N1sum = 0;
            int i = 0;
            double ratio = 0.82;
            if (ns.get("ratio") != null)
                ratio = ns.getDouble("ratio");
            for(FlowInfo info: datas)
            {
                // writer.write(""
                //         + info.flowID.srcIP + ","
                //         + info.flowID.srcPort + ","
                //         + info.flowID.dstIP + ","
                //         + info.flowID.dstPort + ","
                //         + (int)info.pps + ","
                //         + info.realSendNum + ":  ["
                // );
                ppsRange[i] = (int)(info.pps);
                isActiv[i] = (info.realSendNum > (int)(ratio*info.pps*persist_time));
                QueryData result = simSwitch.querySketch(info.flowID, QueryType.QUERY_SIZE);
                if(result instanceof QueryFlowSizeData)
                {
                    int t = 0;
                    for(int flowSize: ((QueryFlowSizeData)result).getFlowSize())
                    {
                        sentsum += flowSize;
                        // writer.write("" + flowSize);
                        sent[i][t++] = flowSize;
                        // t++;
                        // if (t == 0) {writer.write(","); t = 1;}
                    }
                }
                // writer.write("]___[");
                result = receiverSketch.query(info.flowID, QueryType.QUERY_SIZE);
                if(result instanceof QueryFlowSizeData)
                {
                    int t = 0;
                    for(int flowSize: ((QueryFlowSizeData)result).getFlowSize())
                    {
                        querysum += flowSize;
                        // writer.write("" + flowSize);
                        recv[i][t++] = flowSize;
                        // t++;
                        // if (t == 0) {writer.write(","); t = 1;}
                    }
                }
                // writer.write("]\n");
                i++;
            }
            int collision = 0, missed = 0;
            double activSent = 0, noActSent = 0, activSum = 0, noActSum = 0;
            for (int k = 0; k < i; k++) {
                if ((int)recv[k][0] == 0) missed++;
                if ((int)recv[k][1] == 0) missed++;
                if ((int)sent[k][0] < (int)sent[k][1]) {
                    collision++;
                    realsum += sent[k][0];
                    maxsum += recv[k][0];
                    if (ppsRange[k] <= 100)
                        {N0sent += sent[k][0]; N0sum += recv[k][0];}
                    else if (ppsRange[k] > 1600)
                        {N1sent += sent[k][0]; N1sum += recv[k][0];}
                    if (isActiv[k])
                        {activSent += sent[k][0]; activSum += recv[k][0];}
                    else
                        {noActSent += sent[k][0]; noActSum += recv[k][0];}
                } else if ((int)sent[k][0] > (int)sent[k][1]) {
                    collision++;
                    realsum += sent[k][1];
                    maxsum += recv[k][1];
                    if (ppsRange[k] <= 100)
                        {N0sent += sent[k][1]; N0sum += recv[k][1];}
                    else if (ppsRange[k] > 1600)
                        {N1sent += sent[k][1]; N1sum += recv[k][1];}
                    if (isActiv[k])
                        {activSent += sent[k][1]; activSum += recv[k][1];}
                    else
                        {noActSent += sent[k][1]; noActSum += recv[k][1];}
                } else {
                    realsum += sent[k][0];
                    double tmpmax = recv[k][0] > recv[k][1] ? recv[k][0] : recv[k][1];
                    maxsum += tmpmax;
                    if (ppsRange[k] <= 100)
                        {N0sent += sent[k][0]; N0sum += tmpmax;}
                    else if (ppsRange[k] > 1600)
                        {N1sent += sent[k][0]; N1sum += tmpmax;}
                    if (isActiv[k])
                        {activSent += sent[k][0]; activSum += tmpmax;}
                    else
                        {noActSent += sent[k][0]; noActSum += tmpmax;}
                }
            }
            double timelyUpdate;
            if (maxsum/realsum > 0.7)
                timelyUpdate = 2*maxsum/realsum - 1;
            else
                timelyUpdate = 0.57*maxsum/realsum;
            int noTimelyCount = 0, noTimelyCount1 = 0, noTimelyCount2 = 0;
            int t = simulate_flow_num*eleph/100;
            for (int k = 0; k < t; k++) {
                if (recv[k][0] / sent[k][0] < timelyUpdate)
                    noTimelyCount1++;
                if (recv[k][1] / sent[k][1] < timelyUpdate)
                    noTimelyCount1++;
            }
            for (int k = t; k < i; k++) {
                if (recv[k][0] / sent[k][0] < timelyUpdate)
                    noTimelyCount2++;
                if (recv[k][1] / sent[k][1] < timelyUpdate)
                    noTimelyCount2++;
            }
            noTimelyCount = noTimelyCount1 + noTimelyCount2;
            // writer.write(outStr);
            // writer.write("average   rate:" + querysum/sentsum + "\n");
            // writer.write("accurate  rate:" + maxsum/realsum + "\n");
            // writer.write("collision rate:" + (1.0*collision/i) + "\n");
            // writer.write("No update rate:" + (0.5*missed/i) + "\n");
            // writer.write("NoTimelyUpdate:" + (0.5*noTimelyCount/i) + "\n");
            // writer.write("NoTimely Eleph:" + (0.5*noTimelyCount1/t) + "\n");
            // writer.write("NoTimely Mouse:" + (0.5*noTimelyCount2/(i-t)) + "\n");
            // writer.write("NoAct accurate:" + (noActSum/noActSent) + "\n");
            // writer.write("Activ accurate:" + (activSum/activSent) + "\n");
            // writer.write("HigPPSaccurate:" + (N1sum/N1sent) + "\n");
            // writer.write("LowPPSaccurate:" + (N0sum/N0sent) + "\n");
            // writer.close();
            System.out.printf("rough accuracy:%.15f\n", querysum/sentsum);
            System.out.printf("corrected accu:%.15f\n", maxsum/realsum);
            System.out.printf("collision rate:%.15f\n", 1.0*collision/i);
            System.out.printf("RegisterAccess:%017d\n", Constant.regAcceTimes);
            System.out.printf("No update rate:%.15f\n", 0.5*missed/i);
            System.out.printf("overThreshRate:%.15f\n", 1.0*Constant.overThreshLetSum/Constant.letSum);
            if (N1sent == 0) System.out.printf("HigPPSaccurate:%.14f\n", -1.0);
            else System.out.printf("HigPPSaccurate:%.15f\n", N1sum/N1sent);
            if (N0sent == 0) System.out.printf("LowPPSaccurate:%.14f\n", -1.0);
            else System.out.printf("LowPPSaccurate:%.15f\n", N0sum/N0sent);
            if ((int)noActSent == 0) System.out.printf("NoAct accurate:%.14f\n", -1.0);
            else System.out.printf("NoAct accurate:%.15f\n", noActSum/noActSent);
            if ((int)activSent == 0) System.out.printf("Activ accurate:%.14f\n", -1.0);
            else System.out.printf("Activ accurate:%.15f\n", activSum/activSent);
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }
}
