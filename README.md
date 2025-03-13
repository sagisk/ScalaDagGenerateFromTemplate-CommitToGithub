

# DAG - Velocity Templating
## Features
**Velocity Template Engine**: Utilizes the VelocityEngine to process templates and generate files.

**Resource Management**: Uses Resource from cats-effect to manage file writers, ensuring proper resource cleanup.

**Functional Programming**: Applies functional programming principles to handle side effects (e.g., file writing) using IO from cats-effect.

**Components**  
`initializeVelocityEngine()`: Initializes the Velocity engine with necessary configuration.  
`getFileWriterResources()`: Provides a resource that handles opening and closing a file writer.  
`uploadToPath()`: The main function to load the template, inject context, and write the resulting output to a specified file.  

## Setup
**Setup template.vm** - copy the real template to `dag-template/tempate.vm` 

To use this app, make sure to have the following dependencies in your project:

### Dependencies
`cats-effect`: For functional effects and resource management.  
`velocity-engine-core`: For processing the Velocity templates.  
`scala-logging (optional)`: For logging, if desired.

# Commit to Github

## Features
**GitHub Personal Access Token Authentication**: Uses a GitHub personal access token for authentication when interacting with the GitHub API.

**API Interaction**: Communicates with the GitHub API to check if a file exists, get the file's SHA, and commit new content to a repository.

**Asynchronous Execution**: Makes API requests asynchronously using Akka HTTP and Akka Streams, providing non-blocking execution.

**Error Handling**: Includes error handling to capture potential issues when interacting with the GitHub API.

## Setup
**GitHub Personal Access Token**: Create a GitHub personal access token with repository access by following GitHub's guide. Replace the placeholder "your_token" with your token.    
**GitHub Repository**: Specify your GitHub repository details (owner, repo, branch) and the file (filePath) you want to commit.  
**GitHub Repository Permissions**: Specify "Contents" repository permissions (read & write)   
**Dependencies**: Add the necessary dependencies to your build.sbt.  
**File Path**: Update filePath with the file you want to commit (e.g., "path_to_the_dag").

### References:
- Look here for the docs on GitHub Commit:
  https://docs.github.com/en/rest/repos/contents?apiVersion=2022-11-28
