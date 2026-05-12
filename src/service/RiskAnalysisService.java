package src.service;

public class RiskAnalysisService {

    public String calculateRisk(double attendance,
                                double averageGrade,
                                double activity) {

        double score = (attendance * 0.4)
                     + (averageGrade * 0.4)
                     + (activity * 0.2);

        if (score >= 75) {
            return "Низький ризик";
        } else if (score >= 50) {
            return "Середній ризик";
        } else {
            return "Високий ризик";
        }
    }
}
