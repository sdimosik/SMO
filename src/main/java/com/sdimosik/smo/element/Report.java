package com.sdimosik.smo.element;

import java.util.List;

public class Report {
    public final List<Integer> countGeneratedTask4Source;
    public final List<Double> probabilityOfFailure4Source;
    public final List<Double> avgTimeInBuffer;
    public final List<Double> avgTimeInAppliance;
    public final List<Double> kUsedAppliance;
    public final List<Double> bufferDispersion;
    public final List<Double> applianceDispersion;

    public Report(
        List<Integer> countGeneratedTask4Source,
        List<Double> probabilityOfFailure4Source,
        List<Double> avgTimeInBuffer,
        List<Double> avgTimeInAppliance,
        List<Double> kUsedAppliance,
        List<Double> bufferDispersion,
        List<Double> applianceDispersion
    ) {
        this.countGeneratedTask4Source = countGeneratedTask4Source;
        this.probabilityOfFailure4Source = probabilityOfFailure4Source;
        this.avgTimeInBuffer = avgTimeInBuffer;
        this.avgTimeInAppliance = avgTimeInAppliance;
        this.kUsedAppliance = kUsedAppliance;
        this.bufferDispersion = bufferDispersion;
        this.applianceDispersion = applianceDispersion;
    }

}
