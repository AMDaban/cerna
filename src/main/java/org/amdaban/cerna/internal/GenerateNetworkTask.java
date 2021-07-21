package org.amdaban.cerna.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.amdaban.cerna.internal.exceptions.BadDataException;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
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

        public String toString() {
            return rna1 + ", " + rna2 + "->" + Arrays.toString(connectingMIRNAs);
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

    private final CyNetworkManager networkManager;
    private final CyNetworkFactory networkFactory;
    private final CyNetworkNaming networkNaming;
    private final CyNetworkViewFactory networkViewFactory;
    private final CyNetworkViewManager networkViewManager;
    private final ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory;

    public Set<String> miRNAs;

    private Map<String, CyNode> mRNANodes = new HashMap<>();
    private Map<String, CyNode> miRNANodes = new HashMap<>();
    private Map<String, CyNode> lncRNANodes = new HashMap<>();
    private Map<String, CyNode> circRNANodes = new HashMap<>();

    CyNetwork network;

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateNetworkTask.class);

    public GenerateNetworkTask(ExpressionProfileDB mRNAExpProfDB, ExpressionProfileDB miRNAExpProfDB,
            ExpressionProfileDB lncRNAExpProfDB, ExpressionProfileDB circRNAExpProfDB, RNAInteractionDB mRNAIntDB,
            RNAInteractionDB lncRNAIntDB, RNAInteractionDB circRNAIntDB, double corrThres, double confThres,
            CyNetworkManager networkManager, CyNetworkFactory networkFactory, CyNetworkNaming networkNaming,
            CyNetworkViewFactory networkViewFactory, CyNetworkViewManager networkViewManager,
            ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory) {

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

        this.networkManager = networkManager;
        this.networkFactory = networkFactory;
        this.networkNaming = networkNaming;
        this.networkViewManager = networkViewManager;
        this.networkViewFactory = networkViewFactory;
        this.applyPreferredLayoutTaskFactory = applyPreferredLayoutTaskFactory;
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

        this.generateNetwork(mRNAlncRNAConnectingmiRNAs, mRNAcircRNAConnectingmiRNAs);

        this.applyPreferredLayout();
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
            if (scoreWithRNA1 == null || scoreWithRNA1.doubleValue() < this.confThres) {
                continue;
            }

            Double scoreWithRNA2 = refInteractionDB2.getScore(miRNA, rna2);
            if (scoreWithRNA2 == null || scoreWithRNA2.doubleValue() < this.confThres) {
                continue;
            }

            connectingMIRNAs.add(miRNA);
        }
        return new PairConnectingMIRNAs(rna1, rna2, connectingMIRNAs.toArray(new String[0]));
    }

    private void generateNetwork(PairConnectingMIRNAs[] mRNAlncRNAConnectingmiRNAs,
            PairConnectingMIRNAs[] mRNAcircRNAConnectingmiRNAs) {
        network = networkFactory.createNetwork();
        network.getRow(network).set(CyNetwork.NAME, networkNaming.getSuggestedNetworkTitle("ceRNA"));

        this.createNodes();
        this.createEdges(mRNAlncRNAConnectingmiRNAs, mRNAcircRNAConnectingmiRNAs);

        networkManager.addNetwork(network);

        CyNetworkView networkView = networkViewFactory.createNetworkView(network);
        networkViewManager.addNetworkView(networkView);
    }

    private void createNodes() {
        for (String mRNA : this.mRNAExpressionProfileDB.profiles.keySet()) {
            CyNode mRNANode = this.network.addNode();
            network.getDefaultNodeTable().getRow(mRNANode.getSUID()).set("name", "m~" + mRNA);
            this.mRNANodes.put(mRNA, mRNANode);
        }

        for (String miRNA : this.miRNAExpressionProfileDB.profiles.keySet()) {
            CyNode miRNANode = this.network.addNode();
            network.getDefaultNodeTable().getRow(miRNANode.getSUID()).set("name", "mi~" + miRNA);
            this.miRNANodes.put(miRNA, miRNANode);
        }

        for (String lncRNA : this.lncRNAExpressionProfileDB.profiles.keySet()) {
            CyNode lncRNANode = this.network.addNode();
            network.getDefaultNodeTable().getRow(lncRNANode.getSUID()).set("name", "lnc~" + lncRNA);
            this.lncRNANodes.put(lncRNA, lncRNANode);
        }

        for (String circRNA : this.circRNAExpressionProfileDB.profiles.keySet()) {
            CyNode circRNANode = this.network.addNode();
            network.getDefaultNodeTable().getRow(circRNANode.getSUID()).set("name", "circ~" + circRNA);
            this.circRNANodes.put(circRNA, circRNANode);
        }
    }

    private void createEdges(PairConnectingMIRNAs[] mRNAlncRNAConnectingmiRNAs,
            PairConnectingMIRNAs[] mRNAcircRNAConnectingmiRNAs) {

        for (PairConnectingMIRNAs connectingmiRNAs : mRNAlncRNAConnectingmiRNAs) {
            String mRNA = connectingmiRNAs.rna1;
            CyNode mRNANode = this.mRNANodes.get(mRNA);

            String lncRNA = connectingmiRNAs.rna2;
            CyNode lncNode = this.lncRNANodes.get(lncRNA);

            for (String miRNA : connectingmiRNAs.connectingMIRNAs) {
                CyNode miRNANode = this.miRNANodes.get(miRNA);

                network.addEdge(mRNANode, miRNANode, true);
                network.addEdge(miRNANode, lncNode, true);
            }
        }

        for (PairConnectingMIRNAs connectingmiRNAs : mRNAcircRNAConnectingmiRNAs) {
            String mRNA = connectingmiRNAs.rna1;
            CyNode mRNANode = this.mRNANodes.get(mRNA);

            String circRNA = connectingmiRNAs.rna2;
            CyNode circNode = this.circRNANodes.get(circRNA);

            for (String miRNA : connectingmiRNAs.connectingMIRNAs) {
                CyNode miRNANode = this.miRNANodes.get(miRNA);

                network.addEdge(mRNANode, miRNANode, true);
                network.addEdge(miRNANode, circNode, true);
            }
        }
    }

    private void applyPreferredLayout() {
        final Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
        super.insertTasksAfterCurrentTask(applyPreferredLayoutTaskFactory.createTaskIterator(networkViews));
    }
}
