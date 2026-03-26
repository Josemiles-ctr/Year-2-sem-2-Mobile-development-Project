# GitHub Actions Pinning Guide

## Overview

This guide explains how to pin GitHub Actions to specific commit SHAs for enhanced security and reproducibility.

## Why Pin to Commit SHAs?

Using version tags like `@v4` automatically tracks all future releases within that major version, which can introduce:
- **Regressions** - Bug fixes in newer versions may change behavior
- **Security changes** - Behavioral modifications without explicit control
- **Unexpected behavior** - Updates happen automatically without visibility

**Pinning to commit SHAs provides:**
- ✓ **Reproducibility** - Exact, consistent behavior across all workflow runs
- ✓ **Security control** - Updates only happen when intentionally deployed
- ✓ **Audit trail** - Clear history of which versions are used
- ✓ **Predictability** - No surprise behavioral changes

## How to Pin Actions

### Step 1: Find the Release
Visit the action's GitHub releases page. For example:
- `actions/checkout` → https://github.com/actions/checkout/releases
- `actions/setup-java` → https://github.com/actions/setup-java/releases
- `github/codeql-action` → https://github.com/github/codeql-action/releases

### Step 2: Locate the Commit SHA
Each release shows the commit SHA. For example:
```
Release v4.1.1
Commit: abc1234567890def
```

### Step 3: Update Your Workflow
Replace the version tag with the commit SHA:

**Before:**
```yaml
- uses: actions/checkout@v4
```

**After:**
```yaml
- uses: actions/checkout@abc12345  # v4.1.1
```

Include the version tag as a comment for easy reference when updating.

## Current Actions to Pin (In security.yml)

| Action | Current | Release Page |
|--------|---------|--------------|
| `actions/checkout` | @v4 | https://github.com/actions/checkout/releases |
| `actions/setup-java` | @v4 | https://github.com/actions/setup-java/releases |
| `actions/cache` | @v4 | https://github.com/actions/cache/releases |
| `actions/upload-artifact` | @v4 | https://github.com/actions/upload-artifact/releases |
| `actions/dependency-review-action` | @v4 | https://github.com/actions/dependency-review-action/releases |
| `github/codeql-action/upload-sarif` | @v4 | https://github.com/github/codeql-action/releases |
| `gitleaks/gitleaks-action` | @v2 | https://github.com/gitleaks/gitleaks-action/releases |

## Update Process

To update an action to a newer version:

1. Visit the action's releases page
2. Find the new version's commit SHA
3. Update `security.yml` with the new SHA
4. Include the version tag in a comment
5. Test the workflow to ensure compatibility
6. Create a PR with the update
7. Merge after review

## Example

Here's the recommended format for `.github/workflows/security.yml`:

```yaml
- name: Checkout
  # Pinned to v4.1.1 for security and reproducibility
  # To update: Find the latest commit SHA at https://github.com/actions/checkout/releases
  uses: actions/checkout@abc1234567890def  # v4.1.1
  with:
    fetch-depth: 0
```

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Security hardening for GitHub Actions](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions)
- [About dependabot version updates](https://docs.github.com/en/code-security/dependabot/dependabot-version-updates/about-dependabot-version-updates)

## Future Improvements

- Consider using [Dependabot](https://docs.github.com/en/code-security/dependabot/working-with-dependabot) to automate action updates
- Implement automated testing of action updates before merging
- Establish a quarterly review schedule for action updates
