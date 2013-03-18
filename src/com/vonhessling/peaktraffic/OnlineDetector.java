package com.vonhessling.peaktraffic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * A bottom-up approach to finding the largest maximal clusters (cliques) in a graph.
 * Uses the Bron-Kerbosch algorithm and the Algorithm T internally to rapidly identify the clusters. 
 * @author hessling
 */
public class OnlineDetector {

    private final int MIN_CLUSTER_SIZE = 3; // the minimal cluster size we're interested in
    
    private int idCounter = 0; // assigns individual email addresses a unique ID
    private Hashtable<String, String> emailToIds = new Hashtable<String, String>(); // maps email -> ID
    private Hashtable<String, String> idsToEmail = new Hashtable<String, String>(); // maps ID -> email
    
    private ClusterFinder finder;
    private List<Graph> graphs = new ArrayList<Graph>(); // contains the unverified graph and the verified graph (in that order)
    
    public OnlineDetector() {
        graphs.add(new Graph()); // the unverified graph: each existing edge is directed (valid in one direction only)
        graphs.add(new Graph()); // the existing verified graph: each edge is undirected and registered at both nodes (duplicate)
        finder = new ClusterFinder(getVerifiedGraph()); 
    }

   /**
    * Runs the cluster detection algorithm.
    * @param fileName The file name from which to read input data
    * @throws FileNotFoundException Throws FileNotFoundException if file is not found.
    * @throws IOException Throws IOException if error occurs reading the file.
    */    
    public void findClusters(String fileName) throws FileNotFoundException, IOException {
        // let potential FileNotFoundException propagate out
        BufferedReader inReader = new BufferedReader(new FileReader(fileName));
        String curLine;
        String from = null;
        String to = null;
        String fromId;
        String toId;
        StringTokenizer st;
        boolean first;
        Set<String> neighborIntersection;
        Set<String> fromNeighbors;
        Set<String> toNeighbors;
        // let potential IOException propagate out
        while ((curLine = inReader.readLine()) != null) {
            st = new StringTokenizer(curLine, "\t"); // split each line at tabs
            st.nextToken(); // skip first token, the date
            first = true;
            while (st.hasMoreTokens()) {
                if (first) {
                    from = st.nextToken();
                } else {
                    to = st.nextToken();
                }
                first = false;
            }
            fromId = getNodeId(from); // convert email addresses into IDs
            toId = getNodeId(to);            

            if (!updateGraphs(fromId, toId)) { // add edge;  only continue processing if the graph situation has changed sufficiently
                continue;
            }

            fromNeighbors = getVerifiedGraph().getDirectNeighbors(fromId); // get the direct neighbors of the "from node" in the verified graph
            toNeighbors = getVerifiedGraph().getDirectNeighbors(toId);     // get the direct neighbors of the   "to node" in the verified graph
            // this is only a minor improvement in speed:
            // we're only interested in finding clusters of MIN_CLUSTER_SIZE <==> each vertex needs to have at least (MIN_CLUSTER_SIZE - 1) direct neighbors to start the Bron Kerbosch algorithm
            if (fromNeighbors.size() < MIN_CLUSTER_SIZE - 1 || toNeighbors.size() < MIN_CLUSTER_SIZE - 1) { 
                continue;
            }
            // now, if the intersection of fromNeighbors and toNeighbors (plus from, to nodes) are not of the minimal size, skip to next iteration
            neighborIntersection = new HashSet<String>(fromNeighbors); 
            neighborIntersection.retainAll(toNeighbors); // new cliques can only be in the _intersection_ between from/toNeighbor
            // from node and to node are parts of the neighborhood to evaluate, of course:
            neighborIntersection.add(fromId);
            neighborIntersection.add(toId);
            if (neighborIntersection.size() < MIN_CLUSTER_SIZE) { 
                continue;
            }

            finder.updateClustersLocal(neighborIntersection); 
        } // end loop over every input line

        // remove the clusters that are not maximal:
        finder.cleanSubsetClusters();
        printClusters(finder.getClusters());
    }
    
    /**
     * Gets the graph whose edges are directed and do not imply that the interaction was mutual
     * @return The "unverified" graph
     */
    private Graph getUnverifiedGraph() {
        return graphs.get(0);
    }
    
    /**
     * Gets the graph whose edges are undirected and _do_ imply that the interaction was mutual
     * @return The "verified" graph
     */
    private Graph getVerifiedGraph() {
        return graphs.get(1);
    }
    
    /**
     * Records the edge information in the corresponding graph. 
     * @param fromId The ID of the node from which the interaction occurred
     * @param toId The ID of the node towards which the interaction occurred
     * @return Returns whether the graphs may have changed the cluster situation (whether the cluster finding algorithm needs to be started or not)
     */
    private boolean updateGraphs(String fromId, String toId) {
        Graph verifiedGraph = getVerifiedGraph();
        Graph unverifiedGraph = getUnverifiedGraph();
        if (verifiedGraph.containsEdge(fromId, toId)) { // ignoring repeat edges that have been verified already
            return false;
        }
        if (unverifiedGraph.containsEdge(toId, fromId)) { // verifying edges: move them from unverified graph to verified graph
            verifiedGraph.addEdge(fromId, toId);
            verifiedGraph.addEdge(toId, fromId);
            unverifiedGraph.removeEdge(toId, fromId);
            return true;
        } else {
            unverifiedGraph.addEdge(fromId, toId); // adding first unverified edge
            return false;
        }
    }

    /**
     * Determines the node id for the given email address; either looks the existing ID up or assigns a new one. 
     * @param email The email address
     * @return The node id
     */
    private String getNodeId(String email) {
        String id = emailToIds.get(email);
        if (id == null) {
            emailToIds.put(email, idCounter + "");
            idsToEmail.put(idCounter + "", email);
            idCounter++;
            return (idCounter - 1) + "";
        } else {
            return id;
        }
    }

    /**
     * Prints all clusters in the required format
     * @param clusters The clusters to print.
     */
    public void printClusters(HashSet<List<String>> clusters) {
        int idCounter = 0;
        List<String> allStrings = new ArrayList<String>();
        StringBuffer curStringBuffer;
        
        for (List<String> curCluster : clusters) {
            curStringBuffer = new StringBuffer();
            // sort the email addresses within a cluster alphabetically:
            List<String> clusterEmails = new ArrayList<String>();
            for (idCounter = 0; idCounter < curCluster.size(); idCounter++) {
                clusterEmails.add(idsToEmail.get(curCluster.get(idCounter)));
            }
            Collections.sort(clusterEmails);
            
            // create the entire string representation for the cluster:
            for (idCounter = 0; idCounter < curCluster.size(); idCounter++) {
                curStringBuffer.append(clusterEmails.get(idCounter) + (idCounter < curCluster.size() - 1? ", " : ""));
            }
            allStrings.add(curStringBuffer.toString());
        }
        // sort all clusters alphabetically:
        Collections.sort(allStrings);
        
        // print all clusters:
        for (String curString: allStrings) {
            System.out.println(curString);
        }
    }
}
