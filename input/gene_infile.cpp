#include <iostream>
#include <fstream>
#include <cmath>
#include <ctime>
#include <random>
using namespace std;
int N = 50000;
// int N0[7] = {0, 2500, 5000, 10000, 15000, 20000, 25000};
string flname[6] = {"infile_Active_norm10", "infile_Active_norm11", "infile_Active_norm12", "infile_Active_norm13", "infile_Active_norm14", "infile_Active_norm15"};
double dtLim[6] = {20.0, 22.0, 24.0, 26.0, 28.0, 30.0};

int main() {
    ofstream outfile;
    int srcip = 167772161, srcpt = 1;
    int dstip = 1862270977,dstpt = 1;
    int pps = 0, packetNum = 0;
    double dt;

    for (int t = 0; t < 6; t++) {
        outfile.open(flname[t]);
        outfile << "50000\n";
        for (int i = 0; i < N; i++) {
            random_device seed;
            ranlux48 engine(seed());
            uniform_int_distribution<> distrib(20, 2000);
            pps = distrib(engine);
            uniform_real_distribution<> distrib2(2.0, dtLim[t]);
            dt = distrib2(engine);
            outfile << srcip << "," << srcpt << "," 
                    << dstip << "," << dstpt << ","
                    << pps   << "," << (int)(pps*dt) << "\n";
            srcip++, dstip++, srcpt++, dstpt++;
        }
        outfile.close();
    }
    return 0;
}
