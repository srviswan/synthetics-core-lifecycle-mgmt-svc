#!/usr/bin/env python3
"""
JIRA to Excel Export Script
Extracts data from JIRA Cloud using REST API and exports to Excel
"""

import requests
import pandas as pd
from requests.auth import HTTPBasicAuth
import json
import sys
from datetime import datetime
import os

# Configuration
JIRA_URL = "https://yourcompany.atlassian.net"  # Replace with your JIRA URL
USERNAME = "your-email@company.com"             # Replace with your email
API_TOKEN = "your-api-token"                    # Replace with your API token
PROJECT_KEY = "YOUR_PROJECT"                    # Replace with your project key

def get_jira_issues(jql_query, max_results=1000):
    """
    Fetch issues from JIRA using JQL query
    """
    url = f"{JIRA_URL}/rest/api/3/search"
    
    auth = HTTPBasicAuth(USERNAME, API_TOKEN)
    headers = {
        "Accept": "application/json",
        "Content-Type": "application/json"
    }
    
    payload = {
        "jql": jql_query,
        "maxResults": max_results,
        "fields": [
            "key", "summary", "status", "assignee", "reporter", 
            "created", "updated", "priority", "issuetype",
            "description", "labels", "components", "fixVersions",
            "resolution", "resolutiondate", "duedate"
        ]
    }
    
    try:
        response = requests.post(url, json=payload, auth=auth, headers=headers)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error fetching JIRA issues: {e}")
        return None

def format_issue_data(issues_data):
    """
    Format JIRA issues data for Excel export
    """
    issues_list = []
    
    for issue in issues_data.get('issues', []):
        fields = issue.get('fields', {})
        
        # Extract assignee information
        assignee = fields.get('assignee')
        assignee_name = assignee.get('displayName') if assignee else 'Unassigned'
        
        # Extract reporter information
        reporter = fields.get('reporter')
        reporter_name = reporter.get('displayName') if reporter else 'Unknown'
        
        # Extract status
        status = fields.get('status', {})
        status_name = status.get('name', 'Unknown')
        
        # Extract priority
        priority = fields.get('priority', {})
        priority_name = priority.get('name', 'Unknown')
        
        # Extract issue type
        issuetype = fields.get('issuetype', {})
        issuetype_name = issuetype.get('name', 'Unknown')
        
        # Extract components
        components = fields.get('components', [])
        components_list = [comp.get('name') for comp in components]
        components_str = ', '.join(components_list) if components_list else 'None'
        
        # Extract labels
        labels = fields.get('labels', [])
        labels_str = ', '.join(labels) if labels else 'None'
        
        # Format dates
        created_date = fields.get('created', '')[:10] if fields.get('created') else ''
        updated_date = fields.get('updated', '')[:10] if fields.get('updated') else ''
        due_date = fields.get('duedate', '') if fields.get('duedate') else ''
        
        issue_dict = {
            'Key': issue.get('key', ''),
            'Summary': fields.get('summary', ''),
            'Status': status_name,
            'Assignee': assignee_name,
            'Reporter': reporter_name,
            'Priority': priority_name,
            'Issue Type': issuetype_name,
            'Components': components_str,
            'Labels': labels_str,
            'Created': created_date,
            'Updated': updated_date,
            'Due Date': due_date,
            'Description': fields.get('description', {}).get('content', [{}])[0].get('content', [{}])[0].get('text', '') if fields.get('description') else ''
        }
        
        issues_list.append(issue_dict)
    
    return issues_list

def export_to_excel(issues_list, filename=None):
    """
    Export issues data to Excel file
    """
    if not filename:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"jira_export_{timestamp}.xlsx"
    
    # Create DataFrame
    df = pd.DataFrame(issues_list)
    
    # Create Excel writer with formatting
    with pd.ExcelWriter(filename, engine='openpyxl') as writer:
        # Write main data
        df.to_excel(writer, sheet_name='JIRA Issues', index=False)
        
        # Get the workbook and worksheet
        workbook = writer.book
        worksheet = writer.sheets['JIRA Issues']
        
        # Auto-adjust column widths
        for column in worksheet.columns:
            max_length = 0
            column_letter = column[0].column_letter
            
            for cell in column:
                try:
                    if len(str(cell.value)) > max_length:
                        max_length = len(str(cell.value))
                except:
                    pass
            
            adjusted_width = min(max_length + 2, 50)  # Max width of 50
            worksheet.column_dimensions[column_letter].width = adjusted_width
        
        # Add summary sheet
        summary_data = {
            'Metric': ['Total Issues', 'Export Date', 'Project', 'Unique Assignees', 'Unique Statuses'],
            'Value': [
                len(issues_list),
                datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                PROJECT_KEY,
                len(set(issue['Assignee'] for issue in issues_list)),
                len(set(issue['Status'] for issue in issues_list))
            ]
        }
        
        summary_df = pd.DataFrame(summary_data)
        summary_df.to_excel(writer, sheet_name='Summary', index=False)
    
    print(f"✅ Excel file exported: {filename}")
    return filename

def main():
    """
    Main function to export JIRA data to Excel
    """
    print("🔗 JIRA to Excel Export Tool")
    print("============================")
    print(f"JIRA URL: {JIRA_URL}")
    print(f"Project: {PROJECT_KEY}")
    print("")
    
    # Example JQL queries - modify as needed
    jql_queries = {
        "All Open Issues": f"project = {PROJECT_KEY} AND status != Done",
        "My Issues": f"project = {PROJECT_KEY} AND assignee = currentUser()",
        "Recent Issues": f"project = {PROJECT_KEY} AND created >= -30d",
        "High Priority": f"project = {PROJECT_KEY} AND priority in (Highest, High)"
    }
    
    print("📋 Available Export Options:")
    for i, (name, query) in enumerate(jql_queries.items(), 1):
        print(f"   {i}. {name}: {query}")
    
    print("   5. Custom JQL Query")
    print("")
    
    try:
        choice = input("Select export option (1-5): ").strip()
        
        if choice in ['1', '2', '3', '4']:
            query_name = list(jql_queries.keys())[int(choice) - 1]
            jql_query = list(jql_queries.values())[int(choice) - 1]
        elif choice == '5':
            jql_query = input("Enter custom JQL query: ").strip()
            query_name = "Custom Query"
        else:
            print("Invalid choice. Using default query.")
            jql_query = jql_queries["All Open Issues"]
            query_name = "All Open Issues"
        
        print(f"\n🔍 Executing query: {jql_query}")
        
        # Fetch issues from JIRA
        issues_data = get_jira_issues(jql_query)
        
        if not issues_data:
            print("❌ Failed to fetch issues from JIRA")
            return
        
        total_issues = issues_data.get('total', 0)
        returned_issues = len(issues_data.get('issues', []))
        
        print(f"📊 Found {total_issues} total issues, fetched {returned_issues}")
        
        if returned_issues == 0:
            print("⚠️  No issues found matching the query")
            return
        
        # Format data for Excel
        formatted_issues = format_issue_data(issues_data)
        
        # Export to Excel
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"jira_export_{query_name.lower().replace(' ', '_')}_{timestamp}.xlsx"
        
        export_to_excel(formatted_issues, filename)
        
        print(f"\n🎉 Export completed successfully!")
        print(f"📁 File saved: {os.path.abspath(filename)}")
        print(f"📊 Exported {len(formatted_issues)} issues")
        
    except KeyboardInterrupt:
        print("\n⚠️  Export cancelled by user")
    except Exception as e:
        print(f"❌ Error during export: {e}")

if __name__ == "__main__":
    # Check dependencies
    try:
        import pandas as pd
        import openpyxl
    except ImportError as e:
        print("❌ Missing required packages. Install with:")
        print("   pip install pandas openpyxl requests")
        sys.exit(1)
    
    main()
