package src.ml;

import src.model.StudentRecord;

import java.util.List;

public class LogisticRegression {
    private double[] weights;
    private double bias;
    private boolean trained;

    public LogisticRegression(int featureCount) {
        this.weights = new double[featureCount];
        this.bias = 0.0;
        this.trained = false;
    }

    public void train(List<StudentRecord> data, double learningRate, int epochs) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Немає даних для навчання.");
        }

        for (int epoch = 0; epoch < epochs; epoch++) {
            for (StudentRecord record : data) {
                double[] x = normalize(record.getFeatures());
                int y = record.getPassed();

                double predicted = predictProbabilityNormalized(x);
                double error = predicted - y;

                for (int i = 0; i < weights.length; i++) {
                    weights[i] -= learningRate * error * x[i];
                }

                bias -= learningRate * error;
            }
        }

        trained = true;
    }

    public double predictProbability(double attendance, double score, double activity) {
        double[] x = normalize(new double[]{attendance, score, activity});
        return predictProbabilityNormalized(x);
    }

    public int predict(double attendance, double score, double activity) {
        return predictProbability(attendance, score, activity) >= 0.5 ? 1 : 0;
    }

    public double evaluateAccuracy(List<StudentRecord> data) {
        if (!trained) {
            throw new IllegalStateException("Модель ще не навчена.");
        }

        int correct = 0;
        for (StudentRecord record : data) {
            int predicted = predict(record.getAttendance(), record.getScore(), record.getActivity());
            if (predicted == record.getPassed()) {
                correct++;
            }
        }

        return (double) correct / data.size();
    }

    private double predictProbabilityNormalized(double[] x) {
        double z = bias;
        for (int i = 0; i < weights.length; i++) {
            z += weights[i] * x[i];
        }
        return sigmoid(z);
    }

    private double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    private double[] normalize(double[] x) {
        double[] normalized = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            normalized[i] = x[i] / 100.0;
        }
        return normalized;
    }

    public boolean isTrained() {
        return trained;
    }

    public double[] getWeights() {
        return weights;
    }

    public double getBias() {
        return bias;
    }
}