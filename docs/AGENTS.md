# Agent instructions

## Required editing workflow

- All repository edits must be made through the GitHub plugin. This applies to creating, updating, deleting, renaming, and moving files, including code, documentation, configuration, tests, assets, and this AGENTS.md file.
- Never edit files locally, including in the workspace, a clone, a worktree, or a temporary copy. Do not prepare local edits for a later commit or upload.
- Do not use local editors, apply_patch, shell commands, scripts, formatters, code generators, Git commands, the GitHub CLI, direct HTTP/API requests, or browser editing as a substitute for the GitHub plugin.
- Use the GitHub plugin to read the current target branch and file contents before making changes. Use the current file SHA for updates and deletions, preserve unrelated content, and verify the resulting remote files or diff through the plugin.
- Perform any required branch creation, commits, and pull request changes through the GitHub plugin as well.
- Local inspection is permitted only when it is read-only. Do not run tools that modify local files, including automatic fixes or generation of build/test artifacts. Use remote validation when available and report any checks that cannot be performed within this restriction.
- If the GitHub plugin is unavailable, lacks a required operation, or denies access, stop the affected edit and explain the limitation. Never fall back to local edits or another write mechanism.
- Apply these requirements to any delegated agents working on this repository.
