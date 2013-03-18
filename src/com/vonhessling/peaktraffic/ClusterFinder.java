package com.vonhessling.peaktraffic;
import java.util.*;

/**
 * This class implements Bron-Kerbosch clique detection algorithm as it is
 * described in [Samudrala R.,Moult J.:A Graph-theoretic Algorithm for
 * comparative Modeling of Protein Structure; J.Mol. Biol. (1998); vol 279; pp.
 * 287-302]
 * @author hessling
 */
public class ClusterFinder
{
    private Graph g;
    private HashSet<List<String>> clusters;

    /**
     * Creates a new cluster finder for the given graph. 
     * @param graph The graph to use
     */
    public ClusterFinder(Graph g)
    {
        clusters = new HashSet<List<String>>();
        this.g = g;
    }
    
    /**
     * Given a set of nodes, adds the maximal cliques to clusters
     * This is a "local" method because only a small part of the graph is examined for new clusters. 
     * @param allDirectNeighbors The set of nodes which may have encountered a new cluster situation 
     */
    public void updateClustersLocal(Set<String> allDirectNeighbors)
    {
        List<String> potentialCluster = new ArrayList<String>();
        List<String> candidates = new ArrayList<String>();
        List<String> alreadyFound = new ArrayList<String>();
        candidates.addAll(allDirectNeighbors);
//TODO remove?!
Collections.sort(candidates); // see whether the unified sorting will make processing order more predictable!?      
        findCliques(potentialCluster, candidates, alreadyFound);
    }
    


    /**
     * Recursively finds all maximal cliques using the Bron-Kerbosch algorithm.
     * @param potentialCluster The potential cluster at this recursion step
     * @param candidates The candidate nodes which may be added to this cluster
     * @param alreadyFound The nodes which have been proven to lead to a valid extension of the current cluster
     */
    private void findCliques(List<String> potentialCluster, List<String> candidates, List<String> alreadyFound) {
        List<String> candidatesCopy = new ArrayList<String>(candidates);
        if (!endReached(candidates, alreadyFound)) {
            for (String candidate : candidatesCopy) { // loop over all candidates...
                List<String> newCandidates = new ArrayList<String>();
                List<String> newAlreadyFound = new ArrayList<String>();

                // move candidate node to potentialCluster:
                potentialCluster.add(candidate);
                candidates.remove(candidate);
                // create newCandidates by removing nodes in candidates not connected to candidate node:
                for (String newCandidate : candidates) {
                    if (g.containsEdge(candidate, newCandidate)) {
                        newCandidates.add(newCandidate);
                    } 
                } 

                // create newAlreadyFound by removing nodes in alreadyFound that are not connected to candidate node:
                for (String newFound : alreadyFound) {
                    if (g.containsEdge(candidate, newFound)) {
                        newAlreadyFound.add(newFound);
                    } 
                } 

                // if newCandidates and newAlreadyFound are empty...
                if (newCandidates.isEmpty() && newAlreadyFound.isEmpty()) {
                    // potentialCluster is a maximal cluster
// TODO remove condition?: register clusters of at least 3 members: 
if (potentialCluster.size() >= 3) {
clusters.add(new ArrayList<String>(potentialCluster));
}
                } else {
                    findCliques(potentialCluster, newCandidates, newAlreadyFound); // call recursively
                } 
                // move candidate from potentialCluster to alreadyFound;
                alreadyFound.add(candidate);
                potentialCluster.remove(candidate);
            } // end loop over all candidates
        } // end if end reached 
    }

    /**
     * Determines whether the termination criterion has been fulfilled for the Bron-Kerbosch algorithm
     * @param candidates The candidate nodes
     * @param alreadyFound The set of nodes which have been proven to lead to a valid extension of the current cluster
     * @return Returns whether the termination criterion has been fulfilled.
     */
    private boolean endReached(List<String> candidates, List<String> alreadyFound)
    {
        boolean end = false;
        int edgeCounter;
        // if a node in alreadyFound is connected to all nodes in candidates...
        for (String found : alreadyFound) {
            edgeCounter = 0;
            for (String candidate : candidates) {
                if (g.containsEdge(found, candidate)) {
                    edgeCounter++;
                }
            }
            if (edgeCounter == candidates.size()) {
                end = true;
            }
        }
        return end;
    }

    /**
     * Removes subsets of valid maximal clusters.  Uses Algorithm-T to produce these subsets with high speed. 
     */
    public void cleanSubsetClusters() {
        List<List<String>> allCombinations;
        List<List<String>> toRemove = new ArrayList<List<String>>();
        for (List<String> curCluster : clusters) {
            allCombinations = Utils.getAllCombinations(3, curCluster.size() - 1, curCluster);
            for (List<String> curCombination : allCombinations) {
                if (clusters.contains(curCombination)) {
                    toRemove.add(curCombination);
                }
            }
        }            
        clusters.removeAll(toRemove);
    }
 
    /**
     * Call cleanSubsetClusters() first if you need to eliminate subsets of valid maximal clusters
     * @return Returns the clusters that have been found so far; may contain subsets of valid maximal clusters!
     */
    public HashSet<List<String>> getClusters() {
        return clusters;
    }
}
