package src.model;

public class StudentRecord {
    private double attendance;
    private double score;
    private double activity;
    private int passed;

    public StudentRecord(double attendance, double score, double activity, int passed) {
        this.attendance = attendance;
        this.score = score;
        this.activity = activity;
        this.passed = passed;
    }

    public double getAttendance() {
        return attendance;
    }

    public double getScore() {
        return score;
    }

    public double getActivity() {
        return activity;
    }

    public int getPassed() {
        return passed;
    }

    public double[] getFeatures() {
        return new double[]{attendance, score, activity};
    }

    @Override
    public String toString() {
        return "StudentRecord{" +
                "attendance=" + attendance +
                ", score=" + score +
                ", activity=" + activity +
                ", passed=" + passed +
                '}';
    }
}