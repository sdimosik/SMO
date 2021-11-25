package com.sdimosik.smo;

import dnl.utils.text.table.TextTable;

import java.util.List;

public class Report {
    private final List<Integer> countGeneratedTask4Source;
    private final List<Double> probabilityOfFailure4Source;
    private final List<Double> avgTimeInBuffer;
    private final List<Double> avgTimeInAppliance;
    private final List<Double> kUsedAppliance;

    public Report(
        List<Integer> countGeneratedTask4Source,
        List<Double> probabilityOfFailure4Source,
        List<Double> avgTimeInBuffer,
        List<Double> avgTimeInAppliance,
        List<Double> kUsedAppliance
    ) {
        this.countGeneratedTask4Source = countGeneratedTask4Source;
        this.probabilityOfFailure4Source = probabilityOfFailure4Source;
        this.avgTimeInBuffer = avgTimeInBuffer;
        this.avgTimeInAppliance = avgTimeInAppliance;
        this.kUsedAppliance = kUsedAppliance;
    }

    public void print() {

        System.out.println("\n-----------------Table 1-----------------");
        String[] namesT1 = new String[6];
        namesT1[0] = "numOfSource";
        namesT1[1] = "countTask";
        namesT1[2] = "Prob of Fail";
        namesT1[3] = "T exist";
        namesT1[4] = "T buffer";
        namesT1[5] = "T appliance";

        int sizeT1 = countGeneratedTask4Source.size();
        Object[][] dataT1 = new Object[sizeT1][namesT1.length];
        for (int i = 0; i < sizeT1; i++) {
            dataT1[i][0] = i;
            dataT1[i][1] = countGeneratedTask4Source.get(i);
            dataT1[i][2] = probabilityOfFailure4Source.get(i);
            dataT1[i][3] = avgTimeInBuffer.get(i) + avgTimeInAppliance.get(i);
            dataT1[i][4] = avgTimeInBuffer.get(i);
            dataT1[i][5] = avgTimeInAppliance.get(i);
        }
        TextTable t1 = new TextTable(namesT1, dataT1);
        t1.printTable();

        System.out.println("\n-----------------Table 2-----------------");
        String[] namesT2 = new String[2];
        namesT2[0] = "numOfAppliance";
        namesT2[1] = "K of Used";

        int sizeT2 = kUsedAppliance.size();
        Object[][] dataT2 = new Object[sizeT2][namesT2.length];
        for (int i = 0; i < sizeT2; i++) {
            dataT2[i][0] = i;
            dataT2[i][1] = kUsedAppliance.get(i);
        }
        TextTable t2 = new TextTable(namesT2, dataT2);
        t2.printTable();
    }
}
