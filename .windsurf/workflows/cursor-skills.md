---
auto_execution_mode: 2
---

`---
description: Cursor Skills - Define cursor navigation and manipulation capabilities for the agent

---

# Cursor Skills

This workflow defines the cursor-related skills and capabilities that the AI agent can perform in the Windsurf IDE.

## Cursor Navigation Skills

### Basic Movement

- **Line Navigation**: Move cursor to specific line numbers
- **Word Navigation**: Jump between words, forward and backward
- **Paragraph Navigation**: Move between paragraphs or code blocks
- **File Navigation**: Jump to file definitions and references

### Selection Skills

- **Character Selection**: Select individual characters
- **Word Selection**: Select whole words
- **Line Selection**: Select entire lines
- **Block Selection**: Select code blocks or multi-line selections
- **Smart Selection**: Intelligently select logical code units (functions, classes, statements)

### Editing Skills

- **Insert Mode**: Insert text at cursor position
- **Replace Mode**: Replace text at cursor position
- **Delete Operations**: Delete characters, words, lines, or selections
- **Copy/Paste**: Copy and paste selections
- **Cut/Paste**: Cut and paste selections

## Advanced Cursor Skills

### Code-Aware Navigation

- **Go to Definition**: Navigate to symbol definitions
- **Find References**: Locate all references to a symbol
- **Symbol Search**: Search for symbols within the codebase
- **File Structure**: Navigate through file structure outline

### Multi-Cursor Operations

- **Multi-Cursor Selection**: Create multiple cursor positions
- **Simultaneous Editing**: Edit at multiple cursor positions simultaneously
- **Cursor Cloning**: Clone cursor to multiple locations

### Smart Cursor Positioning

- **Context-Aware Placement**: Position cursor based on code context
- **Auto-Indentation**: Maintain proper indentation when moving cursor
- **Bracket Matching**: Navigate between matching brackets/parentheses
- **Quote Matching**: Navigate between matching quotes

## Cursor Position Awareness

### Context Understanding

- **Scope Detection**: Understand current code scope (function, class, block)
- **Syntax Awareness**: Recognize current syntax context (comment, string, code)
- **Indentation Level**: Track current indentation depth
- **Line Context**: Understand surrounding lines for context

### Position Reporting

- **Line/Column Reporting**: Report current line and column position
- **Selection Reporting**: Report current selection bounds
- **File Position**: Report position within the overall file
- **Workspace Context**: Understand position within the project structure

## Cursor Skill Rules

### Best Practices

1. Always maintain valid syntax when editing code
2. Preserve code formatting and indentation
3. Respect existing code style and conventions
4. Use smart selection over manual selection when possible
5. Leverage code-aware navigation for efficiency

### Safety Rules

1. Never delete code without understanding its purpose
2. Always verify cursor position before destructive operations
3. Preserve file structure and organization
4. Maintain code functionality when editing
5. Ask for confirmation on major changes

### Performance Guidelines

1. Use efficient navigation methods (go-to-definition vs manual search)
2. Batch cursor operations when possible
3. Minimize unnecessary cursor movements
4. Use multi-cursor for repetitive edits
5. Leverage IDE features for complex operations

## Usage Examples

### Navigate to Function

```
1. Use "Go to Definition" to find function implementation
2. Cursor automatically positions at function declaration
3. Agent can now analyze or edit the function
```

### Smart Selection

```
1. Agent identifies a logical code block
2. Uses smart selection to highlight the entire block
3. Can then perform operations on the selected block
```

### Multi-Cursor Edit

```
1. Identify pattern to edit across multiple locations
2. Create cursors at all relevant positions
3. Perform simultaneous edit at all cursor positions
```

## Skill Levels

### Level 1: Basic

- Line-by-line navigation
- Simple character/word selection
- Basic insert/delete operations

### Level 2: Intermediate

- Code-aware navigation
- Smart selection
- Multi-cursor operations

### Level 3: Advanced

- Complex multi-cursor patterns
- Advanced code structure navigation
- Context-aware editing
