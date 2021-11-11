package element;

import java.util.List;

public class Report {
    private final List<Integer> countGeneratedTask4Source;
    private final List<Double> probabilityOfFailure4Source;
    private final List<Double> avgTimeOfExistTask4Source;
    private final List<Double> kUsedAppliance;

    public Report(
        List<Integer> countGeneratedTask4Source,
        List<Double> probabilityOfFailure4Source,
        List<Double> avgTimeOfExistTask4Source,
        List<Double> kUsedAppliance
    ) {
        this.countGeneratedTask4Source = countGeneratedTask4Source;
        this.probabilityOfFailure4Source = probabilityOfFailure4Source;
        this.avgTimeOfExistTask4Source = avgTimeOfExistTask4Source;
        this.kUsedAppliance = kUsedAppliance;
    }

    @Override
    public String toString() {
        return "Report{" +
            "\ncountGeneratedTask4Source=" + countGeneratedTask4Source +
            ", \nprobabilityOfFailure4Source=" + probabilityOfFailure4Source +
            ", \navgTimeOfExistTask4Source=" + avgTimeOfExistTask4Source +
            ", \nkUsedAppliance=" + kUsedAppliance +
            '}';
    }
}
