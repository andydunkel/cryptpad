package de.dasoftware.cryptpad.ui;

import de.dasoftware.cryptpad.model.SearchResult;
import de.dasoftware.cryptpad.model.SearchResult.MatchType;
import de.dasoftware.cryptpad.i18n.Messages;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel displaying search results in a list
 */
public class SearchResultPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    private JList<SearchResult> resultList;
    private DefaultListModel<SearchResult> listModel;
    private JLabel statusLabel;
    
    public SearchResultPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(Messages.getString("search.results")));
        
        // List model and JList
        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setCellRenderer(new SearchResultCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        
        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    /**
     * Updates the result list with new search results
     */
    public void setResults(List<SearchResult> results, String searchText) {
        listModel.clear();
        
        if (results == null || results.isEmpty()) {
            if (searchText != null && !searchText.trim().isEmpty()) {
                statusLabel.setText(Messages.getString("search.noresults"));
            } else {
                statusLabel.setText(" ");
            }
            return;
        }
        
        for (SearchResult result : results) {
            listModel.addElement(result);
        }
        
        statusLabel.setText(Messages.getString("search.resultcount", results.size()));
    }
    
    /**
     * Clears all results
     */
    public void clearResults() {
        listModel.clear();
        statusLabel.setText(" ");
    }
    
    /**
     * Gets the JList for adding selection listeners
     */
    public JList<SearchResult> getResultList() {
        return resultList;
    }
    
    /**
     * Custom cell renderer for search results
     */
    private static class SearchResultCellRenderer extends DefaultListCellRenderer {
        
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof SearchResult) {
                SearchResult result = (SearchResult) value;
                
                // Build display text
                StringBuilder html = new StringBuilder("<html>");
                
                // Node title with path
                html.append("<b>").append(escapeHtml(result.getNode().getUserObject().toString())).append("</b>");
                
                // Match type indicator
                String typeLabel = getMatchTypeLabel(result.getMatchType());
                html.append(" <font color='gray'>[").append(typeLabel).append("]</font>");
                
                // Path on second line
                if (!result.getNodePath().isEmpty()) {
                    html.append("<br><font color='gray' size='-2'>")
                        .append(escapeHtml(result.getNodePath()))
                        .append("</font>");
                }
                
                // Snippet on third line if present
                if (!result.getContextSnippet().isEmpty()) {
                    html.append("<br><font size='-2'>")
                        .append(escapeHtml(result.getContextSnippet()))
                        .append("</font>");
                }
                
                html.append("</html>");
                setText(html.toString());
            }
            
            return this;
        }
        
        private String getMatchTypeLabel(MatchType type) {
            switch (type) {
                case TITLE:
                    return Messages.getString("search.match.title");
                case CONTENT:
                    return Messages.getString("search.match.content");
                case BOTH:
                    return Messages.getString("search.match.both");
                default:
                    return "";
            }
        }
        
        private String escapeHtml(String text) {
            return text.replace("&", "&amp;")
                       .replace("<", "&lt;")
                       .replace(">", "&gt;");
        }
    }
}