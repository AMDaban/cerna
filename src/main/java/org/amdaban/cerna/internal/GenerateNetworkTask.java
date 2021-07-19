package org.amdaban.cerna.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.amdaban.cerna.internal.exceptions.BadDataException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateNetworkTask extends AbstractTask {
    class PairCorrelationScore {
        public String e1;
        public String e2;
        public double score;

        public PairCorrelationScore(String e1, String e2, Double score) {
            this.e1 = e1;
            this.e2 = e2;
            this.score = score;
        }

        public String toString() {
            return e1 + ", " + e2 + "->" + Double.toString(this.score);
        }
    }

    class PairRNA {
        public String rna1;
        public String rna2;

        public PairRNA(String rna1, String rna2) {
            this.rna1 = rna1;
            this.rna2 = rna2;
        }

        public String toString() {
            return rna1 + "_" + rna2;
        }
    }

    class PairConnectingMIRNAs {
        public String rna1;
        public String rna2;
        public String[] connectingMIRNAs;

        public PairConnectingMIRNAs(String rna1, String rna2, String[] connectingMIRNAs) {
            this.rna1 = rna1;
            this.rna2 = rna2;
            this.connectingMIRNAs = connectingMIRNAs;
        }
    }

    public ExpressionProfileDB mRNAExpressionProfileDB;
    public ExpressionProfileDB miRNAExpressionProfileDB;
    public ExpressionProfileDB lncRNAExpressionProfileDB;
    public ExpressionProfileDB circRNAExpressionProfileDB;

    public RNAInteractionDB mRNAInteractionDB;
    public RNAInteractionDB lncRNAInteractionDB;
    public RNAInteractionDB circRNAInteractionDB;

    public double corrThres;
    public double confThres;

    public Set<String> miRNAs;

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateNetworkTask.class);

    public GenerateNetworkTask(ExpressionProfileDB mRNAExpProfDB, ExpressionProfileDB miRNAExpProfDB,
            ExpressionProfileDB lncRNAExpProfDB, ExpressionProfileDB circRNAExpProfDB, RNAInteractionDB mRNAIntDB,
            RNAInteractionDB lncRNAIntDB, RNAInteractionDB circRNAIntDB, double corrThres, double confThres) {

        this.mRNAExpressionProfileDB = mRNAExpProfDB;
        this.miRNAExpressionProfileDB = miRNAExpProfDB;
        this.lncRNAExpressionProfileDB = lncRNAExpProfDB;
        this.circRNAExpressionProfileDB = circRNAExpProfDB;

        this.mRNAInteractionDB = mRNAIntDB;
        this.lncRNAInteractionDB = lncRNAIntDB;
        this.circRNAInteractionDB = circRNAIntDB;

        this.corrThres = corrThres;
        this.confThres = confThres;

        this.miRNAs = this.miRNAExpressionProfileDB.profiles.keySet();
    }

    @Override
    public void run(TaskMonitor monitor) throws BadDataException {
        this.validateInputData();

        PairCorrelationScore[] lncRNAcorrScores = this.computePairCorrelationScores(this.mRNAExpressionProfileDB,
                this.lncRNAExpressionProfileDB);
        PairRNA[] lncRNAcorrScoresfiltered = this.filterPCSMatrix(lncRNAcorrScores);
        PairConnectingMIRNAs[] mRNAlncRNAConnectingmiRNAs = this.extractConnectingMIRNAsBatch(lncRNAcorrScoresfiltered,
                this.mRNAInteractionDB, this.lncRNAInteractionDB);

        PairCorrelationScore[] circRNAcorrScores = this.computePairCorrelationScores(this.mRNAExpressionProfileDB,
                this.circRNAExpressionProfileDB);
        PairRNA[] circRNAcorrScoresfiltered = this.filterPCSMatrix(circRNAcorrScores);
        PairConnectingMIRNAs[] mRNAcircRNAConnectingmiRNAs = this.extractConnectingMIRNAsBatch(
                circRNAcorrScoresfiltered, this.mRNAInteractionDB, this.circRNAInteractionDB);
    }

    private void validateInputData() throws BadDataException {
        if (!this.checkSampleVectorsEquality()) {
            throw new BadDataException("sample vector are not equal in expression files");
        }
    }

    private boolean checkSampleVectorsEquality() {
        String[] refSampleVector = this.mRNAExpressionProfileDB.samples;
        if (!Arrays.equals(refSampleVector, this.miRNAExpressionProfileDB.samples)) {
            return false;
        }
        if (!Arrays.equals(refSampleVector, this.lncRNAExpressionProfileDB.samples)) {
            return false;
        }
        if (!Arrays.equals(refSampleVector, this.circRNAExpressionProfileDB.samples)) {
            return false;
        }
        return true;
    }

    private PairCorrelationScore[] computePairCorrelationScores(ExpressionProfileDB exp1, ExpressionProfileDB exp2) {
        PairCorrelationScore[] result = new PairCorrelationScore[exp1.profiles.size() * exp2.profiles.size()];

        int idx = 0;
        for (Map.Entry<String, double[]> entry1 : exp1.profiles.entrySet()) {
            String id1 = entry1.getKey();
            double[] profile1 = entry1.getValue();

            for (Map.Entry<String, double[]> entry2 : exp2.profiles.entrySet()) {
                String id2 = entry2.getKey();
                double[] profile2 = entry2.getValue();

                double score = this.computePearsonCorrelationScore(profile1, profile2);

                result[idx] = new PairCorrelationScore(id1, id2, score);

                idx++;
            }
        }

        return result;
    }

    private double computePearsonCorrelationScore(double[] p1, double[] p2) {
        PearsonsCorrelation pearsonCorrelation = new PearsonsCorrelation();
        return pearsonCorrelation.correlation(p1, p2);
    }

    private PairRNA[] filterPCSMatrix(PairCorrelationScore[] pairCorrelationMatrix) {
        List<PairRNA> filtered = new ArrayList<>();
        for (PairCorrelationScore pairCorrelationScore : pairCorrelationMatrix) {
            if (pairCorrelationScore.score >= this.corrThres) {
                filtered.add(new PairRNA(pairCorrelationScore.e1, pairCorrelationScore.e2));
            }
        }
        return filtered.toArray(new PairRNA[0]);
    }

    private PairConnectingMIRNAs[] extractConnectingMIRNAsBatch(PairRNA[] pairRNAs, RNAInteractionDB refInteractionDB1,
            RNAInteractionDB refInteractionDB2) {
        List<PairConnectingMIRNAs> result = new ArrayList<>();
        for (PairRNA pairRNA : pairRNAs) {
            result.add(this.extractConnectingMIRNAsSingle(pairRNA, refInteractionDB1, refInteractionDB2));
        }
        return result.toArray(new PairConnectingMIRNAs[0]);
    }

    private PairConnectingMIRNAs extractConnectingMIRNAsSingle(PairRNA pairRNA, RNAInteractionDB refInteractionDB1,
            RNAInteractionDB refInteractionDB2) {
        String rna1 = pairRNA.rna1;
        String rna2 = pairRNA.rna2;

        List<String> connectingMIRNAs = new ArrayList<>();
        for (String miRNA : this.miRNAs) {
            Double scoreWithRNA1 = refInteractionDB1.getScore(miRNA, rna1);
            if (scoreWithRNA1 == null || scoreWithRNA1.doubleValue() >= this.confThres) {
                continue;
            }

            Double scoreWithRNA2 = refInteractionDB1.getScore(miRNA, rna2);
            if (scoreWithRNA2 == null || scoreWithRNA2.doubleValue() >= this.confThres) {
                continue;
            }

            connectingMIRNAs.add(miRNA);
        }
        return new PairConnectingMIRNAs(rna1, rna2, connectingMIRNAs.toArray(new String[0]));
    }
}
