package de.dasoftware.cryptpad.service;

import de.dasoftware.cryptpad.model.EntryTreeNode;
import de.dasoftware.cryptpad.model.SearchResult;
import de.dasoftware.cryptpad.model.SearchResult.MatchType;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for searching through the tree structure
 */
public class SearchService {
    
    private static final int SNIPPET_LENGTH = 50;
    
    /**
     * Searches recursively through all nodes
     * 
     * @param root Root node to start search from
     * @param searchText Text to search for (case-insensitive)
     * @return List of search results
     */
    public List<SearchResult> search(EntryTreeNode root, String searchText) {
        List<SearchResult> results = new ArrayList<>();
        
        if (root == null || searchText == null || searchText.trim().isEmpty()) {
            return results;
        }
        
        String searchLower = searchText.toLowerCase();
        searchRecursive(root, searchLower, "", results);
        
        return results;
    }
    
    /**
     * Recursive search implementation
     */
    private void searchRecursive(EntryTreeNode node, String searchText, 
                                  String parentPath, List<SearchResult> results) {
        
        String title = node.getUserObject().toString();
        String content = node.getContent();
        String currentPath = parentPath.isEmpty() ? title : parentPath;
        
        boolean titleMatch = title.toLowerCase().contains(searchText);
        boolean contentMatch = content != null && content.toLowerCase().contains(searchText);
        
        if (titleMatch || contentMatch) {
            MatchType matchType;
            if (titleMatch && contentMatch) {
                matchType = MatchType.BOTH;
            } else if (titleMatch) {
                matchType = MatchType.TITLE;
            } else {
                matchType = MatchType.CONTENT;
            }
            
            String snippet = contentMatch ? createSnippet(content, searchText) : "";
            results.add(new SearchResult(node, matchType, snippet, currentPath));
        }
        
        // Search children
        for (int i = 0; i < node.getChildCount(); i++) {
            EntryTreeNode child = (EntryTreeNode) node.getChildAt(i);
            String childPath = parentPath.isEmpty() ? title : parentPath + " > " + title;
            searchRecursive(child, searchText, childPath, results);
        }
    }
    
    /**
     * Creates a context snippet around the first match
     */
    private String createSnippet(String content, String searchText) {
        int index = content.toLowerCase().indexOf(searchText);
        
        if (index == -1) {
            return "";
        }
        
        int start = Math.max(0, index - SNIPPET_LENGTH / 2);
        int end = Math.min(content.length(), index + searchText.length() + SNIPPET_LENGTH / 2);
        
        StringBuilder snippet = new StringBuilder();
        
        if (start > 0) {
            snippet.append("...");
        }
        
        snippet.append(content, start, end);
        
        if (end < content.length()) {
            snippet.append("...");
        }
        
        // Replace newlines with spaces for display
        return snippet.toString().replace("\n", " ").replace("\r", "");
    }
}