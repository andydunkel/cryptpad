package de.dasoftware.cryptpad.model;

/**
 * Represents a search result with node reference and match details
 */
public class SearchResult {
    
    public enum MatchType {
        TITLE,
        CONTENT,
        BOTH
    }
    
    private final EntryTreeNode node;
    private final MatchType matchType;
    private final String contextSnippet;
    private final String nodePath;
    
    public SearchResult(EntryTreeNode node, MatchType matchType, String contextSnippet, String nodePath) {
        this.node = node;
        this.matchType = matchType;
        this.contextSnippet = contextSnippet;
        this.nodePath = nodePath;
    }
    
    public EntryTreeNode getNode() {
        return node;
    }
    
    public MatchType getMatchType() {
        return matchType;
    }
    
    public String getContextSnippet() {
        return contextSnippet;
    }
    
    public String getNodePath() {
        return nodePath;
    }
    
    @Override
    public String toString() {
        return nodePath + " > " + node.getUserObject().toString();
    }
}