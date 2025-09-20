#!/bin/bash

# Simple JIRA to Excel Export Script using curl and jq
# Requires: curl, jq, and basic authentication setup

set -e

# Configuration - UPDATE THESE VALUES
JIRA_URL="https://yourcompany.atlassian.net"
USERNAME="your-email@company.com"
API_TOKEN="your-api-token"
PROJECT_KEY="YOUR_PROJECT"

# Function to export JIRA issues to CSV (can be opened in Excel)
export_jira_to_csv() {
    local jql_query="$1"
    local output_file="$2"
    
    echo "🔍 Fetching JIRA issues..."
    echo "Query: $jql_query"
    
    # Fetch issues from JIRA API
    response=$(curl -s -u "$USERNAME:$API_TOKEN" \
        -H "Accept: application/json" \
        -H "Content-Type: application/json" \
        "$JIRA_URL/rest/api/3/search" \
        -d "{
            \"jql\": \"$jql_query\",
            \"maxResults\": 1000,
            \"fields\": [\"key\", \"summary\", \"status\", \"assignee\", \"reporter\", \"created\", \"updated\", \"priority\", \"issuetype\"]
        }")
    
    # Check if request was successful
    if echo "$response" | jq -e '.issues' > /dev/null 2>&1; then
        echo "✅ Successfully fetched issues"
        
        # Extract and format data
        echo "📊 Processing data..."
        
        # Create CSV header
        echo "Key,Summary,Status,Assignee,Reporter,Priority,Issue Type,Created,Updated" > "$output_file"
        
        # Process each issue and add to CSV
        echo "$response" | jq -r '.issues[] | [
            .key,
            .fields.summary,
            .fields.status.name,
            (.fields.assignee.displayName // "Unassigned"),
            (.fields.reporter.displayName // "Unknown"),
            (.fields.priority.name // "Unknown"),
            .fields.issuetype.name,
            (.fields.created[:10]),
            (.fields.updated[:10])
        ] | @csv' >> "$output_file"
        
        # Count issues
        issue_count=$(echo "$response" | jq '.issues | length')
        total_count=$(echo "$response" | jq '.total')
        
        echo "✅ Export completed!"
        echo "📁 File: $output_file"
        echo "📊 Exported: $issue_count issues (Total available: $total_count)"
        
    else
        echo "❌ Error fetching issues from JIRA"
        echo "Response: $response"
        return 1
    fi
}

# Function to setup API token instructions
show_setup_instructions() {
    echo "🔧 SETUP INSTRUCTIONS"
    echo "====================="
    echo ""
    echo "1️⃣ Get JIRA API Token:"
    echo "   - Go to: https://id.atlassian.com/manage-profile/security/api-tokens"
    echo "   - Click 'Create API token'"
    echo "   - Copy the generated token"
    echo ""
    echo "2️⃣ Update Configuration:"
    echo "   - Edit this script and replace:"
    echo "     JIRA_URL=\"https://yourcompany.atlassian.net\""
    echo "     USERNAME=\"your-email@company.com\""
    echo "     API_TOKEN=\"your-api-token\""
    echo "     PROJECT_KEY=\"YOUR_PROJECT\""
    echo ""
    echo "3️⃣ Common JQL Queries:"
    echo "   - All open issues: project = YOUR_PROJECT AND status != Done"
    echo "   - My issues: assignee = currentUser()"
    echo "   - Recent issues: created >= -30d"
    echo "   - High priority: priority in (Highest, High)"
    echo ""
}

# Main execution
main() {
    echo "📊 JIRA to Excel Export Tool"
    echo "============================"
    echo ""
    
    # Check dependencies
    if ! command -v curl &> /dev/null; then
        echo "❌ curl is required but not installed"
        exit 1
    fi
    
    if ! command -v jq &> /dev/null; then
        echo "❌ jq is required but not installed"
        echo "Install with: brew install jq (macOS) or apt-get install jq (Ubuntu)"
        exit 1
    fi
    
    # Check if configuration is updated
    if [[ "$JIRA_URL" == "https://yourcompany.atlassian.net" ]]; then
        echo "⚠️  Configuration not updated!"
        show_setup_instructions
        exit 1
    fi
    
    # Get export parameters
    echo "📋 Export Options:"
    echo "1. All open issues"
    echo "2. My assigned issues"
    echo "3. Recent issues (last 30 days)"
    echo "4. High priority issues"
    echo "5. Custom JQL query"
    echo ""
    
    read -p "Select option (1-5): " choice
    
    case $choice in
        1)
            jql_query="project = $PROJECT_KEY AND status != Done"
            output_file="jira_open_issues_$(date +%Y%m%d_%H%M%S).csv"
            ;;
        2)
            jql_query="assignee = currentUser()"
            output_file="jira_my_issues_$(date +%Y%m%d_%H%M%S).csv"
            ;;
        3)
            jql_query="project = $PROJECT_KEY AND created >= -30d"
            output_file="jira_recent_issues_$(date +%Y%m%d_%H%M%S).csv"
            ;;
        4)
            jql_query="project = $PROJECT_KEY AND priority in (Highest, High)"
            output_file="jira_high_priority_$(date +%Y%m%d_%H%M%S).csv"
            ;;
        5)
            read -p "Enter JQL query: " jql_query
            output_file="jira_custom_export_$(date +%Y%m%d_%H%M%S).csv"
            ;;
        *)
            echo "Invalid choice. Using default (all open issues)"
            jql_query="project = $PROJECT_KEY AND status != Done"
            output_file="jira_export_$(date +%Y%m%d_%H%M%S).csv"
            ;;
    esac
    
    echo ""
    export_jira_to_csv "$jql_query" "$output_file"
    
    echo ""
    echo "💡 To open in Excel:"
    echo "   - macOS: open '$output_file'"
    echo "   - Windows: start '$output_file'"
    echo "   - Linux: libreoffice '$output_file'"
}

# Run main function
main "$@"
