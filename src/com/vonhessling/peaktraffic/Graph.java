package com.vonhessling.peaktraffic;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Graph {
    
    private Hashtable<String, Set<String>> g = new Hashtable<String, Set<String>>();

    /**
     * Determines whether this graph contains an edge from node1 to node2
     * @param node1 The source node for the edge
     * @param node2 The target node for the edge
     * @return Returns whether this graph contains an edge from node1 to node2
     */
    public boolean containsEdge(String node1, String node2) {
        if ((node1 == null) || (node2 == null)){
            throw new IllegalArgumentException("Given node(s) are null!");
        }
        Set<String> neighbors = getDirectNeighbors(node1); 
        if (neighbors == null) {
            return false;
        }
        return neighbors.contains(node2); 
    }
    
    /**
     * Adds the given edge to this graph
     * @param node1 The source node for the edge
     * @param node2 The target node for the edge
     */
    public void addEdge(String node1, String node2) {
        if ((node1 == null) || (node2 == null)){
            throw new IllegalArgumentException("Given node(s) are null!");
        }
        Set<String> neighbors = g.get(node1);
        if (neighbors == null) {
            Set<String> entry = new HashSet<String>();
            entry.add(node2);
            g.put(node1, entry);
        } else {
            neighbors.add(node2);
        }
    }
    
    /**
     * Removes the edge from node1 to node2 in this graph
     * @param node1 The source node for the edge
     * @param node2 The target node for the edge
     */
    public void removeEdge(String node1, String node2) {
        if ((node1 == null) || (node2 == null)){
            throw new IllegalArgumentException("Given node(s) are null!");
        }
        Set<String> neighbors = g.get(node1);
        if (neighbors != null) {
            neighbors.remove(node2);
        }
    }
    
    /**
     * Determines the direct neighbors of the given node in this graph
     * @param node The node for which to get it's neighbors
     * @return Returns the direct neighbors of the given node, or an empty set if none
     */
    public Set<String> getDirectNeighbors(String node) {
        if (node == null) {
            throw new IllegalArgumentException("Given node is null!");
        }
        Set<String> neighbors = g.get(node);
        if (neighbors == null) {
            return new HashSet<String>();
        }
        return neighbors; 
    }
}
